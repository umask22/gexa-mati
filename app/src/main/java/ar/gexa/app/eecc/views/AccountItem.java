package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class AccountItem extends RecyclerView.ViewHolder {

    public final View view;
    public final TextView accountNameView;
    public final TextView cuitView;
    public final TextView relationshipView;
    public final TextView addressDescriptionView;

    public TextView addressTitle;


    public Account account;

    public AccountItem(View view) {

        super(view);
        this.view = view;
        relationshipView = AndroidUtils.findViewById(R.id.relationshipView, TextView.class, this.view);
        accountNameView = AndroidUtils.findViewById(R.id.accountNameView, TextView.class, this.view);
        cuitView=AndroidUtils.findViewById(R.id.cuitView,TextView.class,this.view);
        addressDescriptionView = AndroidUtils.findViewById(R.id.addressDescriptionView, TextView.class, this.view);
        addressTitle = AndroidUtils.findViewById(R.id.textView81, TextView.class, this.view);
    }
}
