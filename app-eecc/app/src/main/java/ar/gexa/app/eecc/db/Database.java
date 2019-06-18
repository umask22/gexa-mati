package ar.gexa.app.eecc.db;

import android.arch.persistence.room.RoomDatabase;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.models.User;

@android.arch.persistence.room.Database(entities = {User.class, Activity.class, Account.class, Contact.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    public abstract UserDAO getUserDao();
    public abstract ActivityDAO getActivityDao();
    public abstract AccountDAO getAccountDao();
    public abstract ContactDAO getContactDao();
}