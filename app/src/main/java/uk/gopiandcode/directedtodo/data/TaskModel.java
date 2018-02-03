package uk.gopiandcode.directedtodo.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.gopiandcode.directedtodo.db.TaskContract;

public class TaskModel implements Serializable {
    private boolean deleted = false;
    private TaskListModel mListModel;
    private String mId;
    private Optional<Long> mDate;
    private String mTitle;

    public TaskModel(TaskListModel listModel, String id, Long date, String title) {
       this.mListModel = listModel;
       this.mId = id;
       this.mDate = Optional.of(date);
       this.mTitle = title;
   }

    public TaskModel(TaskListModel listModel, String id, String title) {
       this.mListModel = listModel;
       this.mId = id;
       this.mTitle = title;
       this.mDate = Optional.empty();
    }

    private void checkState() {
        if(deleted) {
            Log.d("Model", "Task " + this + " being manipulated after deletion");
            throw new RuntimeException("Task has been deleted, you shouldn't mess with it!");
        }
    }

    public String getId() {
        return mId;
    }

   public String getTitle() {
       checkState();
       return mTitle;
   }

    public Optional<Long> getDate() {
        checkState();
       return mDate;
   }

   public void setTitle(String newTitle) {
       checkState();
       SQLiteDatabase db = mListModel.getTasksDbHelper().getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(TaskContract.TaskEntry.COL_TASK_TITLE, newTitle);
       int update = db.update(TaskContract.TaskEntry.TABLE,
               values,
               TaskContract.TaskEntry._ID + " = ?",
               new String[]{
                       this.mId
               });
           if(update == 1) {
                this.mTitle = newTitle;
           }
           db.close();
   }

   public boolean setDate(Long newDate) {
       checkState();

       boolean seen = false;
       Long best = null;
       for (TaskModel taskModel : getDependants()) {
           Optional<Long> date = taskModel.getDate();
           if (date.isPresent()) {
               Long aLong = date.get();
               if (!seen || Long.compare(aLong, best) > 0) {
                   seen = true;
                   best = aLong;
               }
           }
       }
       Optional<Long> latestDependant = seen ? Optional.of(best) : Optional.empty();
       if(latestDependant.isPresent()){
           if(latestDependant.get() > newDate)
               return false;
       }

       SQLiteDatabase db = mListModel.getTasksDbHelper().getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(TaskContract.TaskEntry.COL_TASK_DATE, newDate);
       int update = db.update(TaskContract.TaskEntry.TABLE,
               values,
               TaskContract.TaskEntry._ID + " = ?",
               new String[]{
                       this.mId
               });
           if(update == 1) {
                this.mDate = Optional.of(newDate);
           }
           db.close();
           return true;
       }

    public boolean addDependant(TaskModel other) {
        checkState();
       return mListModel.registerDependency(this, other);
    }

    public boolean removeDependant(TaskModel other) {
        checkState();
       return mListModel.deregisterDependancy(this, other);
    }

    public List<TaskModel> getDependants() {
        checkState();
       return mListModel.retrieveDependancies(this);
    }

    public List<TaskModel> getApplicableDependants() {
        Set<TaskModel> dependants = new HashSet<>( this.getDependants());
        if(this.mDate.isPresent())    {
            List<TaskModel> list = new ArrayList<>();
            for (TaskModel taskModel : this.mListModel.getTasks()) {
                if (!taskModel.equals(this)) {
                    if (!taskModel.mDate.isPresent() || mDate.get() > taskModel.mDate.get()) {
                        if (!dependants.contains(taskModel)) {
                            if (!mListModel.hasCircularDependancyBetweenTasks(taskModel, this)) {
                                list.add(taskModel);
                            }
                        }
                    }
                }
            }
            return list;
        } else {

            List<TaskModel> list = new ArrayList<>();
            for (TaskModel taskModel : this.mListModel.getTasks()) {
                if (!taskModel.equals(this)) {
                    if (!dependants.contains(taskModel)) {
                        if (!mListModel.hasCircularDependancyBetweenTasks(taskModel, this)) {
                            list.add(taskModel);
                        }
                    }
                }
            }
            return list;
        }
    }

    public void removeTask() {
        checkState();
      if(mListModel.removeTask(this)) {
          this.deleted = true;
      }
    }

    @Override
    public String toString() {
        return "TaskModel{" +
                "deleted=" + deleted +
                ", mListModel=" + mListModel +
                ", mId='" + mId + '\'' +
                ", mDate=" + mDate +
                ", mTitle='" + mTitle + '\'' +
                '}';
    }
}
