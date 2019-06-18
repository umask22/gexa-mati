package ar.gexa.app.eecc.widget;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class ContactNewWidget extends DialogFragment {

    public interface ContactNewListener {
        void onContactCreated(Contact contact);
        void onContactNotCreated();
    }

    private FragmentManager fragmentManager;

    private ContactNewListener listener;

    private EditText contactDescriptionView;
    private EditText contactNumberView;
    private EditText contactObservationView;

    public static ContactNewWidget newInstance(FragmentManager fragmentManager) {
        final ContactNewWidget contactNewWidget = new ContactNewWidget();
        contactNewWidget.fragmentManager = fragmentManager;
        return contactNewWidget;
    }

    public void setListener(ContactNewListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.widget_contact_new, null, false);
        bind(view);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);

        alertDialogBuilder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                onSave();

            }
        });
        alertDialogBuilder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        contactDescriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!contactDescriptionView.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return dialog;
    }

    private void bind (View view) {

        contactDescriptionView = AndroidUtils.findViewById(R.id.contactDescriptionView,EditText.class,view);
        contactNumberView = AndroidUtils.findViewById(R.id.contactNumberView, EditText.class,view);
        contactObservationView = AndroidUtils.findViewById(R.id.contactObservationView,EditText.class,view);

        contactNumberView.setText(PhoneCallBO.getInstance().getNumber());
        contactNumberView.setEnabled(false);
    }

    public void onSave(){

//        final Contact contact = new Contact();
//        contact.setDescription(contactDescriptionView.getText().toString());
//        contact.setValue(contactNumberView.getText().toString());
//        contact.setObservation(contactObservationView.getText().toString());
//        contact.setAccount(AccountRepository.get().getAccount());
//
//        listener.onContactCreated(contact);
    }

    public void show() {

        show(fragmentManager, "");
        setCancelable(false);
    }
}

