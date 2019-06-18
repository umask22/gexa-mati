package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.utils.AndroidUtils;
import common.models.resources.AccountResource;

public class RouteSearchAddressItemView extends RecyclerView.ViewHolder {

    public View view;
    public TextView accountNameView;
    public TextView addressView;
    public TextView cuitView;
    public TextView nr;
//    public final TextView nextActivityView;


    public Account resource;

    public RouteSearchAddressItemView(View view) {

        super(view);
        this.view = view;
        nr = AndroidUtils.findViewById(R.id.relationshipView, TextView.class, this.view);
        accountNameView = AndroidUtils.findViewById(R.id.accountNameView, TextView.class, this.view);
        addressView = AndroidUtils.findViewById(R.id.addressNameView, TextView.class, this.view);
        cuitView = AndroidUtils.findViewById(R.id.cuitView, TextView.class, this.view);
    }
}