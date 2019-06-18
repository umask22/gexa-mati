package ar.gexa.app.eecc.views;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.ActivityService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.SyncConstants;
import common.models.constants.VisitConstants;
import okhttp3.ResponseBody;

public class RouteSearchView extends AppCompatActivity implements LocationListener, OnMapReadyCallback, SearchView.OnQueryTextListener {

    private LocationManager locationManager;

    private GoogleMap googleMap;
    private Map<String, Marker> markers = new HashMap<>();

    private RecyclerView addressRecyclerView;

    private double lat = 0.0;
    private double lng = 0.0;

    private AddressRecyclerAdapter addressRecyclerAdapter;

    private Account selectedItemAccount;

    private FloatingActionButton searchAccountFilterView;

    private SeekBar seekBar;
    private TextView dataSeekbar;
    private int initValueSeekbar = 2;

    private ConstraintLayout content;
    private ConstraintLayout constraintMap;

    private boolean mapsFullScreenBoolean;
    private TextView noAccounts;
    private TextView countAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_search);

        bind();
        MapsInitializer.initialize(getApplicationContext());
        content = findViewById(R.id.content);
        constraintMap = findViewById(R.id.constraintMap);
        mapsFullScreenBoolean = true;
//        NotificationService.getInstance().onStopLocatingUser(getApplicationContext());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableGPS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        GexaClient.getInstance().cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GexaClient.getInstance().cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.visit_search_menu, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mapsFullScreen:
                mapsFullScreen();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void mapsFullScreen(){

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(content);

        if (mapsFullScreenBoolean){
            constraintSet.connect(R.id.map, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(content);
            constraintMap.setVisibility(View.INVISIBLE);
            searchAccountFilterView.setVisibility(View.INVISIBLE);
            mapsFullScreenBoolean = false;
        }else {
            constraintSet.connect(R.id.map, ConstraintSet.BOTTOM, R.id.guideline3, ConstraintSet.TOP,0);
            constraintSet.applyTo(content);
            constraintMap.setVisibility(View.VISIBLE);
            searchAccountFilterView.setVisibility(View.VISIBLE);
            mapsFullScreenBoolean = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void onFilterShow() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialog = inflater.inflate(R.layout.maps_filter, null);

        seekBar = AndroidUtils.findViewById(R.id.seekBar, SeekBar.class, dialog);
        dataSeekbar = AndroidUtils.findViewById(R.id.value, TextView.class, dialog);
        seekBar.setProgress(initValueSeekbar);

        dialogBuilder.setView(dialog);
        dialogBuilder.setPositiveButton(R.string.activity_search_filter_filter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initValueSeekbar = seekBar.getProgress();
                markers.clear();
                onSearchAccount();
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(R.string.activity_search_filter_back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dataSeekbar.setText("Km: " + initValueSeekbar + "/" + seekBar.getMax());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                initValueSeekbar = i;
                dataSeekbar.setText("Km: " + i + "/" + seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        dialogBuilder.show();
    }

    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            moveCameraMaps(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };


    private void bind() {

        countAccount = findViewById(R.id.countAccount);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        searchAccountFilterView = findViewById(R.id.searchAccountView);
        noAccounts = findViewById(R.id.nullText);

        addressRecyclerAdapter = new AddressRecyclerAdapter();
        addressRecyclerView = findViewById(R.id.list);
        addressRecyclerView.addItemDecoration(new DividerItemDecoration(addressRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        addressRecyclerView.setAdapter(addressRecyclerAdapter);

        searchAccountFilterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterShow();
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {}
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);

        onSearchAccount();
    }

    private void enableGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            buildAlertMessageNoGps();
    }

    private void moveCameraMaps(LatLng coordinates) {
        if (googleMap != null && coordinates != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 12));
    }

    private void onShowPositionAccountsOnMap(List<Account> accounts, double radius, LatLng value) {
        googleMap.clear();

        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(0,new LatLng(-34.581438, -58.514871)); latLngs.add(1,new LatLng(-34.576737, -58.512625)); latLngs.add(2,new LatLng(-34.573269, -58.510997));
        latLngs.add(3,new LatLng(-34.569774, -58.509485)); latLngs.add(4,new LatLng(-34.567297, -58.507985)); latLngs.add(5,new LatLng(-34.564478, -58.506928));
        latLngs.add(6,new LatLng(-34.551472, -58.500778)); latLngs.add(7,new LatLng(-34.550270, -58.499987)); latLngs.add(8,new LatLng(-34.549572, -58.499190));
        latLngs.add(9,new LatLng(-34.548289, -58.496630)); latLngs.add(10,new LatLng(-34.543605, -58.486394)); latLngs.add(11,new LatLng(-34.541225, -58.480559));
        latLngs.add(12,new LatLng(-34.540377, -58.478731)); latLngs.add(13,new LatLng(-34.538843, -58.475701)); latLngs.add(14,new LatLng(-34.537949, -58.473412));
        latLngs.add(15,new LatLng(-34.535250, -58.467087)); latLngs.add(16,new LatLng(-34.534489, -58.465179)); latLngs.add(17,new LatLng(-34.534317, -58.463596));
        latLngs.add(18,new LatLng(-34.534489, -58.462325)); latLngs.add(19,new LatLng(-34.534818, -58.461544)); latLngs.add(20,new LatLng(-34.535675, -58.460632));
        latLngs.add(21,new LatLng(-34.536386, -58.459999)); latLngs.add(22,new LatLng(-34.537914, -58.458198)); latLngs.add(23,new LatLng(-34.538547, -58.457229));
        latLngs.add(24,new LatLng(-34.539148, -58.456025)); latLngs.add(25,new LatLng(-34.539625, -58.454902)); latLngs.add(26,new LatLng(-34.540279, -58.452695));
        latLngs.add(27,new LatLng(-34.540739, -58.451416)); latLngs.add(28,new LatLng(-34.542144, -58.448764)); latLngs.add(29,new LatLng(-34.543607, -58.446372));
        latLngs.add(30,new LatLng(-34.546788, -58.440659)); latLngs.add(31,new LatLng(-34.550581, -58.433759)); latLngs.add(32,new LatLng(-34.551648, -58.431903));
        latLngs.add(33,new LatLng(-34.550838, -58.429492)); latLngs.add(34,new LatLng(-34.552320, -58.426266)); latLngs.add(35,new LatLng(-34.553085, -58.424187));
        latLngs.add(36,new LatLng(-34.553933, -58.420646)); latLngs.add(37,new LatLng(-34.554469, -58.418201)); latLngs.add(38,new LatLng(-34.555115, -58.415863));
        latLngs.add(39,new LatLng(-34.555548, -58.414720)); latLngs.add(40,new LatLng(-34.556787, -58.412446)); latLngs.add(41,new LatLng(-34.559104, -58.409489));
        latLngs.add(42,new LatLng(-34.560511, -58.407770)); latLngs.add(43,new LatLng(-34.563793, -58.404170)); latLngs.add(44,new LatLng(-34.565003, -58.402002));
        latLngs.add(45,new LatLng(-34.565900, -58.401242)); latLngs.add(46,new LatLng(-34.566468, -58.401078)); latLngs.add(47,new LatLng(-34.567371, -58.400989));
        latLngs.add(48,new LatLng(-34.568021, -58.400870)); latLngs.add(49,new LatLng(-34.568568, -58.400345)); latLngs.add(50,new LatLng(-34.568743, -58.400341));
        latLngs.add(51,new LatLng(-34.568877, -58.400453)); latLngs.add(52,new LatLng(-34.569345, -58.400269)); latLngs.add(53,new LatLng(-34.570078, -58.399000));
        latLngs.add(54,new LatLng(-34.570079, -58.398985)); latLngs.add(55,new LatLng(-34.572416, -58.394920)); latLngs.add(56,new LatLng(-34.572259, -58.393286));
        latLngs.add(57,new LatLng(-34.572322, -58.392677)); latLngs.add(58,new LatLng(-34.576407, -58.385571)); latLngs.add(59,new LatLng(-34.576535, -58.385437));
        latLngs.add(60,new LatLng(-34.576727, -58.385370)); latLngs.add(61,new LatLng(-34.577110, -58.385363)); latLngs.add(62,new LatLng(-34.577612, -58.385477));
        latLngs.add(63,new LatLng(-34.577868, -58.385356)); latLngs.add(64,new LatLng(-34.578082, -58.384661)); latLngs.add(65,new LatLng(-34.578318, -58.383132));
        latLngs.add(66,new LatLng(-34.579551, -58.380900)); latLngs.add(67,new LatLng(-34.579811, -58.380299)); latLngs.add(68,new LatLng(-34.580008, -58.379532));
        latLngs.add(69,new LatLng(-34.579843, -58.376082)); latLngs.add(70,new LatLng(-34.590697, -58.386921)); latLngs.add(71,new LatLng(-34.591637, -58.387969));
        latLngs.add(72,new LatLng(-34.592857, -58.387637)); latLngs.add(73,new LatLng(-34.595650, -58.387216)); latLngs.add(74,new LatLng(-34.598354, -58.388240));
        latLngs.add(75,new LatLng(-34.599300, -58.386858)); latLngs.add(76,new LatLng(-34.599293, -58.388113)); latLngs.add(77,new LatLng(-34.599763, -58.395790));
        latLngs.add(78,new LatLng(-34.599677, -58.397174)); latLngs.add(79,new LatLng(-34.599691, -58.398759)); latLngs.add(80,new LatLng(-34.599310, -58.401952));
        latLngs.add(81,new LatLng(-34.598006, -58.404544)); latLngs.add(82,new LatLng(-34.598063, -58.410320)); latLngs.add(83,new LatLng(-34.597809, -58.415972));
        latLngs.add(84,new LatLng(-34.597702, -58.423423)); latLngs.add(85,new LatLng(-34.597858, -58.426688)); latLngs.add(86,new LatLng(-34.599706, -58.430218));
        latLngs.add(87,new LatLng(-34.602001, -58.432195)); latLngs.add(88,new LatLng(-34.605561, -58.439325)); latLngs.add(89,new LatLng(-34.607163, -58.445319));
        latLngs.add(90,new LatLng(-34.607573, -58.446243)); latLngs.add(91,new LatLng(-34.601765, -58.468901)); latLngs.add(92,new LatLng(-34.605257, -58.474036));
        latLngs.add(93,new LatLng(-34.600852, -58.477474)); latLngs.add(94,new LatLng(-34.600194, -58.476014)); latLngs.add(95,new LatLng(-34.599518, -58.478841));
        latLngs.add(96,new LatLng(-34.597663, -58.483267)); latLngs.add(97,new LatLng(-34.596699, -58.497140)); latLngs.add(98,new LatLng(-34.594664, -58.502711));
        latLngs.add(99,new LatLng(-34.593952, -58.503444)); latLngs.add(100,new LatLng(-34.588655, -58.508225)); latLngs.add(101,new LatLng(-34.581438, -58.514871));

        googleMap.addPolyline(new PolylineOptions().add(latLngs.get(0), latLngs.get(1), latLngs.get(2), latLngs.get(3), latLngs.get(4), latLngs.get(5)
                , latLngs.get(6), latLngs.get(7), latLngs.get(8), latLngs.get(9), latLngs.get(10), latLngs.get(11), latLngs.get(12), latLngs.get(13)
                , latLngs.get(14), latLngs.get(15), latLngs.get(16), latLngs.get(17), latLngs.get(18), latLngs.get(19), latLngs.get(20), latLngs.get(21)
                , latLngs.get(22), latLngs.get(23), latLngs.get(24), latLngs.get(25), latLngs.get(26), latLngs.get(27), latLngs.get(28), latLngs.get(29)
                , latLngs.get(30), latLngs.get(31), latLngs.get(32), latLngs.get(33), latLngs.get(34), latLngs.get(35)
                , latLngs.get(36), latLngs.get(37), latLngs.get(38), latLngs.get(39), latLngs.get(40), latLngs.get(41), latLngs.get(42), latLngs.get(43)
                , latLngs.get(44), latLngs.get(45), latLngs.get(46), latLngs.get(47), latLngs.get(48), latLngs.get(49), latLngs.get(50), latLngs.get(51)
                , latLngs.get(52), latLngs.get(53), latLngs.get(54), latLngs.get(55), latLngs.get(56), latLngs.get(57), latLngs.get(58), latLngs.get(59)
                , latLngs.get(60), latLngs.get(61), latLngs.get(62), latLngs.get(63), latLngs.get(64), latLngs.get(65), latLngs.get(66), latLngs.get(67)
                , latLngs.get(68), latLngs.get(69), latLngs.get(70), latLngs.get(71), latLngs.get(72), latLngs.get(73), latLngs.get(74), latLngs.get(75)
                , latLngs.get(76), latLngs.get(77), latLngs.get(78), latLngs.get(79), latLngs.get(80), latLngs.get(81), latLngs.get(82), latLngs.get(83)
                , latLngs.get(84), latLngs.get(85), latLngs.get(86), latLngs.get(87), latLngs.get(88), latLngs.get(89), latLngs.get(90), latLngs.get(91)
                , latLngs.get(92), latLngs.get(93), latLngs.get(94), latLngs.get(95), latLngs.get(96), latLngs.get(97), latLngs.get(98), latLngs.get(99)
                , latLngs.get(100), latLngs.get(101)).width(2).color(Color.BLACK));
        googleMap.addCircle(new CircleOptions().center(value).radius(radius * 1000).strokeWidth(1).strokeColor(Color.BLACK).fillColor(Color.argb(50, 50, 50, 50)));

        BitmapDescriptor markerBlack = BitmapDescriptorFactory.fromResource(R.drawable.baseline_location_on_black_24);

        if (accounts != null && accounts.size() > 0) {

            for (Account resource : accounts) {
                if (resource.lat != 0 && resource.lng != 0
                        && resource.lat != this.lat && resource.lng != this.lng) {
                    final LatLng latLng = new LatLng(resource.lat, resource.lng);

                    Marker mymarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng).title(resource.name)
                            .icon(markerBlack));
                    mymarker.setTag(resource.id);
                    markers.put("P:" + resource.id, mymarker);
                }
            }
        } else {
            for (String resourceId : markers.keySet()) {
                if (resourceId.contains("V:") && !resourceId.equals("V:" + this)) {
                    markers.get(resourceId).setVisible(false);
                }
            }
        }
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

    private void updatePosition(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    }

    private void onSearchAccount() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        updatePosition(location);

        final LatLng latLng = new LatLng(this.lat, this.lng);

        final List<Account> addresses = new ArrayList<>();

        for(Account account : AccountRepository.getInstance().findAll()){
            if (account.getLat() != 0 && account.getLng() != 0) {
                double distance = distance(account.getLat(), account.getLng(), this.lat, this.lng);
                if (distance < initValueSeekbar) {
                    addresses.add(account);
                }
            }
        }
        onShowPositionAccountsOnMap(addresses, initValueSeekbar,latLng);
        addressRecyclerAdapter.update(addresses);
        countAccount.setText(String.valueOf(addressRecyclerView.getAdapter().getItemCount()));
    }

    public static double distance(double latFrom, double lngFrom, double latTo, double lngTo) {
        double earthRadius = 6371;//kilometers
        double dLat = Math.toRadians(latTo - latFrom);
        double dLng = Math.toRadians(lngTo - lngFrom);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(latFrom)) * Math.cos(Math.toRadians(latTo));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    Activity activity = new Activity();

    public void createQuickVisit(final Account account) {

        activity.setUser(UserRepository.getInstance().find().getUsername());
        activity.setAccountCuit(String.valueOf(account.getCuit()));
        activity.setLat(this.lat);
        activity.setLng(this.lng);
        activity.setDescription("Visita mapeo");
        activity.setStateType(VisitConstants.StateType.COMPLETED.name());
        activity.setOrderDate(DateUtils.toString(new Date(), DateUtils.Pattern.DEFAULT));
        activity.setType("Visit");
        activity.setAccountName(account.getName());
        activity.setCode("V:" + account.getCuit() + ":" + DateUtils.toString(new Date(), DateUtils.Pattern.CODE));

        final AlertDialog.Builder builder = new AlertDialog.Builder(RouteSearchView.this);
        View view = RouteSearchView.this.getLayoutInflater().inflate(R.layout.visit_fast_observation, null);

        TextView accountNameView = AndroidUtils.findViewById(R.id.accountNameView, TextView.class, view);
        TextView dateView = AndroidUtils.findViewById(R.id.dateView, TextView.class, view);

        final Spinner spinner = AndroidUtils.findViewById(R.id.typeVisitSpinnerView, Spinner.class, view);
        final EditText observationView = AndroidUtils.findViewById(R.id.observationView, EditText.class, view);

        accountNameView.setText(account.getName());
        dateView.setText(DateUtils.toString(new Date(), DateUtils.Pattern.DD_MM_YY_HH_MM));
        builder.setView(view);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.setVisitType(Activity.getVisitTypeByDescription(spinner.getSelectedItem().toString()).name());
                onSave(activity);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        observationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!observationView.getText().toString().trim().isEmpty());
                activity.setResult(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void onSave(final Activity activity) {
        ActivityService.getInstance().save(activity);

        final AlertWidget widget = AlertWidget.create();
        widget.showActivitySynchronize(RouteSearchView.this, new View.OnClickListener() {
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
                ActivityService.getInstance().onVisitSave(activity, new Callback<ResponseBody>(RouteSearchView.this) {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {

                        UserService.getInstance().onActivitySynchronized();

                        try {
                            ActivityService.getInstance().onActivitySynchronized(ActivityRepository.getInstance().findByCode(activity.getCode()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AndroidUtils.findViewById(R.id.syncLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.GONE);
                        AndroidUtils.findViewById(R.id.finishLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.VISIBLE);
                        AndroidUtils.findViewById(R.id.textView121, TextView.class, widget.parentView).setText("Enviado");
                        AndroidUtils.findViewById(R.id.buttonOk, Button.class, widget.parentView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                widget.hide();
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
                            }
                        });
                    }
                });
            }
        }, 3000);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        addressRecyclerAdapter.getFilter().filter(TextUtils.isEmpty(query) ? null : query);
        return true;
    }


    private class AddressRecyclerAdapter extends RecyclerView.Adapter<RouteSearchAddressItemView> {

        private FilterAccount filter;
        private List<Account> addresses = new ArrayList<>();
        private List<Account> filtereds = new ArrayList<>();

        public void update(final List<Account> data) {
            if (data != null) {
                this.addresses.clear();
                this.addresses.addAll(data);

                this.filtereds.clear();
                this.filtereds.addAll(data);
                notifyDataSetChanged();

                if (this.addresses.size() > 0)
                    noAccounts.setText(null);
                else
                    noAccounts.setText("no se encontraron cuentas dentro del radio de "+ initValueSeekbar +" km");
            }
        }

        @Override
        public RouteSearchAddressItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.account_address_item, parent, false);
            return new RouteSearchAddressItemView(view);
        }

        @Override
        public void onBindViewHolder(final RouteSearchAddressItemView holder, final int position) {
            final BitmapDescriptor markerBlack = BitmapDescriptorFactory.fromResource(R.drawable.baseline_location_on_black_24);
            final BitmapDescriptor markerBlue = BitmapDescriptorFactory.fromResource(R.drawable.baseline_location_on_black_24_blue);
            holder.resource = addresses.get(position);
            holder.accountNameView.setText(holder.resource.name);
            holder.addressView.setText(holder.resource.addressDescription);
            holder.cuitView.setText(holder.resource.cuit);
            holder.nr.setText(holder.resource.nr);

            if (selectedItemAccount != null) {
                if (holder.resource.id.equals(selectedItemAccount.id)) {
                    holder.view.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_dark));
                    holder.accountNameView.setTextColor(getResources().getColor(android.R.color.white));
                    holder.addressView.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    holder.view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    holder.accountNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    holder.addressView.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                }
            }

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    int pos = 0;
                    for (Account account : addresses) {
                        if (marker.getTag().equals(account.getId())){
                            if (selectedItemAccount != addresses.get(pos)) {

                                mapsFullScreenBoolean = false;
                                mapsFullScreen();

                                selectedItemAccount = addresses.get(pos);
                                Marker markerClick = markers.get("P:" + selectedItemAccount.id);
                                markerClick.setIcon(markerBlue);
                                addressRecyclerView.smoothScrollToPosition(pos);
                                addressRecyclerAdapter.notifyDataSetChanged();

                                for (String resourceId : markers.keySet()) {
                                    if (!resourceId.equals("P:" + selectedItemAccount.id)) {
                                        markerClick = markers.get(resourceId);
                                        markerClick.setIcon(markerBlack);
                                    }
                                }
                            }
                        }
                        pos++;
                    }
                    return false;
                }
            });

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (selectedItemAccount != holder.resource) {
                        selectedItemAccount = holder.resource;
                        if (holder.resource.lat != 0 && holder.resource.lng != 0)
                            moveCameraMaps(new LatLng(holder.resource.lat, holder.resource.lng));
                        LatLng latlng = new LatLng(holder.resource.lat, holder.resource.lng);

                        Marker marker = markers.get("P:" + selectedItemAccount.id);
                        marker.setIcon(markerBlue);

                        for (String resourceId : markers.keySet()) {
                            if (resourceId.contains("P:") && !resourceId.equals("P:" + selectedItemAccount.id)) {
                                marker = markers.get(resourceId);
                                marker.setIcon(markerBlack);
                            }
                        }
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16));
                        addressRecyclerAdapter.notifyDataSetChanged();
                        locationManager.removeUpdates(locationListener);
                    } else {
                        selectedItemAccount = null;
                        holder.view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        holder.accountNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        holder.addressView.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            return;
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                        onSearchAccount();
                        addressRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            });

            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectedItemAccount = holder.resource;
                    addressRecyclerAdapter.notifyDataSetChanged();
                    createQuickVisit(holder.resource);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        public Filter getFilter() {
            if(filter == null)
                filter = new FilterAccount();
            return filter;
        }

        private class FilterAccount extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (constraint != null && constraint.length() > 0) {
                    final List<Account> filtered = new ArrayList<>();
                    for (Account account : filtereds) {
                        if (account.name.toLowerCase().contains(constraint.toString().toLowerCase()))
                            filtered.add(account);
                        if(account.cuit.toLowerCase().contains(constraint.toString().toLowerCase()))
                            filtered.add(account);
                    }
                    filterResults.count = filtered.size();
                    filterResults.values = filtered;
                } else {
                    filterResults.count = filtereds.size();
                    filterResults.values = filtereds;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                addresses = (List<Account>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}