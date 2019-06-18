package ar.gexa.app.eecc.repository;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ar.gexa.app.eecc.db.DatabaseManager;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import common.filter.Filter;
import common.filter.FilterUtils;
import common.models.resources.ActionResource;

public class ActivityRepository {

    private static volatile ActivityRepository instance;

    public static ActivityRepository getInstance() {
        return instance == null ? instance = new ActivityRepository() : instance;
    }

    public void deleteAll() {
        DatabaseManager.getInstance().getDB().getActivityDao().deleteAll();
    }

    public List<Activity> findAllByFilter(final Filter filter) throws Exception {

        final Callable<List<Activity>> callable = new Callable<List<Activity>>() {
            @Override
            public List<Activity> call() {
                final String dateFrom = FilterUtils.find("dateFrom", filter).queryValue;
                final String dateUntil = FilterUtils.find("dateUntil", filter).queryValue;
                final String stateType = FilterUtils.find("stateType", filter).queryValue;

                return DatabaseManager.getInstance().getDB().getActivityDao().findAllByOrderDateBetweenAndStateType(dateFrom, dateUntil, stateType);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public List<Activity> findAllBySyncState(final String syncStateOne, final String syncStateTwo, final String stateType) throws Exception {

        final Callable<List<Activity>> callable = new Callable<List<Activity>>() {
            @Override
            public List<Activity> call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findAllBySyncState(syncStateOne, syncStateTwo, stateType);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public void save(final Activity activity) throws Exception {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getActivityDao().save(activity);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public void update(final Activity activity) throws Exception{
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getActivityDao().update(activity);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();

    }

    public void saveAll(final List<ActionResource> resources) throws Exception {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getActivityDao().saveAll(Activity.fromResource(resources));
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public void delete(final Activity activity) throws Exception {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                DatabaseManager.getInstance().getDB().getActivityDao().delete(activity);
                return null;
            }
        };
        Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Activity findByCode(final String code) throws Exception {
        final Callable<Activity> callable = new Callable<Activity>() {
            @Override
            public Activity call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findByCode(code);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public List<Activity> findByContactId(final String id) throws Exception {
        final Callable<List<Activity>> callable = new Callable<List<Activity>>() {
            @Override
            public List<Activity> call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findByContactId(id);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Activity findByAccount(final Account account) throws Exception {
        final Callable<Activity> callable = new Callable<Activity>() {
            @Override
            public Activity call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findByAccountCUIT(account.getCuit());
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Activity findByAccountCUITAndStateType(final String accountCuit, final String stateType) throws Exception {
        final Callable<Activity> callable = new Callable<Activity>() {
            @Override
            public Activity call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findByAccountCUITAndStateType(accountCuit, stateType);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }

    public Activity exist(final String code) throws Exception {
        final Callable<Activity> callable = new Callable<Activity>() {
            @Override
            public Activity call() {
                return DatabaseManager.getInstance().getDB().getActivityDao().findByCode(code);
            }
        };
        return Executors.newSingleThreadExecutor().submit(callable).get();
    }
}