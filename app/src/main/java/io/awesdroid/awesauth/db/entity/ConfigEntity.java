package io.awesdroid.awesauth.db.entity;

import com.google.gson.GsonBuilder;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import io.awesdroid.awesauth.model.AppAuthConfig;
import io.awesdroid.awesauth.model.UriAdapter;

/**
 * @auther Awesdroid
 */
@Entity(tableName = "config")
final public class ConfigEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private AppAuthConfig appAuthConfig;

    public ConfigEntity() {
    }

    @Ignore
    public ConfigEntity(int id, AppAuthConfig appAuthConfig) {
        this.id = id;
        this.appAuthConfig = appAuthConfig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AppAuthConfig getAppAuthConfig() {
        return appAuthConfig;
    }

    public void setAppAuthConfig(AppAuthConfig appAuthConfig) {
        this.appAuthConfig = appAuthConfig;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ConfigEntity)) {
            return false;
        }
        return obj.toString().equals(this.toString());
    }

    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create()
                .toJson(this);
    }
}
