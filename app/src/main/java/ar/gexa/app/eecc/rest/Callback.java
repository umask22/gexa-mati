package ar.gexa.app.eecc.rest;

import android.content.Context;
import android.util.Log;

import ar.gexa.app.eecc.services.FCMMessageService;
import ar.gexa.app.eecc.services.NotificationService;
import ar.gexa.app.eecc.widget.AlertWidget;
import retrofit2.Call;
import retrofit2.Response;

public abstract class Callback<E> implements retrofit2.Callback<E> {

    private Context context;

    public Callback(Context context) {
        this.context = context;
    }

    public abstract void onSuccess(E e);
    public void onError(){}

    @Override
    public void onResponse(Call<E> call, Response<E> response) {

        if(response.isSuccessful())
            onSuccess(response.body());
        else if(response.code() == 404)
            onNotFound(response);
        else
            onInternalServerError(response);
    }

    public void onNotFound(Response<E> response) {
        AlertWidget.create().showError(this.context, "¡Ups! 404", "Se ha producido un error al conectar con el servidor[" + response.message() + "]");
        Log.i(Callback.class.getSimpleName(), response.message());
        onError();
    }

    public void onInternalServerError(Response<E> response) {
        NotificationService.getInstance().txtLog(Callback.class.getSimpleName() + response.body() +"///"+ response.message()+"///"+response.errorBody());
//        AlertWidget.create().showError(this.context, "¡Ups! 500", "Se ha producido un error en el servidor[" + response.message() + "]");
        Log.e(Callback.class.getSimpleName(), response.message());
        onError();
    }

    @Override
    public void onFailure(Call call, Throwable t) {
        AlertWidget.create().showError(this.context, t);
        Log.e(Callback.class.getSimpleName(), t.getMessage());
        onError();
    }
}
