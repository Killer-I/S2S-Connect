package killer_i.s2s.s2sconnect.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class Logger {

    private enum LogType {
        INFO(0),
        WARNING(1),
        ERROR(2);

        private int value;

        LogType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private Context c;
    private SQLiteDatabase dbRef;

    public Logger(Context c) {
        this.c = c;
        DBHandler dbHandler = new DBHandler(c);
        this.dbRef = dbHandler.getWritableDatabase();
    }

    public long info(String title, String log) {
        return insertLog(LogType.INFO, title, log);
    }

    public long warning(String title, String log) {
        return insertLog(LogType.WARNING, title, log);
    }

    public long error(String title, String log) {
        return insertLog(LogType.ERROR, title, log);
    }

    private long insertLog(LogType ltype, String title, String log) {
        ContentValues cv = new ContentValues();
        cv.put(DBHandler.C2, ltype.getValue());
        cv.put(DBHandler.C3, title.substring(0, Math.min(title.length(), 100)));
        cv.put(DBHandler.C4, log.trim());
        return dbRef.insert(DBHandler.LOG_TABLE, null, cv);
    }

    public Cursor getLogs() {
        return dbRef.query(
                DBHandler.LOG_TABLE,
                null,
                null,
                null,
                null,
                null,
                null,
                "20"
        );
    }

    public String getLogsAsText() {
        Cursor cursor = getLogs();
        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()) {
            builder.append(getStringFromCursor(cursor));
            builder.append("\n");
        }
        cursor.close();
        return builder.toString();
    }

    private String getStringFromCursor(Cursor c) {
        return c.getString(4) +
                "-" + c.getInt(1) +
                "-" + c.getString(2) +
                "-" + c.getString(3);
    }

    private String getLogType(int lt) {
        if (lt == LogType.INFO.getValue())
            return "INFO";
        if (lt == LogType.WARNING.getValue())
            return "WARNING";
        if (lt == LogType.ERROR.getValue())
            return "ERROR";
        return "--NONE--";
    }

    public void clearAllLogs() {
        dbRef.delete(DBHandler.LOG_TABLE, null, null);
    }

    private static class DBHandler extends SQLiteOpenHelper {

        private Context c;
        private static final String DB_NAME = "logger_db";
        private static final int DB_VERSION = 1 ;

        private static final String LOG_TABLE = "logs";

        private static final String C1 = "id";
        private static final String C2 = "ltype";
        private static final String C3 = "title";
        private static final String C4 = "linfo";
        private static final String C5 = "logtime";

        private static final String CREATE_LOG_TABLE = "CREATE TABLE " + LOG_TABLE + "(" +
                C1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C2 + " INTEGER NOT NULL," +
                C3 + " VARCHAR(100)," +
                C4 + " TEXT," +
                C5 + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";


        public DBHandler(@Nullable Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.c =context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_LOG_TABLE);
            } catch (Exception e) {
                Toast.makeText(this.c, "DB initialization failed", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
