package ar.gexa.app.eecc.widget;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.ExceptionUtils;

public class AlertWidget {

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private TextView titleView;
    private TextView descriptionView;

    public View parentView;
    private View containerView;

    public static AlertWidget create() {
        return new AlertWidget();
    }

    private void bind(Context context, int layoutResourceId) {
        parentView = LayoutInflater.from(context).inflate(layoutResourceId, null, false);
        builder = new AlertDialog.Builder(context);
        builder.setView(parentView);

        titleView = AndroidUtils.findViewById(R.id.titleView, TextView.class, parentView);
        descriptionView = AndroidUtils.findViewById(R.id.descriptionView, TextView.class, parentView);
        containerView = AndroidUtils.findViewById(R.id.containerView, View.class, parentView);
    }

    public void showInfo(Context context, String title, String message) {

        bind(context, R.layout.alert_info);

        titleView.setText(title);
        descriptionView.setText(message);

        final Button btn = AndroidUtils.findViewById(R.id.btn, Button.class, parentView);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        onCreate();
    }

    public void showError(Context context, Throwable throwable) {
        showError(context, ExceptionUtils.getTitle(throwable), throwable.getMessage());
    }

    public void showError(Context context, String title, String description) {

        bind(context, R.layout.alert_error);

        titleView.setText(title);
        descriptionView.setText(description);

        final Button btn = AndroidUtils.findViewById(R.id.btn, Button.class, parentView);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        onCreate();
    }

    public void showLoading(Context context, String title, String description, View.OnClickListener cancelProcessView) {
        bind(context, R.layout.alert_loading);
        titleView.setText(title);
        descriptionView.setText(description);
        AndroidUtils.findViewById(R.id.cancelProcessView, Button.class,parentView).setOnClickListener(cancelProcessView);

        onCreate();
    }

    public void showActivitySynchronize(Context context, View.OnClickListener cancelProcessView) {
        bind(context, R.layout.alert_activity_synchronize);
        AndroidUtils.findViewById(R.id.cancelProcessView, Button.class,parentView).setOnClickListener(cancelProcessView);
        onCreate();
    }


    public void showYesOrNo(Context context, String title, String description, String nameButtonPositive, String nameButtonNegative, String nameButtonMedium, View.OnClickListener yesClickListener, View.OnClickListener noClickListener, View.OnClickListener postponeClickListener) {
        parentView = LayoutInflater.from(context).inflate(R.layout.alert_yes_no, null, false);
        builder = new AlertDialog.Builder(context);
        builder.setView(parentView);

        AndroidUtils.findViewById(R.id.titleView, TextView.class, parentView).setText(title);
        AndroidUtils.findViewById(R.id.descriptionView, TextView.class, parentView).setText(description);

        Button buttonYes = AndroidUtils.findViewById(R.id.yesView, Button.class, parentView);
        buttonYes.setText(nameButtonPositive);
        buttonYes.setOnClickListener(yesClickListener);

        Button buttonMedium = AndroidUtils.findViewById(R.id.postponeView, Button.class, parentView);
        buttonMedium.setText(nameButtonMedium);
        buttonMedium.setOnClickListener(postponeClickListener);

        Button buttonNo = AndroidUtils.findViewById(R.id.noView, Button.class, parentView);
        buttonNo.setText(nameButtonNegative);
        buttonNo.setOnClickListener(noClickListener != null ? noClickListener : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        onCreate();
    }

    private void onCreate() {
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void hide() {
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    public String contactDescription;
    public String mailDescription;

    public void showContactNew(Context context, Account account, View.OnClickListener saveClickListener, View.OnClickListener backClickListener) {

        parentView = LayoutInflater.from(context).inflate(R.layout.contact_new, null, false);
        builder = new AlertDialog.Builder(context);
        builder.setView(parentView);

        AndroidUtils.findViewById(R.id.accountNameView, TextView.class, parentView).setText(account.getName());
        AndroidUtils.findViewById(R.id.contactNumberView, TextView.class, parentView).setText(PhoneCallBO.getInstance().getNumber());
        AndroidUtils.findViewById(R.id.backView, Button.class, parentView).setOnClickListener(backClickListener);
        final Button saveView = AndroidUtils.findViewById(R.id.saveView, Button.class, parentView);
        saveView.setOnClickListener(saveClickListener);
        saveView.setEnabled(false);

        final EditText contactDescriptionView = AndroidUtils.findViewById(R.id.contactDescriptionView, EditText.class, parentView);
        contactDescriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveView.setEnabled(!contactDescriptionView.getText().toString().trim().isEmpty());
                contactDescription = contactDescriptionView.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
        final EditText mailDescriptionView = AndroidUtils.findViewById(R.id.mailDescriptionView, EditText.class, parentView);
        mailDescriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mailDescription = mailDescriptionView.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        onCreate();
    }
}
