package ar.gexa.app.eecc.services;

import android.content.Context;

import java.util.List;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import common.models.resources.ContactResource;

public class ContactService {

    private static ContactService instance;

    public static ContactService getInstance() {
        if (instance == null) {
            synchronized (ContactService.class) {
                if (instance == null)
                    instance = new ContactService();
            }
        }
        return instance;
    }

    public void onContactDelete(String id, String newContactId, Context context){
        try {
            if(!"0".equals(newContactId)) {
                Contact contactNew = ContactRepository.getInstance().findByCode(newContactId);
                List<Activity> activities = ActivityRepository.getInstance().findByContactId(String.valueOf(id));
                if (activities != null && activities.size() != 0) {
                    for (Activity activity : activities) {
                        if (activity != null) {
                            activity.setContactId(newContactId);
                            activity.setContactDescription(contactNew.getDescription());
                            activity.setContactPhone(contactNew.getPhone());
                            ActivityRepository.getInstance().update(activity);
                        }
                    }
                }
            }
            Contact contact = ContactRepository.getInstance().findByCode(id);
//            ContactDirectoryService.getInstance().deleteContactNumber(context,AccountRepository.getInstance().findByCUIT(contact.accountCuit).deviceCode,contact.getPhone(),null);
            ContactRepository.getInstance().delete(contact);
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(ContactService.class.getSimpleName()+e.getMessage());
            e.printStackTrace();
        }
    }

    public void onContactUpdate(String id, final Context context) {
        GexaClient.getInstance().onContactUpdate(Long.valueOf(id), new Callback<ContactResource>(context) {
            @Override
            public void onSuccess(ContactResource resource) {
                try {
                    Contact contact = ContactRepository.getInstance().findByCode(resource.id);

                    if(contact != null){
                        contact.setDescription(resource.description);
                        contact.setPhone(resource.phone);
                        ContactRepository.getInstance().update(contact);

//                        ContactDirectoryService.getInstance().deleteContactNumber(context,AccountRepository.getInstance().findByCUIT(contact.accountCuit).deviceCode,null,contact.code);
//                        ContactDirectoryService.getInstance().addContactNumber(context,AccountRepository.getInstance().findByCUIT(resource.accountCuit),Contact.fromResource(resource));
                    }
                } catch (Exception e) {
                    NotificationService.getInstance().txtLog(ContactService.class.getSimpleName()+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void onContactSave(String id, final Context context) {
        GexaClient.getInstance().onContactSave(Long.valueOf(id), new Callback<ContactResource>(context) {
            @Override
            public void onSuccess(ContactResource resource) {
                try {
                    ContactRepository.getInstance().save(Contact.fromResource(resource));
//                    ContactDirectoryService.getInstance().addContactNumber(context,AccountRepository.getInstance().findByCUIT(resource.accountCuit),Contact.fromResource(resource));
                } catch (Exception e) {
                    NotificationService.getInstance().txtLog(ContactService.class.getSimpleName()+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}