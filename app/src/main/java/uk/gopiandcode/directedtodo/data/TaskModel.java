package uk.gopiandcode.directedtodo.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.Optional;

import uk.gopiandcode.directedtodo.db.TaskContract;

public class TaskModel {
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

       Optional<Long> latestDependant = getDependants().stream().map((taskModel) -> taskModel.getDate()).filter(Optional::isPresent).map(Optional::get).max(Long::compare);
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
