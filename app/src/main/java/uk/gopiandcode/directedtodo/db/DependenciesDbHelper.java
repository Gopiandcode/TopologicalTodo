package uk.gopiandcode.directedtodo.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DependenciesDbHelper extends SQLiteOpenHelper {
    public DependenciesDbHelper(Context context) {
        super(context, TaskContract.DEPENDENCIES_DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TaskContract.DependenciesEntry.TABLE + " ( " +
                TaskContract.DependenciesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK + " INTEGER, " +
                TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS + " INTEGER," +
                "FOREIGN KEY(" +
                    TaskContract.DependenciesEntry.COL_DEPENDENCIES_TASK
                + ") REFERENCES " + TaskContract.TASKS_DB_NAME + "(" + TaskContract.TaskEntry._ID + "), " +
                "FOREIGN KEY(" +
                    TaskContract.DependenciesEntry.COL_DEPENDENCIES_DEPENDANTS
                + ") REFERENCES " + TaskContract.TASKS_DB_NAME + "(" + TaskContract.TaskEntry._ID + "));";
        db.execSQL(createTable);
        db.execSQL("PRAGMA foreign_keys = ON;");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Implement migrations for old entries
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.DependenciesEntry.TABLE);
        onCreate(db);
    }
}
