import java.sql.*;

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
                                    if (baseTask.getType().equals(BaseTask.Type.PREPARED_STATEMENT)) {
                                        PreparedStatement preparedStatement = connection.prepareStatement(((SingleTask) baseTask).getQuery());
                                    } else {
                                        Statement statement = connection.createStatement();
                                        if (baseTask.getType() == BaseTask.Type.SINGLE) {
                                            delta = System.currentTimeMillis();
                                            statement.execute(((SingleTask) baseTask).getQuery());
                                            delta = System.currentTimeMillis() - delta;
                                            ((SingleTask) baseTask).setResultSet(statement.getResultSet());
                                            loadTest.getRequestPerSecond().incrementAndGet();
                                        }
                                        if (baseTask.getType() == BaseTask.Type.BATCH) {
                                            for (int b = 0; b != ((BatchTask) baseTask).getQueryBatch().size(); b++) {
                                                statement.addBatch(((BatchTask) baseTask).getQueryBatch().get(i));
                                            }
                                            delta = System.currentTimeMillis();
                                            statement.executeBatch();
                                            loadTest.getRequestPerSecond().addAndGet(((BatchTask) baseTask).getQueryBatch().size());
                                            delta = System.currentTimeMillis() - delta;
                                        }
                                        baseTask.getStatus().setCode(Status.Code.OK);
                                        baseTask.getStatus().setExecutionTimeMillis(delta);
                                    }
                                } catch (SQLException e) {
                                    delta = System.currentTimeMillis() - delta;
                                    baseTask.getStatus().setCode(Status.Code.FAILED);
                                    baseTask.getStatus().setExecutionTimeMillis(delta);
                                }
                                baseTask.getCallback().accept(baseTask.getStatus());
                                // loadTest.getStatusQueue().put(task.getStatus());
                            }
                        });
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
