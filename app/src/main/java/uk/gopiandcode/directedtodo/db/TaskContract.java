package uk.gopiandcode.directedtodo.db;


import android.provider.BaseColumns;

public class TaskContract {

    public static final String TASKS_DB_NAME = "ul_gopiandcode_directedtodo_db_tasks";
    public static final String DEPENDENCIES_DB_NAME = "ul_gopiandcode_directedtodo_db_tasks";
    public static final int DB_VERSION = 5;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";
        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_DATE = "date";
        public static final String COL_TASK_RANKING = "ranking";
    }

    public class DependenciesEntry implements BaseColumns {
        public static final String TABLE = "dependencies";
        public static final String COL_DEPENDENCIES_TASK = "task";
        public static final String COL_DEPENDENCIES_DEPENDANTS = "dependants";
    }
}
