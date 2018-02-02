package uk.gopiandcode.directedtodo.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {
    public TaskDbHelper(Context context) {
        super(context, TaskContract.TASKS_DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTasksTable = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COL_TASK_DATE + " INTEGER, " +
                TaskContract.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL);";

        String createDependencyTable = "CREATE TABLE " + TaskContract.DependenciesEntry.TABLE + " ( " +
                TaskContract.DependenciesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK + " INTEGER, " +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS + " INTEGER," +
                "FOREIGN KEY(" +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK
                + ") REFERENCES " + TaskContract.TASKS_DB_NAME + "(" + TaskContract.TaskEntry._ID + "), " +
                "FOREIGN KEY(" +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                + ") REFERENCES " + TaskContract.TASKS_DB_NAME + "(" + TaskContract.TaskEntry._ID + "));";

        db.execSQL(createTasksTable);
        db.execSQL(createDependencyTable);
        db.execSQL("PRAGMA foreign_keys = ON;");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.DependenciesEntry.TABLE);
        onCreate(db);
    }
}
