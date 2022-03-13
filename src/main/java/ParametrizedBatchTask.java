import java.util.List;
import java.util.function.Consumer;

public class ParametrizedBatchTask implements BaseTask {

        private final Integer id;
        private final Integer type;
        private final Integer counter;
        private final String query;
        private final List<List<Object>> parameters;
        private final Consumer<Status> callback;
        private Status status;

        public ParametrizedBatchTask(Integer id, String query, List<List<Object>> parameters, Consumer<Status> callback) {
            this.id = id;
            this.query = query;
            this.parameters = parameters;
            this.callback = callback;
            this.counter = 1;
            this.type = Type.BATCH;
            this.status = new Status(this, Status.Code.UNASSIGNED, -1l);
        }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public Integer getCounter() {
        return counter;
    }

    public String getQuery() {
        return query;
    }

    public List<List<Object>> getParameters() {
        return parameters;
    }

    @Override
    public Consumer<Status> getCallback() {
        return callback;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

