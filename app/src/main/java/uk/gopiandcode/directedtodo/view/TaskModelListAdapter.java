package uk.gopiandcode.directedtodo.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
        final View v;
        CheckBox taskCheckbox;
        TextView taskTextview;

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.task_list_row_layout, null);
            taskCheckbox = v.findViewById(R.id.task_complete);
            taskTextview = v.findViewById(R.id.task_title);
        } else {
           v = convertView;
            taskCheckbox = v.findViewById(R.id.task_complete);
            taskTextview = v.findViewById(R.id.task_title);
            taskCheckbox.setEnabled(true);
            taskCheckbox.setChecked(false);
            taskCheckbox.clearAnimation();
        }

        final TaskModel m = getItem(position);
        if (m != null) {

            taskCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    final Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.remove_list_item);
                    v.startAnimation(animation);
                    Handler handle = new Handler();
                    taskCheckbox.setEnabled(false);
                    handle.postDelayed(() -> {
                        listener.onTaskComplete(m);
                        notifyDataSetChanged();
                        animation.cancel();
                        taskCheckbox.setChecked(false);
                        taskCheckbox.setEnabled(true);
                    }, 100);
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
