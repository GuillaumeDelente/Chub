package io.chub.android.data.api;

import android.content.Context;
import android.widget.Toast;

import io.chub.android.R;
import retrofit.RetrofitError;
import rx.functions.Action1;

/**
* Created by guillaume on 12/28/14.
*/
public class ErrorAction implements Action1<Throwable> {
    private Context mContext;

    public ErrorAction(Context context) {
        mContext = context;
    }

    @Override
    public void call(Throwable throwable) {
        if (throwable instanceof RetrofitError) {
            int error;
            switch (((RetrofitError) throwable).getKind()) {
                case NETWORK:
                    error = R.string.network_error;
                    break;
                case HTTP:
                    error = R.string.http_error;
                    break;
                case CONVERSION:
                    error = R.string.conversion_error;
                    break;
                case UNEXPECTED:
                default:
                    error = R.string.unexpected_error;
                    break;
            }
            Toast.makeText(mContext, mContext.getString(error),
                    Toast.LENGTH_SHORT).show();
        } else {
            throwable.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.unknown_error),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
