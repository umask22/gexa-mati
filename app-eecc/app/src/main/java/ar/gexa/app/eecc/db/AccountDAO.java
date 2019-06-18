package ar.gexa.app.eecc.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ar.gexa.app.eecc.models.Account;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface AccountDAO {

    @Insert(onConflict = REPLACE)
    void save(Account account);

    @Update
    void update(Account account);

    @Insert(onConflict = REPLACE)
    void saveAll(List<Account> resources);

    @Delete
    void delete(Account resource);

    @Query("DELETE FROM Account")
    void deleteAll();

    @Query("select * from Account order by name asc")
    List<Account> findAll();

    @Query("select * from Account where id = :id")
    Account findById(Long id);

    @Query("select * from Account where cuit = :cuit")
    Account findByCUIT(String cuit);
}
