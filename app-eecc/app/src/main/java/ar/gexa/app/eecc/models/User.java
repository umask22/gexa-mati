package ar.gexa.app.eecc.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import common.models.resources.UserResource;

@Entity(tableName = "user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @PrimaryKey
    private Long id;

    private String name;

    private String username;

    private String password;

    private String supervisorName;

    private String roleName;

    private String roleType;

    private String deviceToken;

    private String deviceSyncStateType;
    
    private String deviceSyncDate;

    public static User fromResource(UserResource resource){

        final User user = new User();
        user.setName(resource.name);
        user.setUsername(resource.username);
        user.setPassword(resource.password);
        user.setSupervisorName(resource.supervisorName);
        user.setRoleName(resource.roleName);
        user.setRoleType(resource.roleType);
        user.setDeviceToken(resource.deviceToken);
        user.setDeviceSyncStateType(resource.deviceSyncStateType);
        user.setDeviceSyncDate(resource.deviceSyncDate);

        return user;
    }

    public static UserResource toResource(User user) {
        final UserResource resource = new UserResource();
        resource.username = user.getUsername();
        resource.name = user.getName();
        resource.deviceToken = user.getDeviceToken();
        return resource;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceSyncStateType() {
        return deviceSyncStateType;
    }

    public void setDeviceSyncStateType(String deviceSyncStateType) {
        this.deviceSyncStateType = deviceSyncStateType;
    }

    public String getDeviceSyncDate() {
        return deviceSyncDate;
    }

    public void setDeviceSyncDate(String deviceSyncDate) {
        this.deviceSyncDate = deviceSyncDate;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }
}
