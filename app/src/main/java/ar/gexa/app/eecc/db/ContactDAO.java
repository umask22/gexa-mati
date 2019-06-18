package ar.gexa.app.eecc.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ContactDAO {

    @Insert(onConflict = REPLACE)
    void save(Contact contact);

    @Insert(onConflict = REPLACE)
    void saveAll(List<Contact> resources);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact resource);

    @Query("DELETE FROM Contact")
    void deleteAll();

    @Query("select * from Contact order by description asc")
    List<Contact> findAll();

    @Query("select * from Contact where code = :code")
    Contact findByCode(String code);

    @Query("select * from Contact where phone like :number")
    Contact findByNumber(String number);

    @Query("select * from Contact where phone = :number")
    List<Contact> findAllByNumber(String number);

    @Query("select * from Contact where phone like :number")
    List<Contact> findAllByNumberTwo(String number);

    @Query("select * from Contact where accountCuit = :cuit")
    List<Contact> findAllByCuit(String cuit);

}
