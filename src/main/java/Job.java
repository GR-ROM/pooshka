import java.util.List;

public class Job {

    private final Integer id;
    private final Boolean repeatable;
    private final Integer counter;
    private final List<BaseTask> tasks;
    private Integer currentStage;

    public Job(Integer id, Boolean repeatable, Integer counter, List<BaseTask> tasks) {
        this.id = id;
        this.repeatable = repeatable;
        this.counter = counter;
        this.tasks = tasks;
        this.currentStage = 0;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getRepeatable() {
        return repeatable;
    }

    public Integer getCounter() {
        return counter;
    }

    public List<BaseTask> getTasks() {
        return tasks;
    }

    public Integer getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Integer currentStage) {
        this.currentStage = currentStage;
    }

    public void incrementCurrentStage() {
        this.currentStage++;
    }
}
