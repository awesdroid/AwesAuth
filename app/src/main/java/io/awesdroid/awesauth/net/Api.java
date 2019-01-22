package io.awesdroid.awesauth.net;


import io.reactivex.Maybe;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

/**
 * @author Awesdroid
 */
public interface Api {
    @GET
    Maybe<String> getUerInfo(@Url String url, @Header("Authorization") String authorization);

}
