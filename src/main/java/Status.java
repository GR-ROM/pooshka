public class Status {

    public static class Code {
        public static final int UNASSIGNED = 0;
        public static final int OK = 1;
        public static final int FAILED = 2;
    }

    private final BaseTask task;
    private Long executionTimeMillis;
    private Integer code;

    public Status(BaseTask task, Integer code, Long executionTimeMillis) {
        this.task = task;
        this.code = code;
        this.executionTimeMillis = executionTimeMillis;
    }

    public Long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    public Integer getCode() {
        return code;
    }

    public BaseTask getTask() {
        return task;
    }

    public void setExecutionTimeMillis(Long executionTimeMillis) {
        this.executionTimeMillis = executionTimeMillis;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        switch (code) {
            case Code.UNASSIGNED -> { return "UNASSIGNED"; }
            case Code.OK -> { return "OK"; }
            case Code.FAILED -> { return "FAILED"; }
        }
        return "";
    }
}
