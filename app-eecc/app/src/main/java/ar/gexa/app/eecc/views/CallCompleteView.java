package ar.gexa.app.eecc.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.ActivityService;
import ar.gexa.app.eecc.services.NotificationService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.FileUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import ar.gexa.app.eecc.widget.ContactSearchWidget;
import common.models.constants.CallConstants;
import okhttp3.ResponseBody;

public class CallCompleteView extends AppCompatActivity {


    private View callCompleteView;

    private TextView accountView;
    private TextView descriptionView;
    private TextView dateView;

    private EditText observationView;

    private Button saveView;

    private Activity activity;
    private ArrayList<Contact> contactList;

    private boolean callButton = true;
    private boolean shouldAllowBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            activity = ActivityRepository.getInstance().findByCode(getIntent().getExtras().getString("activityCode"));
            bind();
            refresh();
        } catch  (Exception e) {
            NotificationService.getInstance().txtLog("CallComplete"+e.getMessage());
            AlertWidget.create().showError(this, e);
            Log.e(CallCompleteView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldAllowBack) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("doCall", false)) {
                    shouldAllowBack = true;
                    onBackPressed();
                }else {
                    //si la actividad se llamo desde la web
                    finishAndRemoveTask();
                }
                return true;
            case R.id.doCallView:
                if(callButton) {
                    callButton = false;
                    onCallDo();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bind() {
        setContentView(R.layout.call_complete);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactList = new ArrayList<>();
        accountView = findViewById(R.id.accountView);
        descriptionView = findViewById(R.id.descriptionView);
        dateView = findViewById(R.id.dateView);
        observationView = findViewById(R.id.observationView);
        callCompleteView = findViewById(R.id.callCompleteView);
        saveView = findViewById(R.id.saveView);

        final ContactSearchWidget contactSearchWidget = ContactSearchWidget.newInstance(this, AndroidUtils.findViewById(R.id.contactSearchView, Spinner.class, this));

        if(activity.getContactId() != null) {
            contactSearchWidget.populate(ContactRepository.getInstance().findAllByCuit(activity.getAccountCuit()));
            List<Contact> contacts = ContactRepository.getInstance().findAllByCuit(activity.getAccountCuit());
            for (Contact contact : contacts) {
                if (activity.getContactId().equals(contact.code)) {
                    try {
                        contactSearchWidget.setContact(ContactRepository.getInstance().findByCode(String.valueOf(contact.getCode())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Contact newContact = new Contact();
            newContact.setPhone(activity.getContactPhone());
            newContact.setDescription(activity.getContactDescription());
            newContact.setMail(activity.getContactMail());
            newContact.setGexa(true);
            contactList.add(newContact);
            contactSearchWidget.populate(contactList);
            contactSearchWidget.setContact(contactList.get(0));
        }
        contactSearchWidget.setContactSearchListener(new ContactSearchWidget.ContactSearchListener() {
            @Override
            public void onContactSelected(Contact contact) {
                if(contact.getCode() != null) {
                    activity.setContactId(contact.getCode());
                    activity.setContactPhone(contact.getPhone());
                    activity.setContactDescription(contact.getDescription());
                }else {
                    activity.setContactPhone(contact.getPhone());
                    activity.setContactDescription(contact.getDescription());
                }

                if(activity.getCallType() == null) {
                    try {
                        ActivityRepository.getInstance().update(activity);
                    } catch (Exception e) {
                        NotificationService.getInstance().txtLog(CallCompleteView.class.getSimpleName() + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onContactNotSelected() {
                saveView.setEnabled(false);
            }
        });
    }

    private void refresh() {
        accountView.setText(activity.getAccountName());
        descriptionView.setText(activity.getDescription());
        dateView.setText(activity.getOrderDate());

        if(CallConstants.Type.INCOMING.name().equals(activity.getCallType()) || CallConstants.Type.OUTGOING.name().equals(activity.getCallType())) {
            onCallEffective();
            callButton = false;
            shouldAllowBack = false;
        }
    }

    public void onCallDo() {
        PhoneCallBO.getInstance().setCallFromGexa(true);

        final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + activity.getContactPhone()));
//        final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "1121649784"));

        activity.setCallType(CallConstants.Type.OUTGOING.name());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PhoneCallBO.getInstance().setListener(new PhoneCallBO.Listener() {
            @Override
            public void onOutgoingCallEnded() {
                CallCompleteView.this.onOutgoingCallEnded();
            }
        });
        startActivity(intent);
    }

    public void onOutgoingCallEnded() {
        PhoneCallBO.getInstance().setCallFromGexa(false);
        final AlertWidget widget = AlertWidget.create();
        widget.showYesOrNo(this, "completar llamada", "¿Fue efectiva la llamada realizada?","Si", "No","Reagendar", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallEffective();
                callButton = false;
                shouldAllowBack = false;
                widget.hide();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callButton = true;
                onCallUnEffective();
                widget.hide();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                widget.hide();

                activity.setStateType(CallConstants.StateType.CANCELED.name());
                ActivityService.getInstance().update(activity);

                widget.showLoading(CallCompleteView.this, "Reagendado", "Reagendando... Aguarde unos instantes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        widget.hide();
                    }
                });

                GexaClient.getInstance().onMoveActivityToTomorrowApp(activity.getCode(), new Callback<ResponseBody>(CallCompleteView.this) {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        widget.hide();
                        finish();
                    }
                });
            }
        });
    }

    private void onCallEffective() {

        PhoneCallBO.getInstance().onCallSuccess(activity.getCode());

        callCompleteView.setVisibility(View.VISIBLE);
        observationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveView.setEnabled(!observationView.getText().toString().trim().isEmpty());
                activity.setResult(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void onCallUnEffective() {
        PhoneCallBO.getInstance().onCallUnsuccess();
        if(CallConstants.Type.INCOMING.name().equals(activity.getCallType()) && CallConstants.StateType.COMPLETED.name().equals(activity.getStateType()))
            ActivityService.getInstance().delete(activity);
    }

    public void onSaveOrUpdate(View view) {
        saveView.setEnabled(false);

        activity.setStateType(CallConstants.StateType.COMPLETED.name());
        ActivityService.getInstance().update(activity);

        final AlertWidget widget = AlertWidget.create();
        widget.showActivitySynchronize(CallCompleteView.this, new View.OnClickListener() {
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
                ActivityService.getInstance().onActivitySynchronize(activity, new Callback<ResponseBody>(CallCompleteView.this) {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {

                        UserService.getInstance().onActivitySynchronized();
                        ActivityService.getInstance().onActivitySynchronized(activity);

                        AndroidUtils.findViewById(R.id.syncLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.GONE);
                        AndroidUtils.findViewById(R.id.finishLayout, ConstraintLayout.class, widget.parentView).setVisibility(View.VISIBLE);
                        AndroidUtils.findViewById(R.id.buttonOk, Button.class, widget.parentView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FileUtils.delete(Environment.getExternalStorageDirectory() + "/gexa/" + activity.getCode() + ".json");
                                FileUtils.delete(Environment.getExternalStorageDirectory() + "/gexa/calls/" + activity.getCode() + ".3gp");
                                if(CallConstants.Type.INCOMING.name().equals(activity.getCallType()) || CallConstants.Type.OUTGOING.name().equals(activity.getCallType())) {
                                    widget.hide();
                                    finishAndRemoveTask();
                                }else{
                                    widget.hide();
                                    finish();
                                }
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
                                if(CallConstants.Type.INCOMING.name().equals(activity.getCallType())) {
                                    widget.hide();
                                    finishAndRemoveTask();
                                }else{
                                    widget.hide();
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        }, 3000);
    }
}