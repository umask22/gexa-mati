package ar.gexa.app.eecc.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.DateUtils;
import common.filter.Filter;
import common.filter.FilterUtils;
import common.models.constants.SyncConstants;

public class ActivitySearchView extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private DatePicker datePickerView;
    private Filter filter;
    private TextView activityCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        bind();
        onFilter();
    }

    @Override
    protected void onResume() {
        if(recyclerViewAdapter != null && recyclerViewAdapter.getItemCount() > 0)
            onFilter();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.filterView:
                onShowFilter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void onFilter() {
        try {
            recyclerViewAdapter.update(ActivityRepository.getInstance().findAllByFilter(filter));
            refresh();
        }catch (Exception e) {
            Log.e(ActivitySearchView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void  refresh() {
        if (recyclerView.getAdapter() != null)
            activityCount.setText(String.valueOf(recyclerView.getAdapter().getItemCount()));
    }

    private void onShowFilter() {
        final Calendar calendar = Calendar.getInstance();
        final View view = LayoutInflater.from(this).inflate(R.layout.date_search, null, false);

        datePickerView = AndroidUtils.findViewById(R.id.datePickerView, DatePicker.class, view);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(R.string.date_search_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                calendar.set(datePickerView.getYear(), datePickerView.getMonth(), datePickerView.getDayOfMonth());
                filter.params.clear();
                filter.params.add(FilterUtils.createQueryParam("dateFrom", DateUtils.toStringFrom(calendar.getTime(), DateUtils.Pattern.DD_MM_YY_HH_MM)));
                filter.params.add(FilterUtils.createQueryParam("dateUntil", DateUtils.toStringUntil(calendar.getTime(), DateUtils.Pattern.DD_MM_YY_HH_MM)));
                filter.params.add(FilterUtils.createQueryParam("stateType", "PENDING"));

                onFilter();
                dialog.dismiss();
            }

        });
        alertDialogBuilder.setNegativeButton(R.string.account_development_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    private void bind() {
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityCount = findViewById(R.id.activityCount);

        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView = findViewById(R.id.list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        filter = FilterUtils.create();
        filter.params.add(FilterUtils.createQueryParam("dateFrom", DateUtils.toStringFrom(new Date(), DateUtils.Pattern.DD_MM_YY_HH_MM)));
        filter.params.add(FilterUtils.createQueryParam("dateUntil", DateUtils.toStringUntil(new Date(), DateUtils.Pattern.DD_MM_YY_HH_MM)));
        filter.params.add(FilterUtils.createQueryParam("stateType", "PENDING"));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("doCall" , false);
        editor.apply();
    }

    private void onCompleteActivity(Activity activity) {
        final File from = new File(Environment.getExternalStorageDirectory() + "/gexa/calls");

        if (from.isDirectory()) {
            for (File child : from.listFiles()) {
                child.delete();
            }
        }

        Intent intent = null;
        if(activity.getType().equals("Call")) {

            intent = new Intent(ActivitySearchView.this, CallCompleteView.class);
        }else if(activity.getType().equals("Visit"))
            intent = new Intent(ActivitySearchView.this, VisitCompleteView.class);

        intent.putExtra("activityCode", activity.getCode());
        startActivity(intent);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<ActivitySearchItemView> {

        public List<Activity> actions = new ArrayList<>();

        @Override
        public int getItemCount() {
            return actions.size();
        }

        public void setActions(List<Activity> actions) {
            this.actions = actions;
        }

        public void update(final List<Activity> data) {
            if (data != null) {
                this.actions.clear();
                this.actions.addAll(data);
                this.notifyDataSetChanged();
            }
        }

        @Override
        public ActivitySearchItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_search_item, parent, false);
            return new ActivitySearchItemView(view);
        }
        @Override
        public void onBindViewHolder(final ActivitySearchItemView holder, int position) {

            holder.activity = actions.get(position);
            holder.activityDescriptionView.setText(holder.activity.getDescription());
            holder.accountNameView.setText(holder.activity.getAccountName());
            holder.activityScheduleDate.setText(holder.activity.getOrderDate());

            if(holder.activity.getTypeDescription() != null)
                holder.activityTypeView.setText(holder.activity.getTypeDescription());

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCompleteActivity(holder.activity);
                }
            });
        }

        public List<Activity> getActions() {
            return this.actions;
        }
    }
}