package ar.gexa.app.eecc.services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.File;

import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.utils.FileUtils;
import common.models.constants.CallConstants;
import common.models.constants.SyncConstants;
import common.models.resources.ActionResource;
import okhttp3.ResponseBody;

public class ActivityService {

    private static ActivityService instance;

    public static ActivityService getInstance() {
        if (instance == null) {
            synchronized (ActivityService.class) {
                if (instance == null)
                    instance = new ActivityService();
            }
        }
        return instance;
    }

    public void update(Activity activity) {
        try {

            if("Call".equals(activity.getType())) {
                activity.setCallStartDate(DateUtils.toString(PhoneCallBO.getInstance().getStart(), DateUtils.Pattern.DEFAULT));
                activity.setCallFinishDate(DateUtils.toString(PhoneCallBO.getInstance().getFinish(), DateUtils.Pattern.DEFAULT));
            }

            activity.setSyncStateType(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name());
            ActivityRepository.getInstance().update(activity);

            UserService.getInstance().onActivityUpdated();
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void save(Activity activity) {

        try {

            if("Call".equals(activity.getType())) {
                activity.setCallStartDate(DateUtils.toString(PhoneCallBO.getInstance().getStart(), DateUtils.Pattern.DEFAULT));
                activity.setCallFinishDate(DateUtils.toString(PhoneCallBO.getInstance().getFinish(), DateUtils.Pattern.DEFAULT));
            }

            activity.setSyncStateType(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name());
            ActivityRepository.getInstance().save(activity);

            UserService.getInstance().onActivityUpdated();
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void onActivitySynchronize(Activity activity, Callback<ResponseBody> callback){
        if("Call".equals(activity.getType()) && CallConstants.Type.OUTGOING.name().equals(activity.getCallType()) && !"Llamada saliente".equals(activity.getDescription()))
            onCallUpdate(activity, callback);
        else if("Call".equals(activity.getType()) && CallConstants.Type.OUTGOING.name().equals(activity.getCallType()) && "Llamada saliente".equals(activity.getDescription()))
            onCallSave(activity, callback);
        else if("Call".equals(activity.getType()) && CallConstants.Type.INCOMING.name().equals(activity.getCallType()) && "Llamada entrante".equals(activity.getDescription()))
            onCallSave(activity,callback);
        else if("Call".equals(activity.getType()) && CallConstants.Type.INCOMING.name().equals(activity.getCallType()) && !"Llamada entrante".equals(activity.getDescription()))
            onCallUpdate(activity, callback);
        else if("Visit".equals(activity.getType()))
            onVisitUpdate(activity,callback);
    }

    public void onVisitSave(Activity activity, Callback<ResponseBody> callback) {
        GexaClient.getInstance().onVisitSave(Activity.toResource(activity), callback);
    }

    public void onVisitUpdate(Activity activity, Callback<ResponseBody> callback) {
        GexaClient.getInstance().onVisitUpdate(Activity.toResource(activity), callback);
    }

    public void onMissedCall(ActionResource actionResource, Callback<ResponseBody> callback){
        GexaClient.getInstance().onMissedCall(actionResource,callback);
    }

    public void onCallSave(Activity activity, Callback<ResponseBody> callback) {

        try {
            zip(activity);
            GexaClient.getInstance().onCallSave(new File(Environment.getExternalStorageDirectory() + "/gexa/" + activity.getCode() + ".zip"), callback);
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void onCallUpdate(Activity activity, Callback<ResponseBody> callback) {

        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/gexa/" + activity.getCode() + ".zip");
            if(file.exists()){
                GexaClient.getInstance().onCallUpdate(new File(String.valueOf(file)), callback);
            }else{
                zip(activity);
                GexaClient.getInstance().onCallUpdate(new File(String.valueOf(file)), callback);
            }
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void zip(Activity activity) throws Exception {

        FileUtils.writeJSONToFile(new GsonBuilder().setDateFormat(DateUtils.Pattern.DEFAULT).create().toJson(Activity.toResource(activity)), Environment.getExternalStorageDirectory() + "/gexa/" + activity.getCode() + ".json");
        FileUtils.zip(
                activity.getCode(),
                Environment.getExternalStorageDirectory() + "/gexa/" + activity.getCode() + ".json",
                Environment.getExternalStorageDirectory() + "/gexa/calls/" + activity.getCode() + ".3gp");
    }

    public void onActivitySynchronized(Activity activity) {
        try {
            activity.setSyncStateType(SyncConstants.StateType.SYNCHRONIZED.name());
            ActivityRepository.getInstance().update(activity);
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void onSynchronizeActivityByCode(String code, Context context) {
        GexaClient.getInstance().onSynchronizeActivityByCode(code, new Callback<ActionResource>(context) {
            @Override
            public void onSuccess(ActionResource resource) {
                try {
                    ActivityRepository.getInstance().save(Activity.fromResource(resource));
                }catch (Exception e) {
                    NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
                    Log.e(ActivityService.class.getSimpleName(), e.getMessage(),e);
                }

            }
        });
    }

    public void findActionByCode(String actionCode, Context context){

        GexaClient.getInstance().findActionByCode(actionCode, new Callback<ActionResource>(context) {
            @Override
            public void onSuccess(ActionResource resource) {
                try {
                    Activity activity = ActivityRepository.getInstance().findByCode(resource.code);
                    Account account = AccountRepository.getInstance().findByCUIT(resource.account.cuit);
                    if(activity != null){
                        activity.setAddressId(resource.address.id);
                        activity.setAddressDescription(resource.address.description);
                        activity.setLat(resource.address.lat);
                        activity.setLng(resource.address.lng);

                        ActivityRepository.getInstance().update(activity);
                    }

                    if(account != null){
                        account.setAddressType(resource.address.type);
                        account.setAddressDescription(resource.address.description);
                        account.setLat(resource.address.lat);
                        account.setLng(resource.address.lng);
                        AccountRepository.getInstance().update(account);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onActivityDelete(String code) {
        try {
            delete(ActivityRepository.getInstance().findByCode(code));
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(),e);
        }
    }

    public void delete(Activity activity) {
        try {
            if(activity != null)
                ActivityRepository.getInstance().delete(activity);
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(),e);
        }
    }

    public void onAccountDiscarded(Account account) {
        try {
            delete(ActivityRepository.getInstance().findByAccount(account));
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            Log.e(ActivityService.class.getSimpleName(), e.getMessage(),e);
        }
    }

    public boolean exist(String code) {
        try {
            return ActivityRepository.getInstance().findByCode(code) != null;
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ActivityService.class.getSimpleName()+e.getMessage());
            //do nothing
            return false;
        }
    }
}
