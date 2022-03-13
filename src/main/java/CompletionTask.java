import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Consumer;

public class CompletionTask implements BaseTask {

    private final Integer id;
    private final Integer type;
    private final Integer counter;
    private final Consumer<Status> callback;
    private Status status;

    public CompletionTask() {
        this.callback = null;
        this.counter = 0;
        this.type = Type.COMPLETION;
        this.id = 0;
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

    @Override
    public Consumer<Status> getCallback() {
        return callback;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
