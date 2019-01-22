package io.awesdroid.awesauth.db;

import com.google.gson.GsonBuilder;

import android.net.Uri;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.db.dao.ConfigDao;
import io.awesdroid.awesauth.db.entity.ConfigEntity;
import io.awesdroid.awesauth.model.AppAuthConfig;
import io.awesdroid.awesauth.model.UriAdapter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @auther Awesdroid
 */
@RunWith(AndroidJUnit4.class)
public class ConfigDaoTest {
    private static final String TAG = "ConfigDaoTest";
    private AuthDatabase authDatabase;
    private ConfigDao configDao;
    private ConfigEntity expectedConfigEntity;

    @Before
    public void init() {
        authDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                AuthDatabase.class)
                .build();
        configDao = authDatabase.configDao();
    }

    @After
    public void destroy() {
        authDatabase.close();
    }

    @Test
    public void getConfigWhenEmpty() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.supplyAsync(() -> configDao.loadConfig(1))
                .thenAccept(configEntity -> {
                    assertNotNull(configEntity);
                    latch.countDown();
                });
        latch.await(2, TimeUnit.SECONDS);
    }

    private Observable<AppAuthConfig> readConfig() {
        return Observable.just(R.raw.google_config)
                .map(id -> ApplicationProvider.getApplicationContext().getResources().openRawResource(id))
                .map(s -> new InputStreamReader(s, StandardCharsets.UTF_8))
                .map(isr -> new GsonBuilder()
                        .registerTypeAdapter(Uri.class, new UriAdapter())
                        .create()
                        .fromJson(isr, AppAuthConfig.class))
                .subscribeOn(Schedulers.io());
    }

    private Observable<Boolean> insertCofigIntoDb() {
        return readConfig().map(config -> {
            expectedConfigEntity = new ConfigEntity(1, config);
            configDao.insertConfig(expectedConfigEntity);
            return true;
        });
    }

    @Test
    public void getConfigFromDb() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
        insertCofigIntoDb().subscribe(v ->
                CompletableFuture.supplyAsync(() -> configDao.loadConfig(1))
                        .thenAccept(configEntity -> {
                            Log.d(TAG, "getStateFromDb(): configEntity = " + configEntity);
                            assertEquals(expectedConfigEntity, configEntity);
                            latch.countDown();
                        }));
        latch.await(3, TimeUnit.SECONDS);
    }
}
