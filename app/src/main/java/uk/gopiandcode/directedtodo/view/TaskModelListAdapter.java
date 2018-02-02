package uk.gopiandcode.directedtodo.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import uk.gopiandcode.directedtodo.R;
import uk.gopiandcode.directedtodo.data.TaskModel;

interface OnTaskCompleteListener {
    void onTaskComplete(TaskModel taskModel);

    void onTaskSelected(TaskModel taskModel);
}


public class TaskModelListAdapter extends ArrayAdapter<TaskModel> implements ListAdapter {
    OnTaskCompleteListener listener;

    public TaskModelListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public TaskModelListAdapter(@NonNull Context context, @NonNull List<TaskModel> objects, @NonNull OnTaskCompleteListener onComplete) {
        super(context, R.layout.task_list_row_layout, objects);
        listener = onComplete;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.task_list_row_layout, null);
        }

        final TaskModel m = getItem(position);
        if (m != null) {
            CheckBox taskCheckbox = v.findViewById(R.id.task_complete);
            TextView taskTextview = v.findViewById(R.id.task_title);

            taskCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    listener.onTaskComplete(m);
                    notifyDataSetChanged();
                }
            });


            v.setOnClickListener(view -> {
                listener.onTaskSelected(m);
            });

            if (m.getDate().isPresent()) {
                taskTextview.setText(
                        m.getTitle() +
                                Instant.ofEpochMilli(m.getDate().get()).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else {
                taskTextview.setText(m.getTitle());
            }
        }

        return v;
    }
}
