package ar.gexa.app.eecc.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class ContactSearchWidget {

    public interface ContactSearchListener {
        void onContactSelected(Contact resource);
        void onContactNotSelected();
    }

    public static ContactSearchWidget newInstance(Context context, Spinner spinner) {
        return new ContactSearchWidget(context, spinner);
    }

    private ContactSearchListener listener;

    private Context context;

    private Spinner spinnerView;
    private SpinnerAdapterView spinnerAdapterView;

    private Contact contact;

    public ContactSearchWidget(Context context, Spinner spinnerView) {
        this.context = context;
        this.spinnerView = spinnerView;
        bind();
    }


    public void populate(List<Contact> contacts) {
        spinnerAdapterView.populate(contacts);
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        if(this.contact != null) {
            for(int i = 0; i < spinnerAdapterView.getContacts().size(); i++) {
                final Contact obj = spinnerAdapterView.getContacts().get(i);
                if(obj.getId() != null) {
                    if (obj.getId().equals(this.contact.getId()))
                        spinnerView.setSelection(i);
                }else
                    spinnerView.setSelection(i);
            }
        }
        spinnerAdapterView.notifyDataSetChanged();
    }

    public void setContactSearchListener(ContactSearchListener listener) {
        this.listener = listener;
    }

    private void bind() {

        spinnerAdapterView = new SpinnerAdapterView();

        spinnerView.setAdapter(spinnerAdapterView);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                contact = spinnerAdapterView.getContacts().get(position);
                listener.onContactSelected(contact);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                contact = null;
                listener.onContactNotSelected();
            }
        });

    }

    private class SpinnerAdapterView extends ArrayAdapter<Contact> {

        private List<Contact> contacts = new ArrayList<>();

        public SpinnerAdapterView() {
            super(context, R.layout.contact_search_item);
        }

        public void populate(List<Contact> contacts) {
            this.contacts.clear();
            this.contacts.addAll(contacts);
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        @Override
        public View getDropDownView(int position,View convertView, ViewGroup parent) {
            return onBindView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView,ViewGroup parent) {
            return onBindView(position, convertView, parent);
        }

        @Override
        public Contact getItem(int position) {
            return contacts.get(position);
        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        public View onBindView(int position, View convertView, ViewGroup parent) {

            final Contact contact = contacts.get(position);

            final View view = LayoutInflater.from(context).inflate(R.layout.contact_search_item, parent, false);

            TextView textView = AndroidUtils.findViewById(R.id.contactDescriptionView, TextView.class, view);
            textView.setText(contact.getDescription());

            textView = AndroidUtils.findViewById(R.id.contactTypeView, TextView.class, view);
            if(contact.getTypeDescription() != null)
                textView.setText(contact.getTypeDescription());
            else
                textView.setVisibility(View.GONE);

            textView = AndroidUtils.findViewById(R.id.contactPhoneView, TextView.class, view);
            textView.setText(contact.getPhone());
            return view;
        }
    }
}
