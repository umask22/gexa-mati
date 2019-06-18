package ar.gexa.app.eecc.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ar.gexa.app.eecc.models.Activity;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ActivityDAO {

    @Insert(onConflict = REPLACE)
    void save(Activity activity);

    @Insert(onConflict = REPLACE)
    void saveAll(List<Activity> activities);

    @Update
    void update(Activity activity);

    @Delete
    void delete(Activity activity);

    @Query("DELETE FROM Activity")
    void deleteAll();

    @Query("select * from Activity where syncStateType between :syncStateOne and :syncStateTwo and stateType = :stateTypeOne order by orderDate asc")
    List<Activity> findAllBySyncState(String syncStateOne, String syncStateTwo, String stateTypeOne);

    @Query("select * from Activity where orderDate between :dateFrom and :dateUntil and stateType = :stateType order by orderDate asc")
    List<Activity> findAllByOrderDateBetweenAndStateType(String dateFrom, String dateUntil, String stateType);

    @Query("select * from Activity where code = :code")
    Activity findByCode(String code);

    @Query("select * from Activity where contactId = :id")
    List<Activity> findByContactId(String id);

    @Query("select * from Activity where accountCuit = :cuit")
    Activity findByAccountCUIT(String cuit);

    @Query("select * from Activity where accountCuit = :cuit and stateType = :stateType")
    Activity findByAccountCUITAndStateType(String cuit, String stateType);
}
