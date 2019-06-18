package ar.gexa.app.eecc.repository;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ar.gexa.app.eecc.db.DatabaseManager;
import ar.gexa.app.eecc.models.Account;
import common.models.resources.AccountResource;

public class AccountRepository {

    private static volatile AccountRepository instance;

    public static AccountRepository getInstance() {
        if (instance == null) {
            synchronized (AccountRepository.class) {
                if (instance == null)
                    instance = new AccountRepository();
            }
        }
        return instance;
    }

    public void deleteAll() {
        DatabaseManager.getInstance().getDB().getAccountDao().deleteAll();
    }

    public void save(final AccountResource resource) {
        try {

            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getAccountDao().save(Account.fromResource(resource));
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(AccountRepository.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void saveAll(final List<AccountResource> resources) {
        try {

            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getAccountDao().saveAll(Account.fromResource(resources));
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(AccountRepository.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void update(final Account account) {
        try {

            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getAccountDao().update(account);
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(AccountRepository.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public List<Account> findAll() {
//        return DatabaseManager.get().getDB().getAccountDao().findAll();
        try {
            final Callable<List<Account>> callable = new Callable<List<Account>>() {
                @Override
                public List<Account> call() throws Exception {
                    return DatabaseManager.getInstance().getDB().getAccountDao().findAll();
                }
            };
            return Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(AccountRepository.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public Account findById(final Long id) throws Exception {
        final Callable<Account> callable = new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return DatabaseManager.getInstance().getDB().getAccountDao().findById(id);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Account findByCUIT(final String cuit) throws Exception {
        final Callable<Account> callable = new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return DatabaseManager.getInstance().getDB().getAccountDao().findByCUIT(cuit);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public void delete(final Account account) throws Exception {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getAccountDao().delete(account);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();

    }
}
