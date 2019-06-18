package ar.gexa.app.eecc.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

import common.models.resources.AccountResource;
import common.models.resources.ContactResource;

@Entity(tableName = "contact")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {

    @PrimaryKey
    public Long id;
    public String code;
    public String accountCuit;
    public String accountName;
    public String type;
    public String typeDescription;
    public String description;
    public String phone;
    public String phoneIntern;
    public String mail;
    public String observation;

    public boolean isPrincipal;

    public boolean isGexa;

    public static Contact fromResource(ContactResource resource){
        final Contact contact = new Contact();
        contact.setCode(resource.id);
        contact.setAccountCuit(resource.accountCuit);
        contact.setAccountName(resource.accountName);
        contact.setType(resource.type);
        contact.setTypeDescription(resource.typeDescription);
        contact.setDescription(resource.description);
        contact.setPhone(resource.phone);
        contact.setPhoneIntern(resource.phoneIntern);
        contact.setMail(resource.mail);
        contact.setGexa(true);
        contact.setPrincipal(resource.isPrincipal);

        return contact;
    }

    public static ContactResource toResource(Contact contact){
        final ContactResource resource = new ContactResource();
        resource.id = contact.code;
        resource.accountCuit = contact.accountCuit;
        resource.accountName = contact.accountName;
        resource.type = contact.type;
        resource.typeDescription = contact.typeDescription;
        resource.description = contact.description;
        resource.phone = contact.phone;
        resource.phoneIntern = contact.phoneIntern;
        resource.mail = contact.mail;
        resource.isPrincipal = contact.isPrincipal;

        return resource;
    }

    public static List<ContactResource> toResource (List<Contact> contacts){
        final List<ContactResource> resources = new ArrayList<>();
        for(Contact contact : contacts)
            resources.add(toResource(contact));
        return resources;
    }

    public static List<Contact> fromResource(List<AccountResource> resources) {
        final List<Contact> contacts = new ArrayList<>();
        for(AccountResource resource : resources) {
            for (ContactResource contactResource : resource.contacts){
                contacts.add(fromResource(contactResource));
            }
        }
        return contacts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccountCuit() {
        return accountCuit;
    }

    public void setAccountCuit(String accountCuit) {
        this.accountCuit = accountCuit;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrincipal() {
        return isPrincipal;
    }

    public void setPrincipal(boolean principal) {
        isPrincipal = principal;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneIntern() {
        return phoneIntern;
    }

    public void setPhoneIntern(String phoneIntern) {
        this.phoneIntern = phoneIntern;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public boolean isGexa() {
        return isGexa;
    }

    public void setGexa(boolean gexa) {
        isGexa = gexa;
    }
}
