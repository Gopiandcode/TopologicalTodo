package uk.gopiandcode.directedtodo.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import uk.gopiandcode.directedtodo.db.TaskContract;
import uk.gopiandcode.directedtodo.db.TaskDbHelper;

public class TaskListModel {
    private TaskDbHelper mTaskHelper;
    private List<TaskModel> tasks;
    private HashMap<String, Set<String>> dependancyMap;
    private HashMap<String, TaskModel> idMap;

    public TaskDbHelper getTasksDbHelper() {
        return mTaskHelper;
    }

    public void addNewTask(String title) {
        SQLiteDatabase db = mTaskHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskContract.TaskEntry.COL_TASK_TITLE, title);
        Long rowID = db.insert(TaskContract.TaskEntry.TABLE, null, contentValues);
        db.close();

        String id = "" + rowID;
        TaskModel model = new TaskModel(this, id, title);
        idMap.put(id, model);
        tasks.add(model);
        dependancyMap.put(id, new HashSet<>());
    }

    public TaskListModel(Context context) {
        mTaskHelper = new TaskDbHelper(context);
        tasks = new ArrayList<>();
        dependancyMap = new HashMap<>();
        idMap = new HashMap<>();
        loadTasks();
        loadDependencies();
    }

    private void loadTasks() {
        SQLiteDatabase db = mTaskHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{
                        TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COL_TASK_DATE,
                        TaskContract.TaskEntry.COL_TASK_RANKING,
                        TaskContract.TaskEntry.COL_TASK_TITLE
                },
                null, null, null, null, null);
          int titleIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
        int dateIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DATE);
        int rankingIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_RANKING);
        int idIndex = cursor.getColumnIndex(TaskContract.TaskEntry._ID);

       while (cursor.moveToNext()) {

            String title = cursor.getString(titleIndex);
            String dateString = cursor.getString(dateIndex);
            String id = cursor.getString(idIndex);
            TaskModel taskModel;

            if (dateString != null) {
                try {
                    Long date = Long.parseLong(dateString);
                    taskModel = new TaskModel(this, id, date, title);
                    idMap.put(id, taskModel);
                    tasks.add(taskModel);
                    dependancyMap.put(id, new HashSet<>());
                } catch (NumberFormatException exception) {
                    Log.d("Model", "" + exception);
                    continue;
                }
            } else {
                taskModel = new TaskModel(this, id, title);
                idMap.put(id, taskModel);
                tasks.add(taskModel);
                dependancyMap.put(id, new HashSet<>());
            }
            String ranking = cursor.getString(rankingIndex);
            if (ranking != null) {
                try {
                    int anInt = Integer.parseInt(ranking);
                    taskModel.ranking = anInt;
                } catch (NumberFormatException e) {
                    Log.d("Model", "" + e);
                }
            }

        }
        cursor.close();
        db.close();
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    private void loadDependencies() {
        SQLiteDatabase db = mTaskHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.DependenciesEntry.TABLE,
                new String[]{
                        TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK,
                        TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                },
                null, null, null, null, null);
        int taskIndex = cursor.getColumnIndex(TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK);
        int dependantsIndex = cursor.getColumnIndex(TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS);

        while (cursor.moveToNext()) {
            String taskId = cursor.getString(taskIndex);
            String dependanciesId = cursor.getString(dependantsIndex);

            Set<String> result = dependancyMap.get(taskId);
            if (result == null) {
                result = new HashSet<>();
                dependancyMap.put(taskId, result);
            }

            result.add(dependanciesId);
        }

        cursor.close();
        db.close();
    }

    public boolean registerDependency(TaskModel taskModel, TaskModel other) {
        String taskModelId = taskModel.getId();
        String otherId = other.getId();

        if (hasCircularDependancyBetweenTasks(other, taskModel))
            return false;
        Optional<Long> taskDate = taskModel.getDate();
        Optional<Long> otherDate = taskModel.getDate();
        if (taskDate.isPresent() && otherDate.isPresent()) {
            if (taskDate.get() < otherDate.get())
                return false;
        }

        if (!dependancyMap.get(taskModelId).contains(otherId)) {

            SQLiteDatabase db = mTaskHelper.getWritableDatabase();
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

    public boolean hasCircularDependancyBetweenTasks(TaskModel rootTask, TaskModel targetTask) {
        // O(nodes) bfs search to check for path
        Queue<String> dfsStack = new LinkedList<>();
        boolean hasSeenTarget = false;

        dfsStack.add(rootTask.getId());

        while (!dfsStack.isEmpty() && !hasSeenTarget) {
            String id = dfsStack.poll();
            Set<String> connections = dependancyMap.get(id);
            if (connections.contains(targetTask.getId())) {
                hasSeenTarget = true;
            } else {
                dfsStack.addAll(connections);
            }
        }

        return hasSeenTarget;
    }

    public boolean deregisterDependancy(TaskModel taskModel, TaskModel other) {
        String taskModelId = taskModel.getId();
        String otherId = other.getId();


        if (dependancyMap.get(taskModelId).contains(otherId)) {
            SQLiteDatabase db = mTaskHelper.getWritableDatabase();

            if (db.delete(TaskContract.DependenciesEntry.TABLE,
                    TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK + " = ? AND " + TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS + " = ?",
                    new String[]{taskModelId, otherId}) == 1) {
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
        for (String id : dependancyMap.get(taskModel.getId())) {
            result.add(idMap.get(id));
        }
        return result;
    }

    public boolean removeTask(TaskModel taskModel) {

        for (TaskModel task : tasks) {
            task.removeDependant(taskModel);
        }

        SQLiteDatabase taskDb = mTaskHelper.getWritableDatabase();
        String taskModelId = taskModel.getId();

        if (taskDb.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry._ID + " = ?", new String[]{taskModelId}) == 1) {

            for (String id : dependancyMap.get(taskModelId)) {
                taskDb.delete(
                        TaskContract.DependenciesEntry.TABLE,
                        TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK
                                + " = ? AND " +
                                TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                                + " = ?", new String[]{taskModelId, id});
            }


            dependancyMap.remove(taskModelId);
            tasks.remove(taskModel);
            taskDb.close();
            return true;
        } else {
            taskDb.close();
            return false;
        }
    }
}

