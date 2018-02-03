package uk.gopiandcode.directedtodo.view;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import uk.gopiandcode.directedtodo.R;
import uk.gopiandcode.directedtodo.core.ViewFragment;
import uk.gopiandcode.directedtodo.data.TaskModel;
import uk.gopiandcode.directedtodo.presenter.TaskPresenter;

public class TaskView extends ViewFragment<TaskPresenter> implements TaskDisplay {

    private EditText mTaskNameDependantText;
    private AutoCompleteTextView mAddDependantText;
    private FloatingActionButton mAddDependantButton;
    private ListView mDependantTasksList;
    private Button mTaskDeleteButton;
    private TaskModel mTaskModel;

    private static String TASK_MODEL = "TASK_MODEL";
    private List<TaskModel> mApplicableDependants;
    private List<TaskModel> mDependants;

    public static TaskView newInstance(TaskModel model) {
        TaskView taskView = new TaskView();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TASK_MODEL, model);
        taskView.setArguments(bundle);
        return taskView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTaskModel = (TaskModel) savedInstanceState.getSerializable(TASK_MODEL);
        } else {
            mTaskModel = (TaskModel) getArguments().getSerializable(TASK_MODEL);
        }
    }

    @Override
    protected void setListeners() {
        this.mTaskNameDependantText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    String seq = editable.toString();
                    mTaskModel.setTitle(seq);
                    editable.clear();
                    editable.append(mTaskModel.getTitle());
                } else {
                    editable.clear();
                    editable.append(mTaskModel.getTitle());
                }
            }
        });

        this.mAddDependantText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("Model", mApplicableDependants.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    String dependantText = editable.toString();
                    mAddDependantButton.setEnabled(mApplicableDependants.stream().anyMatch(taskModel -> taskModel.getTitle().equals(dependantText)));
                } else {
                   mAddDependantButton.setEnabled(false);
                }
            }
        });

        this.mAddDependantButton.setOnClickListener(view -> {
            String selectedDependantText = this.mAddDependantText.getText().toString();
            TaskModel selected = null;
            for (TaskModel dependant : mApplicableDependants) {
                if(dependant.getTitle().equals(selectedDependantText)) {
                   selected = dependant;
                   break;
                }
            }

            if(selected == null) return;

            mApplicableDependants.remove(selected);
            mDependants.add(selected);
            mAddDependantText.getText().clear();
        });

        this.mTaskDeleteButton.setOnClickListener(view -> {
            mTaskModel.removeTask();
            getFragmentManager().popBackStackImmediate();
        });
    }

    @Override
    protected void populate() {
        this.mApplicableDependants = this.mTaskModel.getApplicableDependants();
        mDependants = this.mTaskModel.getDependants();

        List<String> applicableDependantsStrings = mApplicableDependants.stream().map(TaskModel::getTitle).collect(Collectors.toList());

        mPresenter.loadTaskModels();

        this.mTaskNameDependantText.getText().clear();
        this.mTaskNameDependantText.getText().append(this.mTaskModel.getTitle());

        this.mDependantTasksList.setAdapter(new DependantTaskListAdapter(getActivity(), mDependants));

        this.mAddDependantText.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, applicableDependantsStrings));
        this.mAddDependantText.setValidator(new ApplicableDependantsValidator(applicableDependantsStrings));
    }

    @Override
    protected void init() {
    }

    @Override
    protected void setUi(View v) {
        mTaskDeleteButton = v.findViewById(R.id.task_delete);
        mDependantTasksList = v.findViewById(R.id.dependant_tasks_list);
        mAddDependantButton = v.findViewById(R.id.task_add_dependant_button);
        mAddDependantText = v.findViewById(R.id.task_add_dependant_text);
        mTaskNameDependantText = v.findViewById(R.id.task_name_text);
    }

    @Override
    protected TaskPresenter createPresenter() {
        return new TaskPresenter(this, mTaskModel);
    }

    @Override
    protected int layout() {
        return R.layout.task_layout;
    }



    private class DependantTaskListAdapter extends ArrayAdapter<TaskModel> implements ListAdapter {

        public DependantTaskListAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        public DependantTaskListAdapter(@NonNull Context context, @NonNull List<TaskModel> objects) {
            super(context, R.layout.dependant_task_list_row_layout, objects);
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View v;
            final FloatingActionButton taskDelete;
            final TextView taskTitle;

            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.dependant_task_list_row_layout, null);

                taskDelete = v.findViewById(R.id.list_dependant_task_delete);
                taskTitle = v.findViewById(R.id.list_dependant_task_title);
            } else {
                v = convertView;
                taskDelete = v.findViewById(R.id.list_dependant_task_delete);
                taskTitle = v.findViewById(R.id.list_dependant_task_title);
            }

            final TaskModel m = getItem(position);
            if (m != null) {

//            taskCheckbox.setOnCheckedChangeListener((compoundButton, b) -> { });

                taskTitle.setText(m.getTitle());
                taskDelete.setOnClickListener(view -> {
                    mTaskModel.removeDependant(m);
                    mDependants.remove(m);
                    mApplicableDependants.add(m);
                });

            }

            return v;
        }

    }


    private static class ApplicableDependantsValidator implements AutoCompleteTextView.Validator {
        private final List<String> applicableDependantsStrings;

        public ApplicableDependantsValidator(List<String> applicableDependantsStrings) {
            this.applicableDependantsStrings = applicableDependantsStrings;
        }

        @Override
        public boolean isValid(CharSequence charSequence) {
            return applicableDependantsStrings.contains(charSequence.toString());
        }

        @Override
        public CharSequence fixText(CharSequence charSequence) {
            if(applicableDependantsStrings.size() > 0) {
                return applicableDependantsStrings.get(0);
            }
            return "";
        }
    }
}
