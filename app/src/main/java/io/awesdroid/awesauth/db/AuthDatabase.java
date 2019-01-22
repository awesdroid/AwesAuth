package io.awesdroid.awesauth.db;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import io.awesdroid.awesauth.db.converter.ConfigConverter;
import io.awesdroid.awesauth.db.converter.StateConverter;
import io.awesdroid.awesauth.db.dao.ConfigDao;
import io.awesdroid.awesauth.db.dao.StateDao;
import io.awesdroid.awesauth.db.entity.ConfigEntity;
import io.awesdroid.awesauth.db.entity.StateEntity;
import io.awesdroid.awesauth.utils.ActivityHelper;

/**
 * @author Awesdroid
 */
@Database(entities = {StateEntity.class, ConfigEntity.class}, version = 1)
@TypeConverters({StateConverter.class, ConfigConverter.class})
public abstract class AuthDatabase extends RoomDatabase {
    private static final String TAG = AuthDatabase.class.getSimpleName();
    private static final String DB_NAME = "appauth.db";

    public abstract StateDao stateDao();
    public abstract ConfigDao configDao();

    private static AuthDatabase instance;

    public static AuthDatabase getInstance() {
        if (instance == null) {
            synchronized (AuthDatabase.class) {
                if (instance == null) {
                    instance = buildDatabase(ActivityHelper.getContext());
                }
            }
        }
        return instance;
    }

    private static AuthDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context, AuthDatabase.class, DB_NAME).build();
    }

    public void destroy() {
        Log.d(TAG, "destroy(): instance.isOpen() = " +
                (instance == null? "null" : instance.isOpen()));
        if (instance != null && instance.isOpen()) {
            instance.close();
        }
        instance = null;
    }


}
