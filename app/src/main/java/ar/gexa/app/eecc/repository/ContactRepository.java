package ar.gexa.app.eecc.repository;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ar.gexa.app.eecc.db.DatabaseManager;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import common.models.resources.AccountResource;

public class ContactRepository {

    private static volatile ContactRepository instance;

    public static ContactRepository getInstance() {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null)
                    instance = new ContactRepository();
            }
        }
        return instance;
    }

    public void deleteAll() {
        DatabaseManager.getInstance().getDB().getContactDao().deleteAll();
    }

    public void save(final Contact resource) {
        try {

            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getContactDao().save(resource);
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(ContactRepository.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void update(final Contact contact) throws Exception{
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getContactDao().update(contact);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();

    }

    public void saveAll(final List<AccountResource> resources) {
        try {

            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getContactDao().saveAll(Contact.fromResource(resources));
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(ContactRepository.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public List<Contact> findAll() {
        try {
            final Callable<List<Contact>> callable = new Callable<List<Contact>>() {
                @Override
                public List<Contact> call() throws Exception {
                    return DatabaseManager.getInstance().getDB().getContactDao().findAll();
                }
            };
            return Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(ContactRepository.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public List<Contact> findAllByCuit(final String cuit) {
        try {
            final Callable<List<Contact>> callable = new Callable<List<Contact>>() {
                @Override
                public List<Contact> call() throws Exception {
                    return DatabaseManager.getInstance().getDB().getContactDao().findAllByCuit(cuit);
                }
            };
            return Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(ContactRepository.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public Contact findByCode(final String code) throws Exception {
        final Callable<Contact> callable = new Callable<Contact>() {
            @Override
            public Contact call() throws Exception {
                return DatabaseManager.getInstance().getDB().getContactDao().findByCode(code);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Contact findByNumber(final String number) throws Exception {
        final Callable<Contact> callable = new Callable<Contact>() {
            @Override
            public Contact call() throws Exception {
                return DatabaseManager.getInstance().getDB().getContactDao().findByNumber(number);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public List<Contact> findAllByNumberTwo(final String number) throws Exception {
        final Callable<List<Contact>> callable = new Callable<List<Contact>>() {
            @Override
            public List<Contact> call() throws Exception {
                return DatabaseManager.getInstance().getDB().getContactDao().findAllByNumberTwo(number);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public List<Contact> findAllByNumber(final String number) throws Exception {
        final Callable<List<Contact>> callable = new Callable<List<Contact>>() {
            @Override
            public List<Contact> call() throws Exception {
                return DatabaseManager.getInstance().getDB().getContactDao().findAllByNumber(number);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }



    public void delete(final Contact contact) throws Exception {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getContactDao().delete(contact);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();

    }
}
