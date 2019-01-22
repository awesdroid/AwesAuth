package io.awesdroid.awesauth.db.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.awesdroid.awesauth.db.entity.StateEntity;

/**
 * @auther Awesdroid
 */
@Dao
public interface StateDao {
    @Query("select * from state where id = :id")
    StateEntity loadAppAuthState(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAppAuthState(StateEntity stateEntity);

    @Query("select * from state")
    List<StateEntity> loadAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StateEntity> appAuthStateEntities);
}
