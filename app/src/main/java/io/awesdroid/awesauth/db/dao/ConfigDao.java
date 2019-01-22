package io.awesdroid.awesauth.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.awesdroid.awesauth.db.entity.ConfigEntity;

/**
 * @auther Awesdroid
 */
@Dao
public interface ConfigDao {
    @Query("select * from config where id = :id")
    ConfigEntity loadConfig(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConfig(ConfigEntity config);
}
