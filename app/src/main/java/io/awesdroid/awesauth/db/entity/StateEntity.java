package io.awesdroid.awesauth.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import io.awesdroid.awesauth.model.AppAuthState;

/**
 * @auther Awesdroid
 */
@Entity(tableName = "state")
final public class StateEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private AppAuthState appAuthState;

    public StateEntity() {}

    @Ignore
    public StateEntity(int id, AppAuthState appAuthState) {
        this.id = id;
        this.appAuthState = appAuthState;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AppAuthState getAppAuthState() {
        return appAuthState;
    }

    public void setAppAuthState(AppAuthState appAuthState) {
        this.appAuthState = appAuthState;
    }
}
