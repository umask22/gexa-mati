package ar.gexa.app.eecc.views;

import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.services.ActivityService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.SyncConstants;
import okhttp3.ResponseBody;

public class SyncActivityCompletedView extends AppCompatActivity{

    private TextView accountView;
    private TextView syncStateView;
    private TextView descriptionView;
    private TextView dateView;

    private TextView observationView;
    private View callCompleteView;
    private Button saveView;

    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_activity_complete);

        bind();
        refresh();
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

    private void refresh() {
        if (SyncConstants.StateType.SYNCHRONIZED.name().equals(activity.getSyncStateType())){
            syncStateView.setText(SyncConstants.StateType.getDescription(SyncConstants.StateType.valueOf(activity.getSyncStateType())));
            syncStateView.setBackgroundResource(R.color.holo_green_dark);
        }else {
            syncStateView.setText(SyncConstants.StateType.getDescription(SyncConstants.StateType.valueOf(activity.getSyncStateType())));
        }
        accountView.setText(activity.getAccountName());
        descriptionView.setText(activity.getDescription());
        dateView.setText(activity.getOrderDate());
        observationView.setText(activity.getResult());


        PhoneCallBO.getInstance().onCallSuccess(activity.getCode());

        callCompleteView.setVisibility(View.VISIBLE);
    }

    public void onUpdate(View view) {
        saveView.setEnabled(false);
        ActivityService.getInstance().update(activity);

        final AlertWidget widget = AlertWidget.create();
        widget.showActivitySynchronize(SyncActivityCompletedView.this, new View.OnClickListener() {
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
                ActivityService.getInstance().onActivitySynchronize(activity, new Callback<ResponseBody>(SyncActivityCompletedView.this) {
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
                                finishAndRemoveTask();
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
                                finishAndRemoveTask();
                            }
                        });
                    }
                });
            }
        }, 3000);
    }

    private void bind() {

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        syncStateView = findViewById(R.id.syncStateView);
        accountView = findViewById(R.id.accountView);
        descriptionView = findViewById(R.id.descriptionView);
        dateView = findViewById(R.id.dateView);
        observationView = findViewById(R.id.observationView);
        callCompleteView = findViewById(R.id.callCompleteView);

        saveView = findViewById(R.id.saveView);

        try {
            activity = ActivityRepository.getInstance().findByCode(getIntent().getExtras().getString("activityCode"));
        } catch (Exception e) {
            AlertWidget.create().showError(this, e);
            Log.e(CallCompleteView.class.getSimpleName(), e.getMessage(), e);
        }

//        final ContactSearchWidget contactSearchWidget = ContactSearchWidget.newInstance(this, AndroidUtils.findViewById(R.id.contactSearchView, Spinner.class, this));

//        contactSearchWidget.populate(ContactRepository.getInstance().findAllByCuit(activity.getAccountCuit()));
//        List<Contact> contacts = ContactRepository.getInstance().findAllByCuit(activity.getAccountCuit());
//        for(Contact contact : contacts){
//            if(activity.getContactId().equals(contact.code)){
//                try {
//                    contactSearchWidget.setContact(ContactRepository.getInstance().findByCode(String.valueOf(contact.getCode())));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        contactSearchWidget.setContactSearchListener(new ContactSearchWidget.ContactSearchListener() {
//            @Override
//            public void onContactSelected(Contact contact) {
//                if(contact != null) {
//                    activity.setContactId(contact.getCode());
//                    activity.setContactPhone(contact.getPhone());
//                    activity.setContactDescription(contact.getDescription());
//                    try {
//                        ActivityRepository.getInstance().update(activity);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            @Override
//            public void onContactNotSelected() {
//                saveView.setEnabled(false);
//            }
//        });
    }
}
