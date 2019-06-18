package ar.gexa.app.eecc.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.utils.DateUtils;
import common.models.constants.SyncConstants;
import common.models.dto.DeviceSynchronizeDTO;
import common.models.resources.AccountResource;
import common.models.resources.ContactResource;
import retrofit2.Call;
import retrofit2.Response;

public class SyncService extends IntentService {

    public static void download(Context context, BroadcastReceiver receiver) {
        final Intent intent = new Intent(context, SyncService.class);
        intent.putExtra("synchronization", "download");
        invoke(context, receiver, intent, "onDownloadCompleted");
    }

    public static void upload(Context context, BroadcastReceiver receiver) {
        final Intent intent = new Intent(context, SyncService.class);
        intent.putExtra("synchronization", "upload");
        invoke(context, receiver, intent, "onUploadCompleted");
    }

    private static void invoke(Context context, BroadcastReceiver receiver, Intent intent, String intentFilter) {
        context.startService(intent);
        if(receiver != null)
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(intentFilter));
    }

    public SyncService() {super(SyncService.class.getSimpleName());}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if("download".equals(intent.getExtras().getString("synchronization")))
            onDownload(intent);
        else if("upload".equals(intent.getExtras().getString("synchronization")))
            onUpload(intent);
    }

    private void onUpload(Intent intent) {
        try {
            final DeviceSynchronizeDTO dto = new DeviceSynchronizeDTO();
            dto.user = User.toResource(UserRepository.getInstance().find());
            dto.activities = Activity.toResource(ActivityRepository.getInstance().findAllBySyncState(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name(),null, null));

//            GexaClient.get().getAPI().upload(dto);

        } catch (Exception e) {
            NotificationService.getInstance().txtLog(SyncService.class.getSimpleName()+e.getMessage());
            Log.e(SyncService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void onDownload(Intent intent) {
        try {
            final User user = UserRepository.getInstance().find();


//            for(Account resource : AccountRepository.getInstance().findAll()) {
//                ContactDirectoryService.getInstance().accountByCuit(resource);
//                ContactDirectoryService.getInstance().deleteApp(getApplicationContext(), resource);
//            }


            AccountRepository.getInstance().deleteAll();
            ActivityRepository.getInstance().deleteAll();
            ContactRepository.getInstance().deleteAll();

            final Call<DeviceSynchronizeDTO> call = GexaClient.getInstance().getAPI().synchronize(user.getUsername());
            final Response<DeviceSynchronizeDTO> response = call.execute();
            if(response.isSuccessful()) {
                final DeviceSynchronizeDTO dto = response.body();
//                ContactDirectoryService.getInstance().saveByAccounts(getApplicationContext(), dto.accounts);
                ContactRepository.getInstance().saveAll(dto.accounts);
                AccountRepository.getInstance().saveAll(dto.accounts);
                ActivityRepository.getInstance().saveAll(dto.activities);

                user.setDeviceSyncDate(DateUtils.toString(new Date(), DateUtils.Pattern.DEFAULT));
                user.setDeviceSyncStateType(SyncConstants.StateType.SYNCHRONIZED.name());
                UserRepository.getInstance().update(user);

                LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(new Intent("onDownloadCompleted"));
            }
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(SyncService.class.getSimpleName()+e.getMessage());
            Log.e(SyncService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void saveByAccounts(final Context context, final List<Account> resources) {
        for(Account resource : resources) {
            if(resource.deviceCode != null)
                delete(context, resource);
            create(context, resource);
        }
    }

    public void delete(Context context, Account resource) {
        if(resource.deviceCode != null)
            delete(context, resource.deviceCode);
    }

    public void delete(final Context context, final String deviceCode) {
        context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Contacts._ID + " = ?", new String[] {deviceCode});
    }

    private void create(Context context, Account resource) {

        final ContentResolver contentResolver = context.getContentResolver();
        final ContentValues contentValues = new ContentValues();

        final long rawContactId = getRawId(contentResolver);
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, resource.name);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.Data.DATA15, "gexa");
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, resource.name);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        for(Contact contact : resource.contacts) {

            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            contentValues.put(String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE), ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
            contentValues.put(String.valueOf(ContactsContract.CommonDataKinds.Phone.LABEL), contact.description);
            contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone());
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
        resource.deviceCode = String.valueOf(rawContactId);
    }

    private long getRawId(ContentResolver contentResolver) {
        return ContentUris.parseId(contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, new ContentValues()));
    }

}
