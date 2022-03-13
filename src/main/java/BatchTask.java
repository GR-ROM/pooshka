import java.util.List;
import java.util.function.Consumer;

public class BatchTask implements BaseTask {

    private final Integer id;
    private final Integer type;
    private final Integer counter;
    private final List<String> queryBatch;
    private final Consumer<Status> callback;
    private Status status;

    public BatchTask(Integer id, Integer counter, List<String> queryBatch, Consumer<Status> callback) {
        this.id = id;
        this.type = BaseTask.Type.BATCH;
        this.counter = counter;
        this.queryBatch = queryBatch;
        this.callback = callback;
        this.status = new Status(this, Status.Code.UNASSIGNED, -1l);
    }

    public List<String> getQueryBatch() {
        return queryBatch;
    }

    @Override
    public Integer getType() {
        return this.type;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public Integer getCounter() {
        return counter;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public Consumer<Status> getCallback() {
        return this.callback;
    }
}
