package uk.gopiandcode.directedtodo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskListModel {
    private TaskDbHelper mHelper;
    private List<TaskModel> tasks;
    private HashMap<String, Set<String>> dependancyMap;
    private HashMap<String, TaskModel> idMap;

    public TaskDbHelper getDbHelper() {
       return mHelper;
    }

    public TaskListModel(Context context) {
        mHelper = new TaskDbHelper(context);
        tasks = new ArrayList<>();
        dependancyMap = new HashMap<>();
        idMap = new HashMap<>();
        loadTasks();
        loadDependancies();
    }

    private void loadTasks() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{
                        TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COL_TASK_DATE,
                        TaskContract.TaskEntry.COL_TASK_TITLE
                },
                null, null, null, null, null);
        int titleIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
        int dateIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DATE);
        int idIndex = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
        while (cursor.moveToNext()) {
            String title = cursor.getString(titleIndex);
            String dateString = cursor.getString(dateIndex);
            String id = cursor.getString(idIndex);

            try {
               Long date = Long.parseLong(dateString);
                TaskModel taskModel = new TaskModel(this, id, date, title);
                idMap.put(id, taskModel);
                tasks.add(taskModel);
            } catch(NumberFormatException exception) {
               Log.d("Model", "" + exception);
            }
        }
        cursor.close();
        db.close();
    }

    private void loadDependancies() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.DependenciesEntry.TABLE,
                new String[] {
                    TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK,
                    TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                },
                null, null, null, null, null);
        int taskIndex = cursor.getColumnIndex(TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK);
        int dependantsIndex = cursor.getColumnIndex(TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS);

        while(cursor.moveToNext()) {
            String taskId = cursor.getString(taskIndex);
            String dependanciesId = cursor.getString(dependantsIndex);

            Set<String> result = dependancyMap.get(taskId);
            if(result == null) {
                result = new HashSet<>();
                dependancyMap.put(taskId, result);
            }

            result.add(dependanciesId);
        }

        cursor.close();
        db.close();
    }

    public boolean registerDependancy(TaskModel taskModel, TaskModel other) {
        String taskModelId = taskModel.getId();
        String otherId = other.getId();

        if (hasCircularDependancyBetweenTasks(other, taskModel))
            return false;

        if(!dependancyMap.get(taskModelId).contains(otherId)) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK, taskModelId);
            values.put(TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS, otherId);
            db.insert(TaskContract.DependenciesEntry.TABLE, null, values);
            db.close();
            dependancyMap.get(taskModelId).add(otherId);
            return true;
        } else {
            return false;
        }
    }

    public boolean hasCircularDependancyBetweenTasks(TaskModel other, TaskModel taskModel) {
        // O(nodes) dfs search to check for path
        return false;
    }

    public boolean deregisterDependancy(TaskModel taskModel, TaskModel other) {
        String taskModelId = taskModel.getId();
        String otherId = other.getId();


        if(dependancyMap.get(taskModelId).contains(otherId)) {
            SQLiteDatabase db = mHelper.getWritableDatabase();

            if(db.delete( TaskContract.DependenciesEntry.TABLE,
             TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK + " = ? AND " + TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS + " = ?",
                    new String[] {taskModelId, otherId }) == 1) {
                db.close();
                dependancyMap.get(taskModelId).remove(otherId);
               return true;
            } else {
                db.close();
               return false;
            }
        } else {
            return false;
        }

    }

    public List<TaskModel> retrieveDependancies(TaskModel taskModel) {
        List<TaskModel> result = new ArrayList<>();
        for(String id : dependancyMap.get(taskModel.getId())) {
           result.add(idMap.get(id));
        }
        return result;
    }

    public boolean removeTask(TaskModel taskModel) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String taskModelId = taskModel.getId();
        if(db.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry._ID + " = ?", new String[]{taskModelId}) == 1) {
            for(String id : dependancyMap.get(taskModelId)) {
               db.delete(
                       TaskContract.DependenciesEntry.TABLE,
             TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK
                     + " = ? AND " +
                     TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                       + " = ?", new String[] {taskModelId, id });
            }
            dependancyMap.remove(taskModelId);
            tasks.remove(taskModel);
            db.close();
            return true;
        } else {
            db.close();
           return false;
        }
    }
}

