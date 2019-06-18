package ar.gexa.app.eecc.repository;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ar.gexa.app.eecc.db.DatabaseManager;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.services.UserService;
import common.models.resources.UserResource;

public class UserRepository {

    private static volatile UserRepository instance;

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null)
                    instance = new UserRepository();
            }
        }
        return instance;
    }

    public User find() {
        try {
            final Callable<User> callable = new Callable<User>() {
                @Override
                public User call() throws Exception {
                    return DatabaseManager.getInstance().getDB().getUserDao().findCurrentUser();
                }
            };
            return Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(UserService.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public void save(final UserResource resource) {
        try {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call(){
                    try {
                        DatabaseManager.getInstance().getDB().getUserDao().save(User.fromResource(resource));
                        return null;
                    }catch (Exception e){
                        Log.e("UserSave",e.getMessage());
                    }
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(UserService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void delete() {
        try {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseManager.getInstance().getDB().getUserDao().delete();
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(UserService.class.getSimpleName(), e.getMessage(), e);
        }
    }

    public void update(final User user) {

        try {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call(){
                    DatabaseManager.getInstance().getDB().getUserDao().update(user);
                    return null;
                }
            };
            Executors.newSingleThreadExecutor().submit(callable).get();
        }catch (Exception e) {
            Log.e(UserService.class.getSimpleName(), e.getMessage(), e);
        }
    }
}
