package ar.gexa.app.eecc.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.v7.app.AppCompatActivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

import common.models.constants.SyncConstants;
import common.models.constants.VisitConstants;
import common.models.resources.AccountResource;
import common.models.resources.ActionResource;
import common.models.resources.ActivityResource;
import common.models.resources.AddressResource;
import common.models.resources.ContactResource;

@Entity(tableName = "activity")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity {

    @PrimaryKey
    private Long id;

    private String code;

    private String orderDate;

    private String description;

    private String accountCuit;

    private String accountName;

    private String user;

    private String state;

    private String stateType;

    private String result;

    private String type;

    private String typeDescription;

    private String reasonNotVisit;

    private String priority;

    private String priorityType;

    private String contactId;

    private String contactDescription;

    public String contactPhone;

    public String contactMail;

    private String addressId;

    private String addressDescription;

    private double lat;

    private double lng;

    private String callStartDate;

    private String callFinishDate;

    private String callPath;

    private String callType;

    private String visitType;

    private String syncStateType = SyncConstants.StateType.SYNCHRONIZED.name();

    public static Activity fromResource(ActionResource resource){

        final Activity activity = new Activity();
        activity.setCode(resource.code);
        activity.setOrderDate(resource.orderDate);
        activity.setDescription(resource.activity.description);
        activity.setAccountCuit(resource.account.cuit);
        activity.setAccountName(resource.account.name);
        activity.setUser(resource.user);
        activity.setState(resource.activity.state);
        activity.setStateType(resource.activity.stateType);
        activity.setResult(resource.activity.observation);
        activity.setType(resource.activity.type);
        activity.setTypeDescription(resource.activity.typeDescription);
        activity.setReasonNotVisit(resource.activity.reasonNotVisit);
        activity.setPriority(resource.activity.priority);
        activity.setPriorityType(resource.activity.priorityType);
        if(resource.address != null) {
            activity.setAddressId(resource.address.id);
            activity.setAddressDescription(resource.address.description);
            activity.setLat(resource.address.lat);
            activity.setLng(resource.address.lng);
        }

        if(resource.contact != null) {
            activity.setContactId(resource.contact.id);
            activity.setContactDescription(resource.contact.description);
            activity.setContactPhone(resource.contact.phone);
            activity.setContactMail(resource.contact.mail);
        }

        activity.setCallType(resource.activity.callType);
        activity.setVisitType(resource.activity.visitType);

        return activity;
    }

    public static ActionResource toResource(Activity activity) {
        final ActionResource resource = new ActionResource();

        resource.code = activity.getCode();
        resource.orderDate = activity.getOrderDate();
        resource.account = new AccountResource();
        resource.account.cuit = activity.getAccountCuit();
        resource.account.name = activity.getAccountName();
        resource.user = activity.getUser();

        resource.activity = new ActivityResource();
        resource.activity.description = activity.getDescription();
        resource.activity.state = activity.getState();
        resource.activity.stateType = activity.getStateType();
        resource.activity.observation = activity.getResult();
        resource.activity.type = activity.getType();
        resource.activity.typeDescription = activity.getTypeDescription();
        resource.activity.priority = activity.getPriority();
        resource.activity.priorityType = activity.getPriorityType();
        resource.activity.visitType = activity.getVisitType();
        resource.activity.reasonNotVisit = activity.getReasonNotVisit();
        resource.activity.lat = activity.getLat();
        resource.activity.lng = activity.getLng();
        resource.activity.callStartDate = activity.getCallStartDate();
        resource.activity.callFinishDate = activity.getCallFinishDate();
        resource.activity.callType = activity.getCallType();

        resource.address = new AddressResource();
        resource.address.id = activity.getAddressId();
        resource.address.description = activity.getAddressDescription();

        resource.contact = new ContactResource();
        resource.contact.id = activity.getContactId();
        resource.contact.description = activity.getContactDescription();
        resource.contact.phone = activity.getContactPhone();
        resource.contact.mail = activity.getContactMail();

        return resource;
    }

    public static List<ActionResource> toResource(List<Activity> activities) {
        final List<ActionResource> resources = new ArrayList<>();
        for(Activity activity : activities)
            resources.add(toResource(activity));
        return resources;
    }

    public static List<Activity> fromResource(List<ActionResource> resources) {
        final List<Activity> activities = new ArrayList<>();
        for(ActionResource resource : resources)
            activities.add(fromResource(resource));
        return activities;
    }

    public static VisitConstants.Type getVisitTypeByDescription(String type){
        if(type == null)
            return null;

        if("relevamiento".equals(type.toLowerCase()))
            return VisitConstants.Type.SURVEY;
        else if("presentacion".equals(type.toLowerCase()))
            return VisitConstants.Type.PRESENTATION;
        else
            return VisitConstants.Type.SALE;
    }

    public static VisitConstants.ReasonNoVisitType getVisitReasonNoVisitByDescrption(String type){
        if(type == null)
            return null;

        if("cerrado".equals(type.toLowerCase()))
            return VisitConstants.ReasonNoVisitType.CLOSED;
        else if("no existe la direccion".equals(type.toLowerCase()))
            return VisitConstants.ReasonNoVisitType.ADDRESS_NO_EXIST;
        else
            return VisitConstants.ReasonNoVisitType.ACCOUNT_NO_EXIST;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateType() {
        return stateType;
    }

    public void setStateType(String stateType) {
        this.stateType = stateType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactDescription() {
        return contactDescription;
    }

    public void setContactDescription(String contactDescription) {
        this.contactDescription = contactDescription;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
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

    public String getCallStartDate() {
        return callStartDate;
    }

    public void setCallStartDate(String callStartDate) {
        this.callStartDate = callStartDate;
    }

    public String getCallFinishDate() {
        return callFinishDate;
    }

    public void setCallFinishDate(String callFinishDate) {
        this.callFinishDate = callFinishDate;
    }

    public String getCallPath() {
        return callPath;
    }

    public void setCallPath(String callPath) {
        this.callPath = callPath;
    }

    public String getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getSyncStateType() {
        return syncStateType;
    }

    public void setSyncStateType(String syncStateType) {
        this.syncStateType = syncStateType;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(String contactMail) {
        this.contactMail = contactMail;
    }

    public String getReasonNotVisit() {
        return reasonNotVisit;
    }

    public void setReasonNotVisit(String reasonNotVisit) {
        this.reasonNotVisit = reasonNotVisit;
    }
}
