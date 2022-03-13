import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    private final int threadNum;
    private final int capacity;
    private final String url;
    private final String user;
    private final String password;
    private final AtomicInteger requestPerSecond;
    private final BlockingQueue<Job> jobQueue;
    private final BlockingQueue<Job> resultQueue;
    private final List<Thread> threadList;
    private final List<Worker> workerList;

    public LoadTest(String url, String user, String password, int queueCapacity, int threadNum) {
        this.capacity = queueCapacity;
        this.threadNum = threadNum;
        this.url = url;
        this.user = user;
        this.password = password;
        this.jobQueue = new ArrayBlockingQueue<>(capacity);
        this.resultQueue = new ArrayBlockingQueue<>(capacity);
        this.requestPerSecond = new AtomicInteger(0);
        this.threadList = new ArrayList<>();
        this.workerList = new ArrayList<>();
        for (int i = 0; i != threadNum; i++) {
            Worker worker = new Worker(i, this);
            Thread thread = new Thread(worker);
            worker.setThread(thread);
            threadList.add(thread);
            workerList.add(worker);
        }
        threadList.forEach(Thread::start);
    }

    public void waitForComplete() throws InterruptedException {

    }

    public void suspend() {
        workerList.forEach(Worker::suspend);
    }

    public void resume() {
        workerList.forEach(Worker::wake);
    }

    public void stop() {
        threadList.forEach(Thread::interrupt);
        workerList.forEach(worker -> {
            synchronized (worker) {
                try {
                    while (!worker.isTerminated()) {
                        worker.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public AtomicInteger getRequestPerSecond() {
        return requestPerSecond;
    }

    public BlockingQueue<Job> getJobQueue() {
        return jobQueue;
    }

    public BlockingQueue<Job> getResultQueue() {
        return resultQueue;
    }

}
