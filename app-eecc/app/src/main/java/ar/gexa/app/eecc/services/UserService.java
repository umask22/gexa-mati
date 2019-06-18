package ar.gexa.app.eecc.services;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.DateUtils;
import common.models.constants.SyncConstants;
import common.models.resources.UserResource;

public class UserService {

    private static volatile UserService instance;

    public static UserService getInstance() {
        return instance == null ? instance = new UserService() : instance;
    }

    public void onUserSelected(final UserResource user) {
        user.deviceSyncStateType = SyncConstants.StateType.PENDING_SYNCHRONIZATION.name();
        user.deviceSyncStateDescription = SyncConstants.StateType.getDescription(SyncConstants.StateType.PENDING_SYNCHRONIZATION);
        UserRepository.getInstance().save(user);
    }

    public void onCloseSession(final Context context, final Dialog dialog) {

        try {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    AndroidUtils.saveValueToPreferences("token", "", context);
                    UserRepository.getInstance().delete();

                    for(Account resource : AccountRepository.getInstance().findAll()) {
                        ContactDirectoryService.getInstance().deleteApp(context, resource);
                    }

                    ContactRepository.getInstance().deleteAll();
                    AccountRepository.getInstance().deleteAll();
                    ActivityRepository.getInstance().deleteAll();

                    if(dialog != null)
                        dialog.dismiss();

                    final Intent intent = context.getPackageManager().getLaunchIntentForPackage("ar.gexa.app.eecc");
                    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(UserService.class.getSimpleName()+e.getMessage());
            Log.e(UserService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void onActivityUpdated() {
        final User user = UserRepository.getInstance().find();
        user.setDeviceSyncStateType(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name());
        user.setDeviceSyncDate(DateUtils.toString(new Date(),DateUtils.Pattern.DEFAULT));
        UserRepository.getInstance().update(user);
    }

    public void onActivitySynchronized() {

        final User user = UserRepository.getInstance().find();
        user.setDeviceSyncStateType(SyncConstants.StateType.SYNCHRONIZED.name());
        user.setDeviceSyncDate(DateUtils.toString(new Date(),DateUtils.Pattern.DEFAULT));
        UserRepository.getInstance().update(user);
    }
}
