package com.lithiumsheep.stratechery.api;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

@Keep
public abstract class NetworkCallback<T> implements Callback<T> {

    protected abstract void onSuccess(T response);

    protected abstract void onError(Error error);

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
            this.onError(convertErrorBody(response.errorBody()));
        } else {
            this.onSuccess(response.body());
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (t instanceof UnknownHostException) {    // this exception usually means no network
            onError(new Error(new Throwable("No network available, please check your WiFi or Data connection.")));
        } else {
            onError(new Error(t));
        }
    }

    /**
     * Convert the error (status code not in 200-299 range) to {@link Error} class for easier parsing
     * <p>
     * Concept borrowed from https://stackoverflow.com/questions/35029936/centralized-error-handling-retrofit-2/35031601#35031601
     * which details how to use the abstracted {@link Converter} instead of the parsing lib directly
     *
     * @param responseErrorBody {@link ResponseBody} from {@link Response#errorBody()}
     */
    private Error convertErrorBody(ResponseBody responseErrorBody) {
        Converter<ResponseBody, Error> converter =
                HttpClient.getRetrofit().responseBodyConverter(Error.class, new Annotation[0]);
        try {
            return converter.convert(responseErrorBody);
        } catch (IOException e) {
            return new Error(e);
        }
    }

    public static class Error {
        transient Throwable throwable;

        private String message;

        Error(Throwable throwable) {
            this.throwable = throwable;
            this.message = throwable.getMessage();
        }

        public String getMessage() {
            return this.message;
        }
    }
}
