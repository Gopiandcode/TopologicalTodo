package uk.gopiandcode.directedtodo.presenter;


import java.io.Serializable;

import uk.gopiandcode.directedtodo.core.BasePresenter;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.view.TaskDisplay;

public class TaskPresenter extends BasePresenter<TaskDisplay> implements Serializable {

    private final TaskModel mTaskModel;

    public TaskPresenter(TaskDisplay viewInstance, TaskModel taskmodel) {
        super(viewInstance);
        this.mTaskModel = taskmodel;
    }

    public void loadTaskModels() {
    }

}
