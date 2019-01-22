package io.awesdroid.awesauth.db;

import com.google.gson.GsonBuilder;

import android.net.Uri;
import android.util.Log;

import net.openid.appauth.AuthState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.awesdroid.awesauth.db.dao.StateDao;
import io.awesdroid.awesauth.db.entity.StateEntity;
import io.awesdroid.awesauth.model.AppAuthState;
import io.awesdroid.awesauth.model.UriAdapter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @auther Awesdroid
 */
@RunWith(AndroidJUnit4.class)
public class StateDaoTest {
    private static final String TAG = "StateDaoTest";
    private AuthDatabase authDatabase;
    private StateDao stateDao;
    private AuthState expectedState = null;

    @Before
    public void init() {
        authDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                AuthDatabase.class)
                .build();
        stateDao = authDatabase.stateDao();
    }

    @After
    public void destroy() {
        authDatabase.close();
    }

    @Test
    public void getConfigWhenEmpty() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.supplyAsync(() -> stateDao.loadAppAuthState(1))
                .thenAccept(configEntity -> {
                    assertNotNull(configEntity);
                    latch.countDown();
                });

        latch.await(2, TimeUnit.SECONDS);
    }

    private Observable<AuthState> readState() {
        return Observable.defer(() -> {
            InputStream is = getClass().getResourceAsStream("state.json");
            if (is == null) {
                return Observable.empty();
            }
            return Observable.just(is);
        })
        .map(s -> new InputStreamReader(s, StandardCharsets.UTF_8))
        .map(isr -> new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create()
                .fromJson(isr, AuthState.class))
        .subscribeOn(Schedulers.io());
    }

    private Observable<Boolean> insertCofigIntoDb() {
        return readState().map(state -> {
            expectedState = state;
            Log.d(TAG, "insertCofigIntoDb(): expectedState = " + expectedState.jsonSerializeString());
            stateDao.insertAppAuthState(new StateEntity(1, new AppAuthState(state)));
            return true;
        });
    }

    @Test
    public void getStateFromDb() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
        insertCofigIntoDb().subscribe(v ->
                CompletableFuture.supplyAsync(() -> stateDao.loadAppAuthState(1))
                        .thenAccept(stateEntity -> {
                            assertEquals(1, stateEntity.getId());
                            assertEquals(expectedState.jsonSerializeString(),
                                    stateEntity.getAppAuthState().getAuthState().jsonSerializeString());
                            latch.countDown();
                        })
                        .exceptionally(e -> {
                            fail(e.getMessage());
                            return null;
                        })

        );
        latch.await(3, TimeUnit.SECONDS);
    }
}
