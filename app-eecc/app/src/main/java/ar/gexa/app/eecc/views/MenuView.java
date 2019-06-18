package ar.gexa.app.eecc.views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaCas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.AccountService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.HTTPUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.CallConstants;
import okhttp3.OkHttpClient;

public class MenuView extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;

    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    boolean GpsStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        bind();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutView:
                onCloseSession();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCloseSession() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.close_session, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        final Button backView = AndroidUtils.findViewById(R.id.backView, Button.class, view);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        final Button acceptView = AndroidUtils.findViewById(R.id.closeSessionView, Button.class, view);
        acceptView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AndroidUtils.findViewById(R.id.progressView, ProgressBar.class, view).setVisibility(View.VISIBLE);
                acceptView.setVisibility(View.INVISIBLE);
                backView.setVisibility(View.INVISIBLE);
                AndroidUtils.findViewById(R.id.descriptionView, TextView.class, view).setText("Aguarde unos instantes");

                UserService.getInstance().onCloseSession(MenuView.this, dialog);
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void enableGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finishAndRemoveTask();
        }
    }

    private void bind() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        final RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(new RecyclerViewAdapter());

        final TextView welcomeView = findViewById(R.id.welcomeView);
        welcomeView.setText(getString(R.string.welcome, UserRepository.getInstance().find().getName()));
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<MenuItemView> {

        private List<MenuItem> items;

        private RecyclerViewAdapter() {
            items = new ArrayList<>();
            items.add(createMenuItem("Actividades", "Gestión de actividades pendientes"));
            items.add(createMenuItem("Radiomaps", "Mapeode cuentas"));
            items.add(createMenuItem("Sincronizacion", "Gestion de la base de datos"));
        }

        @Override
        public MenuItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.menu_item, parent, false);
            return new MenuItemView(view);
        }

        @Override
        public void onBindViewHolder(final MenuItemView holder, final int position) {
            holder.menuItem = items.get(position);
            holder.headerView.setText(holder.menuItem.header);
            holder.descriptionView.setText(holder.menuItem.description);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position == 0)
                        startActivity(new Intent(MenuView.this, ActivitySearchView.class));
                    else if(position == 1)
                        startActivity(new Intent(MenuView.this, RouteSearchView.class));
                    else if(position == 2)
                        startActivity(new Intent(MenuView.this, SynchronizationView.class));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private MenuItem createMenuItem(String header, String description) {
            final MenuItem menuItem = new MenuItem();
            menuItem.header = header;
            menuItem.description = description;
            return menuItem;
        }
    }

    private class MenuItem {
        public String header;
        public String description;
    }

    private class MenuItemView extends RecyclerView.ViewHolder{
        public View view;
        public TextView headerView;
        public TextView descriptionView;
        public MenuItem menuItem;

        MenuItemView(View view) {
            super(view);
            this.view = view;
            this.headerView = AndroidUtils.findViewById(R.id.headerView, TextView.class, this.view);
            this.descriptionView = AndroidUtils.findViewById(R.id.descriptionView, TextView.class, this.view);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(MenuView.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(
                        new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS); }
        }else if(!GpsStatus){
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(MenuView.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            if(!GpsStatus) {
                getLocation();
            }
        }
    }

    public void getLocation() {

        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(MenuView.this,Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED){
                    final LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);

                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this);
                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        status.startResolutionForResult(MenuView.this, REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        AlertWidget.create().showError(getApplicationContext(), e);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }
}