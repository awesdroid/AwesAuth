package io.awesdroid.awesauth.net;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import io.reactivex.Maybe;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author Awesdroid
 */
public class Http {
    private static final String TAG = Http.class.getSimpleName();

    @NonNull
    public static Maybe<String> getUserInfo(String url, String token) {
        Log.d(TAG, "getUserInfo: token = " + token + "\nurl=" + url);
        String baseUrl = getBaseUrl(url);

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

        Api api = retrofit.create(Api.class);
        return Maybe.create(emitter -> {
            api.getUerInfo(url, "Bearer ".concat(token))
            .subscribe(
                    emitter::onSuccess,
                    e -> emitter.onError(handleError(e)),
                    emitter::onComplete);
        });
    }

    private static String getBaseUrl(String url) throws IllegalArgumentException {
        Pattern pattern = Pattern
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            Log.d(TAG, "getBaseUrl(): " + matcher.group(0));
            return matcher.group(0);
        }
        throw new IllegalArgumentException("Can't get baseURL from: " + url);
    }

    private static Throwable handleError(Throwable e) {
        // TODO
        return e;
    }
}
