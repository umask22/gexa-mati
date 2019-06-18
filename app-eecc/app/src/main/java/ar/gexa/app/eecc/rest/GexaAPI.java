
package ar.gexa.app.eecc.rest;

import ar.gexa.app.eecc.models.User;
import common.models.dto.DeviceSynchronizeDTO;
import common.models.resources.AccountResource;
import common.models.resources.ActionResource;
import common.models.resources.ContactResource;
import common.models.resources.UserResource;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface GexaAPI {

    @GET("/app/user/{code}")
    Call<UserResource> findUserByCode(@Path("code") String code);

    @PUT("/app/authenticate")
    Call<User> authenticate(@Body UserResource resource);

    @POST("/app/visit")
    Call<ResponseBody> onVisitSave(@Body ActionResource resource);

    @PUT("/app/visit")
    Call<ResponseBody> onVisitUpdate(@Body ActionResource resource);

    @Multipart
    @POST("/app/call")
    Call<ResponseBody> onCallSave(@Part MultipartBody.Part file);

    @Multipart
    @PUT("/app/call")
    Call<ResponseBody> onCallUpdate(@Part MultipartBody.Part file);

    @GET("/app/synchronization/request/{username}")
    Call<Void> synchronizationRequest(@Path("username") String username);

    @GET("/app/synchronize/{username}")
    Call<DeviceSynchronizeDTO> synchronize(@Path("username") String username);

    @GET("/app/activity/{code}")
    Call<ActionResource> findActivityByCode(@Path("code") String code);

    @GET("/app/account/{cuit}")
    Call<AccountResource> findAccountByCUIT(@Path("cuit") String cuit);

    @PUT("/activity/{code}/tomorrow")
    Call<ResponseBody> onMoveActivityToTomorrowApp(@Path("code") String code);

    @PUT("/app/user/gpsAll")
    Call<String> sendUserLocation(@Body String latlng);

    @POST("/app/call/missed")
    Call<ResponseBody> onMissedCall(@Body ActionResource resource);

    @GET("/app/contact/{id}")
    Call<ContactResource> findContactById(@Path("id") Long id);

    @GET("/app/action/{code}")
    Call<ActionResource> findActionByCode(@Path("code") String code);

    @GET("/app/address/{cuit}")
    Call<AccountResource> findAddressPrincipalByAccountCuit(@Path("cuit") String cuit);

}
