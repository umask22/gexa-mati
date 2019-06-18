package ar.gexa.app.eecc.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseManager {

    private static volatile DatabaseManager instance;

    private DatabaseManager() {
        if (instance != null)
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null)
                    instance = new DatabaseManager();
            }
        }
        return instance;
    }

    private Database db;

    public void init(Context context) {
        db = Room.databaseBuilder(context,Database.class, "gexa").build();
    }

    public void onDestroy() {
        if(db.isOpen())
            db.close();
        db = null;
    }

    public Database getDB() {

        if (db == null)
            throw new RuntimeException("Use init(context) method to initializate database.");

        return db;
    }


}
