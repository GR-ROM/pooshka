import com.github.javafaker.Faker;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class Main {

    public static final int THREAD_NUM = 20;
    public static final int QUEUE_CAPACITY = 20000;
    public static final String url = "jdbc:mysql://192.168.0.222:3307/vodman_perm";
    private static final String user = "root";
    private static final String password = "password";

    public static List<List<Object>> genBatch(int size) {
        List<List<Object>> parameters = new ArrayList<>();
        Faker faker = new Faker();
        Random random = new Random();
        for (int i=0;i!=size;i++) {
            List<Object> batchList = new ArrayList<>();
            batchList.add(faker.name().firstName());
            batchList.add(faker.name().lastName());
            batchList.add(faker.name().username());
            parameters.add(batchList);
        }
        return parameters;
    }

    public static void main(String[] args) {
        Instant instant = Clock.systemUTC().instant();
        final float[] lastRPS = {0f};
        LoadTest loadTest = new LoadTest(url, user, password, QUEUE_CAPACITY, THREAD_NUM);
        Consumer<Status> cb = status -> {
            Instant time = Clock.systemDefaultZone().instant().minusMillis(instant.toEpochMilli());
            System.out.printf("%s %d, %.2fqps, %dms, %s%n",
                    LocalTime.ofInstant(time, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    status.getTask().getId(),
                    lastRPS[0],
                    status.getExecutionTimeMillis(),
                    status);
        };

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                long seconds = Clock.systemDefaultZone().instant().minusMillis(instant.toEpochMilli()).getEpochSecond();
                lastRPS[0] = ((float)loadTest.getRequestPerSecond().get() / seconds);
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1, 1000);

        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Job result = loadTest.getResultQueue().take();
                    result.getTasks().forEach(task -> {
//                        if (task.getType().equals(BaseTask.Type.SINGLE)) {
//                            ((SingleTask)task).getResultSet().ifPresent(resultSet -> {
//                                try {
//                                    Random random = new Random();
//                                    resultSet.next();
//                                    String name = resultSet.getString("first_name");
//                                    int id = resultSet.getInt("id");
//                                    char a[] = name.toCharArray();
//                                    int index = random.nextInt(name.length());
//                                    a[index] = (char) (random.nextInt('z') + '0');
//                                    name = String.copyValueOf(a);
//                                    loadTest.getJobQueue().put(new SingleTask(status.getTask().getId(), 1,
//                                            "UPDATE person_v2 SET first_name = '" + name + "' WHERE id = ", cb));
//                                } catch (SQLException | InterruptedException sqlException) {
//                                    sqlException.printStackTrace();
//                                }
//                            });
//                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.start();
        Job job = null;
        for (int i=0;i!=1000;i++) {
            try {
                List<BaseTask> tasks = new ArrayList<>();
                tasks.add(new ParametrizedBatchTask(i,
                        "INSERT INTO person_v2 (first_name, second_name, last_name) VALUES (?, ?, ?)",
                        genBatch(1000),
                        cb));
                job = new Job(i, false, 0, tasks);
                loadTest.getJobQueue().put(job);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            synchronized (job) {
                job.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
        thread.interrupt();
        timer.cancel();
        loadTest.stop();
        System.out.println("All threads are terminated");
    }
}
