package uk.gopiandcode.directedtodo.view;


import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import uk.gopiandcode.directedtodo.R;
import uk.gopiandcode.directedtodo.core.ViewFragment;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.presenter.TaskPresenter;

public class TaskView extends ViewFragment<TaskPresenter> implements TaskDisplay {

    private EditText mTaskNameDependantText;
    private EditText mAddDependantText;
    private FloatingActionButton mAddDependantButton;
    private ListView mDependantTasksList;
    private Button mTaskDeleteButton;

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
        mTaskDeleteButton = (Button) v.findViewById(R.id.task_delete);
        mDependantTasksList = (ListView) v.findViewById(R.id.dependant_tasks_list);
        mAddDependantButton = (FloatingActionButton) v.findViewById(R.id.task_add_dependant_button);
        mAddDependantText = (EditText) v.findViewById(R.id.task_add_dependant_text);
        mTaskNameDependantText = (EditText) v.findViewById(R.id.task_name_text);

    }

    @Override
    protected TaskPresenter createPresenter() {
        return null;
    }

    @Override
    protected int layout() {
        return R.layout.task_layout;
    }

    @Override
    public void setTask(TaskModel model) {

    }
}
