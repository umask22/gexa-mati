package ar.gexa.app.eecc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActionRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.views.CallCompleteView;
import ar.gexa.app.eecc.views.MenuView;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.CallConstants;
import common.models.resources.AccountResource;
import common.models.resources.ActionResource;
import common.models.resources.ContactResource;


public class FCMMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            onProcessMessage(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
//            onProcessNotification(remoteMessage.getNotification());
        }
    }

    private void onProcessMessage(Map<String, String> data) {
        final String messageType = data.get("messageType");
        if("onSynchronizationPending".equals(messageType))
            onSynchronizationPending();
        else if("onActivityCreated".equals(messageType))
            onActivityCreated(data.get("code"));
        else if("onActivityCanceled".equals(messageType))
            onActivityDelete(data.get("code"));
        else if("onActivityCompleted".equals(messageType))
            onActivityDelete(data.get("code"));
        else if("onAccountDiscarded".equals(messageType))
            onAccountDiscarded(data.get("cuit"));
        else if("onAccountAssigned".equals(messageType))
            onAccountAssigned(data.get("cuit"));
        else if("onDeviceLocateStart".equals(messageType))
            onDeviceLocateStart();
        else if("onDeviceLocateStop".equals(messageType))
            onDeviceLocateStop();
        else if("doCall".equals(messageType))
            onDoCall(data.get("actionCode"));
        else if("showMessage".equals(messageType))
            showMessage(data.get("message"));
        else if("saveContact".equals(messageType))
            saveContact(data.get("id"));
        else if("updateContact".equals(messageType))
            updateContact(data.get("id"));
        else if("deleteContact".equals(messageType))
            deleteContact(data.get("id"),data.get("newContactId"));
        else if("deleteAddress".equals(messageType))
            deleteAddress(data.get("code"));
        else if("updateAddress".equals(messageType))
            updateAddress(data.get("cuit"));
        else if("updateApk".equals(messageType))
            updateApk();
        else if("getMail".equals(messageType))
            getMail();
        else
            Log.i(FCMMessageService.class.getSimpleName(), "onMessageReceived(" + messageType + ") not found");
    }

    private void getMail() {
        NotificationService.getInstance().getMail();
    }

    private void updateApk() {
        NotificationService.getInstance().updateApk(getApplicationContext());
    }

    private void updateAddress(String cuit) {
        AccountService.getInstance().onUpdateAddress(cuit, getApplicationContext());
    }

    private void deleteAddress(String actionCode) {
        ActivityService.getInstance().findActionByCode(actionCode, getApplicationContext());
    }

    private void deleteContact(String id, String newContactId) {
        ContactService.getInstance().onContactDelete(id, newContactId, getApplicationContext());
    }

    private void updateContact(String id) {
        ContactService.getInstance().onContactUpdate(id, getApplicationContext());
    }

    private void saveContact(String id) {
        ContactService.getInstance().onContactSave(id, getApplicationContext());
    }

    private void showMessage(String message) {
        NotificationService.getInstance().show(getApplicationContext(), "geXa", message, null);
    }

    private void onDoCall(String activityCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        try {
            Activity activity = ActivityRepository.getInstance().findByCode(activityCode);
            if(CallConstants.StateType.PENDING.name().equals(activity.getStateType())){
                if(ActivityService.getInstance().exist(activityCode)) {
                    editor.putBoolean("doCall" , true);
                    editor.apply();
                    final Intent intent = new Intent(getApplicationContext(), CallCompleteView.class);
                    intent.putExtra("activityCode", activityCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(FCMMessageService.class.getSimpleName()+e.getMessage());
            e.printStackTrace();
        }
    }

    private void onAccountAssigned(String cuit) {
        AccountService.getInstance().onSynchronizeAccountByCUIT(cuit, getApplicationContext());
    }

    private void onAccountDiscarded(String cuit) {
        AccountService.getInstance().onAccountDiscarded(getApplicationContext(), cuit);
    }

    private void onActivityDelete(String code) {
        ActivityService.getInstance().onActivityDelete(code);
    }

    private void onActivityCreated(String code) {
        ActivityService.getInstance().onSynchronizeActivityByCode(code, getApplicationContext());
    }

    private void onDeviceLocateStart() {
        NotificationService.getInstance().onDeviceLocateStart(getApplicationContext());
    }

    private void onDeviceLocateStop() {
        NotificationService.getInstance().onDeviceLocateStop(getApplicationContext());
    }

    private void onSynchronizationPending() {

        SyncService.download(this, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Intent newIntent = new Intent(getApplicationContext(), MenuView.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }
        });
    }

    private void onUnSync() {
//        DeviceService.getInstance().onDeviceUnSynchronize(getApplicationContext());
    }

//    private void onCompleteActivity(Map<String, String> data) {
//        GexaClient.get().findActionByCode(data.get("actionId"), new Callback<Action>() {
//            @Override
//            public void onResponse(Action action) {
//                ActivityBO.get().completeActivity(action);
//                onCompleteActivity();
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Log.e("FCMMessageService", t.getMessage());
//            }
//        });
//    }

//    private void onCompleteActivity() {
//        Intent intent = null;
//        if(ActivityBO.get().getActivity().getCall() != null)
//            intent = new Intent(this, CallCompleteView.class);
//        if(intent != null)
//            startActivity(intent);
//    }
}
