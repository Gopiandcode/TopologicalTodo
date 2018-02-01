package uk.gopiandcode.directedtodo.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

public class TaskModel {
    private boolean deleted = false;
    private TaskListModel mListModel;
    private String mId;
    private Long mDate;
    private String mTitle;

    public TaskModel(TaskListModel listModel, String id, Long date, String title) {
       this.mListModel = listModel;
       this.mId = id;
       this.mDate = date;
       this.mTitle = title;
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

    public Long getDate() {
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

   public void setDate(Long newDate) {
       checkState();
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
                this.mDate = newDate;
           }
           db.close();
       }

    public boolean addDependant(TaskModel other) {
        checkState();
       return mListModel.registerDependancy(this, other);
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
