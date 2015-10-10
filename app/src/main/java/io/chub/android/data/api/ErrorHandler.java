package io.chub.android.data.api;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

import io.chub.android.R;
import retrofit.HttpException;

/**
* Created by guillaume on 12/28/14.
*/
public class ErrorHandler {

    public static void showError(Context context, Throwable throwable) {
        int error;
        if (throwable instanceof HttpException) {
            error = R.string.http_error;
        } else if (throwable instanceof IOException) {
            error = R.string.network_error;
        } else {
            error = R.string.unexpected_error;
        }
        Toast.makeText(context, context.getString(error),
                Toast.LENGTH_SHORT).show();
    }
}
