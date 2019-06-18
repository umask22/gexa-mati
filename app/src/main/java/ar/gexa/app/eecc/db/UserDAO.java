package ar.gexa.app.eecc.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import ar.gexa.app.eecc.models.User;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserDAO {

    @Insert(onConflict = REPLACE)
    void save(User user);

    @Update
    void update(User user);

    @Query("delete from User")
    void delete();

    @Query("select * from User limit 1")
    User findCurrentUser();
}
