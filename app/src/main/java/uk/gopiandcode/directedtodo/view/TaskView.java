package uk.gopiandcode.directedtodo.view;


import android.view.View;

import uk.gopiandcode.directedtodo.core.ViewFragment;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.presenter.TaskPresenter;

public class TaskView extends ViewFragment<TaskPresenter> implements TaskDisplay {
    @Override
    protected void setListeners() {

    }

    @Override
    protected void populate() {

    }

    @Override
    protected void init() {

    }

    @Override
    protected void setUi(View v) {

    }

    @Override
    protected TaskPresenter createPresenter() {
        return null;
    }

    @Override
    protected int layout() {
        return 0;
    }

    @Override
    public void setTask(TaskModel model) {

    }
}
