package ar.gexa.app.eecc.views;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.services.ActivityService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.SyncConstants;
import common.models.constants.VisitConstants;
import okhttp3.ResponseBody;

public class VisitCompleteView extends AppCompatActivity implements LocationListener,OnMapReadyCallback{

    private LocationManager locationManager;
    private GoogleMap map;

    private Spinner typeVisitSpinnerView;
    private Spinner notVisitSpinnerView;

    private TextView accountView;
    private TextView descriptionView;
    private TextView dateView;
    private TextView contactView;
    private TextView addressView;

    private EditText observationView;

    private Button completeView;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_complete);

        bind();
        refresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableGPS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void bind() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        accountView = findViewById(R.id.accountView);
        descriptionView = findViewById(R.id.descriptionView);
        dateView = findViewById(R.id.dateView);
        contactView = findViewById(R.id.contactView);
        addressView = findViewById(R.id.addressView);
        observationView = findViewById(R.id.observationView);
        typeVisitSpinnerView = findViewById(R.id.typeVisitSpinnerView);
        notVisitSpinnerView = findViewById(R.id.notVisitSpinnerView);

        completeView = findViewById(R.id.completeView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        completeView.setEnabled(false);

        try {
            activity = ActivityRepository.getInstance().findByCode(getIntent().getExtras().getString("activityCode"));
        } catch (Exception e) {
            AlertWidget.create().showError(this, e);
            Log.e(CallCompleteView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void refresh() {

        accountView.setText(activity.getAccountName());
        descriptionView.setText(activity.getDescription());
        dateView.setText(activity.getOrderDate());
        contactView.setText(activity.getContactDescription());
        addressView.setText(activity.getAddressDescription());

        for(int i=0; i<typeVisitSpinnerView.getCount();){
            if(typeVisitSpinnerView.getItemAtPosition(i).toString().equals(VisitConstants.Type.getDescription(VisitConstants.Type.valueOf(activity.getVisitType()))))
                typeVisitSpinnerView.setSelection(i);
            i++;
        }

        observationView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                completeView.setEnabled(!observationView.getText().toString().trim().isEmpty());
                activity.setResult(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    public void onSaveOrUpdate(View view) {
        completeView.setEnabled(false);

        activity.setReasonNotVisit(Activity.getVisitReasonNoVisitByDescrption(notVisitSpinnerView.getSelectedItem().toString()).name());
        activity.setVisitType(Activity.getVisitTypeByDescription(typeVisitSpinnerView.getSelectedItem().toString()).name());
        activity.setSyncStateType(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name());
        activity.setStateType("COMPLETED");

        ActivityService.getInstance().update(activity);

        final AlertWidget widget = AlertWidget.create();
        widget.showActivitySynchronize(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                widget.hide();
                finish();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityService.getInstance().onVisitUpdate(activity, new Callback<ResponseBody>(VisitCompleteView.this) {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {

                        UserService.getInstance().onActivitySynchronized();
                        ActivityService.getInstance().onActivitySynchronized(activity);

                        AndroidUtils.findViewById(R.id.syncLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.GONE);
                        AndroidUtils.findViewById(R.id.finishLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.VISIBLE);
                        AndroidUtils.findViewById(R.id.buttonOk, Button.class, widget.parentView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                widget.hide();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        AndroidUtils.findViewById(R.id.syncLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.GONE);
                        AndroidUtils.findViewById(R.id.errorLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.VISIBLE);
                        AndroidUtils.findViewById(R.id.buttonOk1, Button.class, widget.parentView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                widget.hide();
                                finish();
                            }
                        });
                    }
                });
            }
        }, 3000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.baseline_location_on_black_24);

        LatLng address =new LatLng(activity.getLat(),activity.getLng());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10000,0,this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,this);

        if(address.latitude != 0 && address.longitude != 0) {
            map.addMarker(new MarkerOptions().position(address)
                    .title(activity.getAccountName())
                    .snippet(activity.getAddressDescription()).icon(icon));
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        activity.setLat(location.getLatitude());
        activity.setLng(location.getLongitude());
//        observationView.setEnabled(location.getLatitude() != 0 && location.getLongitude() != 0);
        if(location.getLatitude() != 0 && location.getLongitude() != 0){
            notVisitSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position != 0){
                        observationView.setEnabled(false);
                        observationView.getText().clear();
                        activity.setResult(((AppCompatTextView) view).getText().toString());
                        completeView.setEnabled(true);
                    }else {
                        observationView.setEnabled(true);
                        completeView.setEnabled(false);
                        notVisitSpinnerView.setOnItemClickListener(null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        moveMap(new LatLng(location.getLatitude(),location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {}

    private void moveMap(LatLng coordinates) {
        if (map != null && coordinates != null)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 12));
    }

    private void enableGPS() {
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            buildAlertMessageNoGps();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Para continuar, activa la ubicación del dispositivo, que usa el servicio de ubicación")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        onBackPressed();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }
}