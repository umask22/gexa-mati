package ar.gexa.app.eecc.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

import common.models.resources.AccountResource;

@Entity(tableName = "account")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    @PrimaryKey
    public Long id;
    public String deviceCode;
    public String name;
    public String cuit;
    public String executiveName;
    public String mobileCompanyName;
    public String fleet;
    public String nr;

    public String addressType;
    public String addressDescription;
    public double lat;
    public double lng;

    @Ignore
    public List<Contact> contacts = new ArrayList<>();

    public static Account fromResource(AccountResource resource) {

        final Account account = new Account();
        account.setDeviceCode(resource.deviceCode);
        account.setName(resource.name);
        account.setCuit(resource.cuit);
        account.setExecutiveName(resource.executiveName);
        account.setMobileCompanyName(resource.mobileCompanyName);
        account.setFleet(resource.fleet);
        account.setNr(resource.nr);
        if(resource.address != null) {
            account.setAddressDescription(resource.address.description);
            account.setAddressType(resource.address.type);
            account.setLat(resource.address.lat);
            account.setLng(resource.address.lng);
        }

        return account;
    }

    public static List<Account> fromResource(List<AccountResource> resources) {
        final List<Account> accounts = new ArrayList<>();
        for(AccountResource resource : resources) {
            accounts.add(fromResource(resource));
        }
        return accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getExecutiveName() {
        return executiveName;
    }

    public void setExecutiveName(String executiveName) {
        this.executiveName = executiveName;
    }

    public String getMobileCompanyName() {
        return mobileCompanyName;
    }

    public void setMobileCompanyName(String mobileCompanyName) {
        this.mobileCompanyName = mobileCompanyName;
    }

    public String getFleet() {
        return fleet;
    }

    public void setFleet(String fleet) {
        this.fleet = fleet;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressDescription() {
        return addressDescription;
    }

    public void setAddressDescription(String addressDescription) {
        this.addressDescription = addressDescription;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
