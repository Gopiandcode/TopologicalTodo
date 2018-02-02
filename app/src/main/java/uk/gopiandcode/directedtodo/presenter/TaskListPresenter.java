package uk.gopiandcode.directedtodo.presenter;


import android.content.Context;

import uk.gopiandcode.directedtodo.core.BasePresenter;
import uk.gopiandcode.directedtodo.data.TaskListModel;
import uk.gopiandcode.directedtodo.view.TaskListDisplay;

public class TaskListPresenter extends BasePresenter<TaskListDisplay> {

    private final Context context;
    private TaskListModel tasklist;

    public TaskListPresenter(TaskListDisplay viewInstance, Context context) {
        super(viewInstance);
        this.context = context;
        this.tasklist = new TaskListModel(context);
    }

    public void loadTaskModels() {
        getView().setTaskList(tasklist);
    }


}
