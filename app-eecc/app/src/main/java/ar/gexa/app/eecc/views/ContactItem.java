package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class ContactItem extends RecyclerView.ViewHolder {

    public final View view;
    public final TextView contactDescription;
    public final TextView contactPhone;

    public Contact contact;

    public ContactItem(View view) {

        super(view);
        this.view = view;
        contactDescription = AndroidUtils.findViewById(R.id.contactDescriptionView, TextView.class, this.view);
        contactPhone = AndroidUtils.findViewById(R.id.contactPhoneView, TextView.class, this.view);
    }
}
