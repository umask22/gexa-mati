package ar.gexa.app.eecc.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.NotificationService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.FileUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.SyncConstants;
import okhttp3.ResponseBody;

public class SynchronizationView extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private User user;

    private TextView activityCount;
    private Button synchronizeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synchronization);

        bind();
        onFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onFilter();
    }

    private void onFilter() {

        try {
            recyclerViewAdapter.update(ActivityRepository.getInstance().findAllBySyncState(SyncConstants.StateType.PENDING_SYNCHRONIZATION.name()
                    , SyncConstants.StateType.SYNCHRONIZED.name(), "COMPLETED"));
            if (recyclerView.getAdapter() != null) {
                activityCount.setText(String.valueOf(recyclerView.getAdapter().getItemCount()));
            }
        } catch (Exception e) {
            Log.e(ActivitySearchView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void bind() {
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activityCount = findViewById(R.id.activityCount);
        synchronizeView = findViewById(R.id.synchronizeView);

        user = UserRepository.getInstance().find();
        AndroidUtils.findViewById(R.id.syncStateView, TextView.class, this).setText(SyncConstants.StateType.getDescription(SyncConstants.StateType.valueOf(user.getDeviceSyncStateType())));

        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView = findViewById(R.id.list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onSynchronize(View view) {
        final AlertWidget widget = AlertWidget.create();
        widget.showYesOrNo(SynchronizationView.this, "sincronizar", "¿Esta seguro de sincronizar?", "SI", "No", "", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                widget.hide();

                widget.showLoading(SynchronizationView.this, "Sincronizacion", "Sincronizacion en progreso. Aguarde unos instantes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        widget.hide();
                        finish();
                    }
                });

                GexaClient.getInstance().synchronizationRequest(user.getUsername(), new Callback<Void>(getApplicationContext()) {
                    @Override
                    public void onSuccess(Void responseBody) {
                        NotificationService.getInstance().getAllFilesInDir();
                        FileUtils.createDirectory("gexa/calls");
                        widget.hide();
                        finish();
                    }
                });
            }
        }, null,null);
        AndroidUtils.findViewById(R.id.postponeView, Button.class, widget.parentView).setVisibility(View.GONE);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<SynchronizationItemView> {

        private List<Activity> activities = new ArrayList<>();

        @Override
        public int getItemCount() {
            return activities.size();
        }

        public void update(final List<Activity> data) {
            if (data != null) {
                this.activities.clear();
                this.activities.addAll(data);
                this.notifyDataSetChanged();
            }
        }

        @Override
        public SynchronizationItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.synchronization_item, parent, false);
            return new SynchronizationItemView(view);
        }
        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(final SynchronizationItemView holder, int position) {

            holder.activity = activities.get(position);
            holder.activityDescriptionView.setText(holder.activity.getDescription());
            holder.accountNameView.setText(holder.activity.getAccountName());
            holder.activityScheduleDateView.setText(holder.activity.getOrderDate());
            holder.activityTypeView.setText(holder.activity.getTypeDescription());
            if(SyncConstants.StateType.SYNCHRONIZED.name().equals(holder.activity.getSyncStateType())){
                holder.syncStateView.setText(SyncConstants.StateType.getDescription(SyncConstants.StateType.valueOf(holder.activity.getSyncStateType())));
                holder.syncStateView.setBackgroundResource(R.color.holo_green_dark);
            }else {
                holder.syncStateView.setText(SyncConstants.StateType.getDescription(SyncConstants.StateType.valueOf(holder.activity.getSyncStateType())));
            }
            if(!"CANCELED".equals(holder.activity.getStateType())){
                holder.stateTypeView.setVisibility(View.GONE);
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SynchronizationView.this, SyncActivityCompletedView.class);
                    if(holder.activity.getType().equals("Call")) {
                        intent.putExtra("activityCode", holder.activity.getCode());
                        startActivity(intent);
                    }else if(holder.activity.getType().equals("Visit")){
                        intent.putExtra("activityCode", holder.activity.getCode());
                        startActivity(intent);
                    }
                }
            });
        }

        public List<Activity> getActivities() {
            return this.activities;
        }
    }
}