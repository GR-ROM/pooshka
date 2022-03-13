import java.sql.*;
import java.util.List;

public class Worker implements Runnable {

    private final Integer id;
    private final LoadTest loadTest;
    private boolean terminated = false;
    private Thread thread;
    private volatile boolean suspended = false;
    private Object suspendMutex = new Object();

    public Worker(Integer id, LoadTest loadTest) {
        this.id = id;
        this.loadTest = loadTest;
    }

    public void run() {
        System.out.println("Thread " + id + " started");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try (Connection connection = DriverManager.getConnection(loadTest.getUrl(), loadTest.getUser(), loadTest.getPassword())) {
                    System.out.println("Thread " + id + " ready");
                    while (!Thread.currentThread().isInterrupted()) {
                        if (suspended) {
                            synchronized (suspendMutex) {
                                try {
                                    suspendMutex.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Job job = loadTest.getJobQueue().take();
                        job.setCurrentStage(0);
                        job.getTasks().forEach(baseTask -> {
                            long delta = 0;
                            for (int i = 0; i != baseTask.getCounter(); i++) {
                                try {
                                    if (baseTask.getType().equals(BaseTask.Type.BATCH)) {
                                        PreparedStatement preparedStatement = connection.prepareStatement(baseTask.getQuery());
                                        for (List<Object> parameters : ((ParametrizedBatchTask)baseTask).getParameters()) {
                                            preparedStatement.clearParameters();
                                            for (Object parameter : parameters) {
                                                try {
                                                    if (parameter instanceof String) {
                                                        preparedStatement.setString(parameters.indexOf(parameter) + 1, (String) parameter);
                                                    }
                                                    if (parameter instanceof Integer) {
                                                        preparedStatement.setInt(parameters.indexOf(parameter) + 1, (Integer) parameter);
                                                    }
                                                    if (parameter instanceof Long) {
                                                        preparedStatement.setLong(parameters.indexOf(parameter) + 1, (Long) parameter);
                                                    }
                                                }
                                                catch (SQLException e) {
                                                    System.out.println("Wrong parameter");
                                                    throw new SQLException("Wrong parameter");
                                                }
                                            }
                                            preparedStatement.addBatch();
                                        }
                                        delta = System.currentTimeMillis();
                                        preparedStatement.executeBatch();
                                        delta = System.currentTimeMillis() - delta;
                                        loadTest.getRequestPerSecond().addAndGet(((ParametrizedBatchTask)baseTask).getParameters().size());
                                    } else {
                                        Statement statement = connection.createStatement();
                                        if (baseTask.getType() == BaseTask.Type.SINGLE) {
                                            delta = System.currentTimeMillis();
                                            statement.execute(baseTask.getQuery());
                                            delta = System.currentTimeMillis() - delta;
                                            ((SingleTask) baseTask).setResultSet(statement.getResultSet());
                                            loadTest.getRequestPerSecond().incrementAndGet();
                                        }
                                    }
                                    baseTask.getStatus().setCode(Status.Code.OK);
                                    baseTask.getStatus().setExecutionTimeMillis(delta);
                                } catch (SQLException e) {
                                    if (delta != 0) {
                                        delta = System.currentTimeMillis() - delta;
                                    }
                                    baseTask.getStatus().setCode(Status.Code.FAILED);
                                    baseTask.getStatus().setExecutionTimeMillis(delta);
                                }
                                baseTask.getCallback().accept(baseTask.getStatus());
                                // loadTest.getStatusQueue().put(task.getStatus());
                            }
                        });
                        synchronized (job) {
                            job.notify();
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                if (suspended) {
                    synchronized (suspendMutex) {
                        try {
                            suspendMutex.wait();
                        } catch (InterruptedException ie) {
                            continue;
                        }
                    }
                } else {
                    Thread.currentThread().interrupt();
                }
            }
            catch (SQLException sqlException) {
                Thread.currentThread().interrupt();
                sqlException.printStackTrace();
            }
        }
        synchronized (this) {
            this.notify();
            this.terminated = true;
            System.out.println("Thread " + this.id + " terminated");
        }
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public void suspend() {
        this.suspended = true;
        this.thread.interrupt();
    }

    public void wake() {
        this.suspended = false;
        synchronized (this.suspendMutex) {
            this.suspendMutex.notify();
        }
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

}
