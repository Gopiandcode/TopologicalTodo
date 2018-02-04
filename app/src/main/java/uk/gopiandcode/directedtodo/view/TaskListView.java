package uk.gopiandcode.directedtodo.view;


import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gopiandcode.directedtodo.R;
import uk.gopiandcode.directedtodo.algorithm.TopologicalTaskComparator;
import uk.gopiandcode.directedtodo.core.ViewFragment;
import uk.gopiandcode.directedtodo.data.TaskListModel;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.presenter.TaskListPresenter;


public class TaskListView extends ViewFragment<TaskListPresenter> implements TaskListDisplay {

    private RecyclerView todoList;
    private List<TaskModel> taskModels;
    private TaskListModel tasklist;
    private EditText addTaskText;
    private FloatingActionButton addTaskbutton;
    private boolean isEnabled = true;
    private TaskListRecyclerView mTaskModelListAdapter;

    public TaskListView() {
        taskModels = new ArrayList<>();
    }

    private void setAddTaskButtonEnable(boolean enabled) {
        if (isEnabled == enabled) return;
        addTaskbutton.setEnabled(enabled);
        addTaskbutton.setActivated(enabled);

        isEnabled = enabled;
    }

    private void addTask(String title) {
        tasklist.addNewTask(title);
        addTaskText.getText().clear();
        taskModels.sort(new TopologicalTaskComparator(this.tasklist));
        mTaskModelListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void setListeners() {
        mTaskModelListAdapter = new TaskListRecyclerView(taskModels); // new TaskModelListAdapter(getActivity(), taskModels, new InternalOnTaskCompleteListener());
        todoList.setAdapter(mTaskModelListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(mTaskModelListAdapter));
        touchHelper.attachToRecyclerView(todoList);
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
            taskModels.sort(new TopologicalTaskComparator(this.tasklist));
            mTaskModelListAdapter.notifyDataSetChanged();
        });
    }


    private class InternalOnTaskCompleteListener implements OnTaskCompleteListener {
        @Override
        public void onTaskComplete(TaskModel taskModel) {
            taskModel.removeTask();
            taskModels.sort(new TopologicalTaskComparator(tasklist));
            mTaskModelListAdapter.notifyDataSetChanged();
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
            if (converted.contains("\n")) {
                converted = converted.replace("\n", "");
                addTask(converted);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
    }

    class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback  {
        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = 0;//ItemTouchHelper.START | ItemTouchHelper.END;
           return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if(mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition())) {
//                taskModels.sort(new TopologicalTaskComparator(tasklist));
//                mTaskModelListAdapter.notifyDataSetChanged();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }

    class TaskListRecyclerView extends RecyclerView.Adapter<TaskListRecyclerView.TaskListTaskViewHolder> implements ItemTouchHelperAdapter {
        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {

            if(fromPosition < toPosition) {
               TaskModel movingModel = mTasks.get(fromPosition);
                for(int i = fromPosition+1; i <= toPosition; i++) {
                   if(tasklist.hasCircularDependancyBetweenTasks(mTasks.get(i), movingModel)) {
                       return false;
                   }
                }

                for(int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mTasks, i, i+1);
                }

                 for(int i = fromPosition; i < toPosition; i++) {
                     mTasks.get(i).setRanking(i);
                }


            } else {
                TaskModel movingModel = mTasks.get(fromPosition);
                for(int i = fromPosition - 1; i >= toPosition; i--) {
                    if (tasklist.hasCircularDependancyBetweenTasks(movingModel, mTasks.get(i))) {
                        return false;
                    }
                }

                for(int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mTasks, i, i-1);
                }
                for(int i = fromPosition; i > toPosition; i--) {
                    mTasks.get(i).setRanking(i);
                }



            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }


        class TaskListTaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private CheckBox mCompletedTaskCheckBox;
            private TextView mTaskName;
            private TaskModel mTaskModel;

            public TaskListTaskViewHolder(View itemView) {
                super(itemView);
                mCompletedTaskCheckBox = itemView.findViewById(R.id.task_complete);
                mTaskName = itemView.findViewById(R.id.task_title);
                itemView.setOnClickListener(this);
                mCompletedTaskCheckBox.setOnClickListener(this);
                mTaskName.setOnClickListener(this);
            }

            public void bindTask(TaskModel task) {
                mTaskModel = task;
                mCompletedTaskCheckBox.setChecked(false);
                mTaskName.setText(mTaskModel.getTitle());
            }

            @Override
            public void onClick(View view) {
                int layoutPosition = getAdapterPosition();
                Log.d("Model", "onClick: Detected click at " + layoutPosition);
                if (mTaskModel != null && layoutPosition != RecyclerView.NO_POSITION) {
                    Log.d("Model", "first if passed");
                    Log.d("Model", "check " + view.getId() + " == " + mCompletedTaskCheckBox.getId());
                    if (view.getId() == mCompletedTaskCheckBox.getId()) {
                        // delete task
                        mTaskModel.removeTask();
                        mTaskModelListAdapter.notifyItemRemoved(layoutPosition);
                    } else {
                        // launch new activity
                        getActivity().getFragmentManager().beginTransaction().add(R.id.directed_todo_container, TaskView.newInstance(mTaskModel), mTaskModel.getTitle()).addToBackStack(null).commit();
                        getFragmentManager().addOnBackStackChangedListener(() -> {
                            taskModels.sort(new TopologicalTaskComparator(tasklist));
                            mTaskModelListAdapter.notifyDataSetChanged();
                        });


                    }
                }
            }
        }

        List<TaskModel> mTasks;


        public TaskListRecyclerView(List<TaskModel> mTasks) {
            this.mTasks = mTasks;

        }

        @Override
        public TaskListTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_row_layout, parent, false);
            return new TaskListTaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TaskListTaskViewHolder holder, int position) {
            TaskModel task = mTasks.get(position);
            holder.bindTask(task);
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }


}
