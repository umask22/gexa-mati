package ar.gexa.app.eecc.rest;

import java.io.File;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.utils.HTTPUtils;
import common.models.resources.AccountResource;
import common.models.resources.ActionResource;
import common.models.resources.ContactResource;
import common.models.resources.UserResource;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class GexaClient {

    public static final String SERVER_IP_ADDRESS = "gexa.com.ar";
    public static final String SERVER_PORT_ADDRESS = "";

//    public static final String SERVER_IP_ADDRESS = "192.168.1.113";
//    public static final String SERVER_PORT_ADDRESS = ":9000";

    private static volatile GexaClient instance;

    private Retrofit retrofit;

    private Call<?> call;


    private GexaClient() {}

    public static GexaClient getInstance() {
        if (instance == null) {
            synchronized (GexaClient.class) {
                if (instance == null)
                    instance = new GexaClient();
            }
        }
        return instance;
    }

    public void init() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + SERVER_IP_ADDRESS + SERVER_PORT_ADDRESS)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .readTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .connectTimeout(5, TimeUnit.MINUTES)
                        .build())
                .build();
    }

    public void cancel() {
        if(call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
    }

    public GexaAPI getAPI() {
        if (retrofit == null)
            throw new RuntimeException("Use init() method to initializate Retrofit.");
        return retrofit.create(GexaAPI.class);
    }

    public void sendUserLocation(String latlng, final Callback<String> callback) {
        final Call<String> call = GexaClient.getInstance().getAPI().sendUserLocation(latlng);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void findUserById(String code, final Callback<UserResource> callback) {
        final Call<UserResource> call = getAPI().findUserByCode(code);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void authenticate(final Callback<User> callback) {
        final Call<User> call = getAPI().authenticate(User.toResource(UserRepository.getInstance().find()));
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void synchronizationRequest(String username, final Callback<Void> callback) {
        final Call<Void> call = getAPI().synchronizationRequest(username);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onVisitSave(ActionResource resource, final Callback<ResponseBody> callback) {
        final Call<ResponseBody> call = getAPI().onVisitSave(resource);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onVisitUpdate(ActionResource resource, final Callback<ResponseBody> callback) {
        final Call<ResponseBody> call = getAPI().onVisitUpdate(resource);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onCallSave(File file, final Callback<ResponseBody> callback) {
        final MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(null,file));
        final Call<ResponseBody> call = getAPI().onCallSave(body);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onCallUpdate(File file, final Callback<ResponseBody> callback) {
        final MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(null,file));
        final Call<ResponseBody> call = getAPI().onCallUpdate(body);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onMissedCall(ActionResource resource, final Callback<ResponseBody> callback) {
        final Call<ResponseBody> call = getAPI().onMissedCall(resource);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onSynchronizeActivityByCode(String code, final Callback<ActionResource> callback) {
        Call<ActionResource> call = getAPI().findActivityByCode(code);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onSynchronizeAccountByCUIT(String cuit, Callback<AccountResource> callback) {
        Call<AccountResource> call = getAPI().findAccountByCUIT(cuit);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onContactSave(Long id, final Callback<ContactResource> callback){
        final Call<ContactResource> call = getAPI().findContactById(id);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void onContactUpdate (Long id, final Callback<ContactResource> callback){
        final Call<ContactResource> call = getAPI().findContactById(id);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }


    public void onMoveActivityToTomorrowApp(String code, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = getAPI().onMoveActivityToTomorrowApp(code);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void findActionByCode(String code, Callback<ActionResource> callback) {
        final Call<ActionResource> call = getAPI().findActionByCode(code);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }

    public void findAddressPrincipalByAccountCuit(String cuit, Callback<AccountResource> callback) {
        final Call<AccountResource> call = getAPI().findAddressPrincipalByAccountCuit(cuit);
        if(HTTPUtils.ping(GexaClient.SERVER_IP_ADDRESS))
            call.enqueue(callback);
        else
            callback.onFailure(call, new ConnectException("unknown host " + GexaClient.SERVER_IP_ADDRESS));
    }


    //    public void saveActivity(ActionResource resource, final Callback<ResponseBody> callback) {
//        final Call<ResponseBody> call = GexaClient.get().getAPI().saveActivity(resource);
//        call.enqueue(callback);
//    }
//
}
