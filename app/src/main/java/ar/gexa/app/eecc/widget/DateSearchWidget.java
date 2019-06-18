package ar.gexa.app.eecc.widget;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class DateSearchWidget extends DialogFragment {

    public interface DateSearchListener {
        void onDateSelected(Date date);
        void onDateNotSelected();
    }

    private FragmentManager fragmentManager;

    private DatePicker datePickerView;

    private DateSearchListener listener;

    private static DateSearchWidget instance;

    public static DateSearchWidget newInstance(FragmentManager fragmentManager) {
        if(instance == null)
            instance = new DateSearchWidget();

        instance.fragmentManager = fragmentManager;

        return instance;
    }

    public void setDateSearchListener(DateSearchListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.date_search, null, false);

        bind(view);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(R.string.date_search_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Calendar calendar = Calendar.getInstance();
                calendar.set(datePickerView.getYear(), datePickerView.getMonth(), datePickerView.getDayOfMonth());
                listener.onDateSelected(calendar.getTime());
                dialog.dismiss();
            }

        });
        alertDialogBuilder.setNegativeButton(R.string.account_development_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            listener.onDateNotSelected();
            dialog.dismiss();
            }

        });

        return alertDialogBuilder.create();
    }

    private void bind(View view) {
        datePickerView = AndroidUtils.findViewById(R.id.datePickerView, DatePicker.class, view);
    }

    public void show() {
        show(fragmentManager, "");
    }
}
