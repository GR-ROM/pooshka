import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Consumer;

public class SingleTask implements BaseTask {

    private final Integer id;
    private final Integer type;
    private final Integer counter;
    private final String query;
    private Optional<ResultSet> resultSet;
    private final Consumer<Status> callback;
    private Status status;

    public SingleTask(Integer id, Integer counter, String query, Consumer<Status> callback) {
        this.id = id;
        this.type = Type.SINGLE;
        this.counter = counter;
        this.query = query;
        this.resultSet = Optional.empty();
        this.callback = callback;
    }

    public String getQuery() {
        return query;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = Optional.ofNullable(resultSet);
    }

    public Optional<ResultSet> getResultSet() {
        return resultSet;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getCounter() {
        return this.counter;
    }

    @Override
    public Integer getType() {
        return type;
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
