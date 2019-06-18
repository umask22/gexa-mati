package ar.gexa.app.eecc.services;

import android.content.Context;
import android.util.Log;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import common.models.resources.AccountResource;

public class AccountService {

    private static AccountService instance;

    public static AccountService getInstance() {
        if (instance == null) {
            synchronized (AccountService.class) {
                if (instance == null)
                    instance = new AccountService();
            }
        }
        return instance;
    }

    public void onAccountDiscarded(Context context, String cuit) {
        try {
            final Account account = AccountRepository.getInstance().findByCUIT(cuit);
            for (Contact contact : ContactRepository.getInstance().findAllByCuit(cuit)){
                ContactRepository.getInstance().delete(contact);
            }
//            ContactDirectoryService.getInstance().deleteApp(context,account);
            ActivityService.getInstance().onAccountDiscarded(account);
            AccountRepository.getInstance().delete(account);
        }catch (Exception e) {
            NotificationService.getInstance().txtLog(AccountService.class.getSimpleName() + e.getMessage());
            Log.e(AccountService.class.getSimpleName(), e.getMessage(),e);
        }
    }

    public void onSynchronizeAccountByCUIT(String cuit, final Context context) {
        GexaClient.getInstance().onSynchronizeAccountByCUIT(cuit, new Callback<AccountResource>(context) {
            @Override
            public void onSuccess(AccountResource resource) {
                try {
                    final Account persistedObj = AccountRepository.getInstance().findByCUIT(resource.cuit);
                    if(persistedObj == null) {
                        AccountRepository.getInstance().save(resource);
                        ContactDirectoryService.getInstance().create(context,resource);
                    }
                    else {
                        final Account account = Account.fromResource(resource);
                        account.setId(persistedObj.getId());
                        AccountRepository.getInstance().update(account);
                    }
                }catch (Exception e) {
                    NotificationService.getInstance().txtLog(AccountService.class.getSimpleName() + e.getMessage());
                    Log.e(AccountService.class.getSimpleName(), e.getMessage(),e);
                }
            }
        });
    }

    public void onUpdateAddress(String cuit, Context context) {
        GexaClient.getInstance().findAddressPrincipalByAccountCuit(cuit, new Callback<AccountResource>(context) {
            @Override
            public void onSuccess(AccountResource accountResource) {
                try {
                    Account account = AccountRepository.getInstance().findByCUIT(accountResource.cuit);
                    if(account != null) {
                        account.setAddressType(accountResource.address.type);
                        account.setAddressDescription(accountResource.address.description);
                        account.setLat(accountResource.address.lat);
                        account.setLng(accountResource.address.lng);
                        AccountRepository.getInstance().update(account);
                    }
                } catch (Exception e) {
                    NotificationService.getInstance().txtLog(AccountService.class.getSimpleName() + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
