import java.util.function.Consumer;

public interface BaseTask {

    public static class Type {
        public static final int BATCH = 0;
        public static final int SINGLE = 1;
        public static final int COMPLETION = 3;
    }

    Integer getType();
    Integer getId();
    Integer getCounter();
    String getQuery();
    Status getStatus();
    Consumer<Status> getCallback();

}
