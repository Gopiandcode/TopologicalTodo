package uk.gopiandcode.directedtodo.view;


import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import uk.gopiandcode.directedtodo.R;
import uk.gopiandcode.directedtodo.algorithm.TopologicalTaskComparator;
import uk.gopiandcode.directedtodo.core.ViewFragment;
import uk.gopiandcode.directedtodo.data.TaskListModel;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.presenter.TaskListPresenter;

public class TaskListView extends ViewFragment<TaskListPresenter> implements TaskListDisplay {

    private ListView todoList;
    private List<TaskModel> taskModels;
    private TaskListModel tasklist;
    private EditText addTaskText;
    private FloatingActionButton addTaskbutton;
    private boolean isEnabled = true;

    public TaskListView() {
        taskModels = new ArrayList<>();
    }
    private void setAddTaskButtonEnable(boolean enabled) {
       if(isEnabled == enabled) return;
       addTaskbutton.setEnabled(enabled);
       addTaskbutton.setActivated(enabled);

       isEnabled = enabled;
    }

    private void addTask(String title) {
        tasklist.addNewTask(title);
        addTaskText.getText().clear();
        ((TaskModelListAdapter)todoList.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter<TaskModel>) todoList.getAdapter()).sort(new TopologicalTaskComparator(this.tasklist));
    }

    @Override
    protected void setListeners() {
        todoList.setAdapter(new TaskModelListAdapter(getActivity(), taskModels, new InternalOnTaskCompleteListener()));
        addTaskText.addTextChangedListener(new InternalTextWatcher());
        addTaskbutton.setOnClickListener(view -> {
            String string = addTaskText.getText().toString();
            addTask(string);
        });
    }

    @Override
    protected void populate() {
        mPresenter.loadTaskModels();
        taskModels = this.tasklist.getTasks();
    }

    @Override
    protected void init() {
        setAddTaskButtonEnable(false);
    }

    @Override
    protected void setUi(View v) {
        todoList = v.findViewById(R.id.todo_list);
        addTaskText = v.findViewById(R.id.todo_text_input);
        addTaskbutton = v.findViewById(R.id.todo_add_button);
    }

    @Override
    protected TaskListPresenter createPresenter() {
        return new TaskListPresenter(this, getContext());
    }

    @Override
    protected int layout() {
        return R.layout.task_list_layout;
    }

    @Override
    public void setTaskList(TaskListModel tasklist) {
        this.tasklist = tasklist;
    }

    private void openTaskPanel(TaskModel taskModel) {
        Log.d("model", "openTaskPanel: " + taskModel);
        getActivity().getFragmentManager().beginTransaction().add(R.id.directed_todo_container, TaskView.newInstance(taskModel), taskModel.getTitle()).addToBackStack(null).commit();
        getFragmentManager().addOnBackStackChangedListener(() -> {
            ((ArrayAdapter<TaskModel>)todoList.getAdapter()).notifyDataSetChanged();
            ((ArrayAdapter<TaskModel>) todoList.getAdapter()).sort(new TopologicalTaskComparator(this.tasklist));
        });
    }


    private class InternalOnTaskCompleteListener implements OnTaskCompleteListener {
        @Override
        public void onTaskComplete(TaskModel taskModel) {
            taskModel.removeTask();
            ((ArrayAdapter<TaskModel>)todoList.getAdapter()).notifyDataSetChanged();
            ((ArrayAdapter<TaskModel>) todoList.getAdapter()).sort(new TopologicalTaskComparator(tasklist));
        }

        @Override
        public void onTaskSelected(TaskModel taskModel) {
            openTaskPanel(taskModel);
        }
    }

    private class InternalTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setAddTaskButtonEnable(!(charSequence.length() == 0));
            String converted = charSequence.toString();
            if(converted.contains("\n")) {
                converted = converted.replace("\n", "");
                addTask(converted);
            }
       }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
