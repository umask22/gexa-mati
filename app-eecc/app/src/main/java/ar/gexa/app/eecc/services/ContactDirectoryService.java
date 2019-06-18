package ar.gexa.app.eecc.services;


import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Contact;
import common.models.resources.AccountResource;
import common.models.resources.ContactResource;

public class ContactDirectoryService {

    private static volatile ContactDirectoryService instance;

    public static ContactDirectoryService getInstance() {
        return instance == null ? instance = new ContactDirectoryService() : instance;
    }

    public Contact findContactByNumber(Context context, String number) {

        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(number));
        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.DATA15}, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                Contact resource = new Contact();
                resource.accountName = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                contactLookup.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID);
                resource.isGexa = "gexa".equals(contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DATA15)));
                return resource;
            }
        }finally{
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return null;
    }

    public void saveByAccounts(final Context context, final List<AccountResource> resources) {
        for(AccountResource resource : resources) {
            if(resource.deviceCode != null)
                delete(context, resource);
            create(context, resource);
        }
    }

    public void create(Context context, AccountResource resource) {

        final ContentResolver contentResolver = context.getContentResolver();
        final ContentValues contentValues = new ContentValues();

        final long rawContactId = getRawId(contentResolver);
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, resource.name);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.Data.DATA13, resource.cuit);
        contentValues.put(ContactsContract.Data.DATA15, "gexa");
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, resource.name);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

        for(ContactResource contact : resource.contacts) {

            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            contentValues.put(String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE), ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
            contentValues.put(String.valueOf(ContactsContract.CommonDataKinds.Phone.LABEL), contact.description);
            contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phone);
            contentValues.put(ContactsContract.Data.DATA14, contact.id);

            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
        resource.deviceCode = String.valueOf(rawContactId);
    }

    public void addContactNumber(Context context,Account account,Contact contact){

        final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        ContentProviderOperation contentProviderOperation ;
        builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, account.deviceCode)
                .withValue(ContactsContract.Data.DATA15,"gexa")
                .withValue(ContactsContract.Data.DATA14, contact.code)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, contact.description);
        contentProviderOperation = builder.build();
        ops.add(contentProviderOperation);
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            NotificationService.getInstance().txtLog(ContactDirectoryService.class.getSimpleName()+e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteContactNumber(Context context,String deviceCode,String phone,String code){

        final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        if(code == null) {
            String[] args = new String[]{deviceCode,phone};
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", args).build());
        }else {
            String[] args = new String[]{deviceCode,code};
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.DATA14 + "=?", args).build());
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            NotificationService.getInstance().txtLog(ContactDirectoryService.class.getSimpleName()+e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Context context, AccountResource resource) {
        if(resource.deviceCode != null)
            delete(context, resource.deviceCode);
    }

    public void deleteApp(Context context, Account resource) {
        if(resource.deviceCode != null)
            delete(context, resource.deviceCode);
    }

    public void delete(final Context context, final String deviceCode) {
        context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Contacts._ID + " = ?", new String[] {deviceCode});
    }

//    public void accountByCuit(final Context context, Account account) {
//
//
//    }

    private long getRawId(ContentResolver contentResolver) {
        return ContentUris.parseId(contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, new ContentValues()));
    }
}
