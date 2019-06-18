package ar.gexa.app.eecc.views;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Date;
import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.widget.DateSearchWidget;

public class ActivitySearchFilterView extends DialogFragment {

    interface Listener {
        void onFilter(String activityType, String activityScheduleDate);
    }

    private Listener listener;
    private FragmentManager fragmentManager;
    private Spinner activityTypeView;
    private TextView activityScheduleDateView;
    private DateSearchWidget dateSearchWidget;

    private Date selectedDate = new Date();

    public static ActivitySearchFilterView newInstance(FragmentManager fragmentManager) {
        final ActivitySearchFilterView accountDevelopmentView = new ActivitySearchFilterView();
        accountDevelopmentView.fragmentManager = fragmentManager;
        return accountDevelopmentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_filter, null, false);

        activityTypeView = AndroidUtils.findViewById(R.id.activityTypeView, Spinner.class, view);
        final ArrayAdapter<CharSequence> activityTypeAdapterView = ArrayAdapter.createFromResource(getActivity(), R.array.activity_search_filter_activityTypes, android.R.layout.simple_spinner_item );
        activityTypeAdapterView.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityTypeView.setAdapter(activityTypeAdapterView);

        dateSearchWidget = DateSearchWidget.newInstance(fragmentManager);
        dateSearchWidget.setDateSearchListener(new DateSearchWidget.DateSearchListener() {
            @Override
            public void onDateSelected(Date date) {
                selectedDate = date;
                refresh();
            }
            @Override
            public void onDateNotSelected() {
                selectedDate = new Date();
            }
        });

        final ImageButton activityScheduleDateSelectView = AndroidUtils.findViewById(R.id.activityScheduleDateSelectView, ImageButton.class, view);
        activityScheduleDateSelectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSearchWidget.show();
            }
        });

        activityScheduleDateView = AndroidUtils.findViewById(R.id.activityScheduleDateView, TextView.class, view);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.activity_search_filter_filter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onFilter(getType(String.valueOf(activityTypeView.getSelectedItem())), DateUtils.toString(selectedDate, DateUtils.Pattern.DD_MM_YY));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.activity_search_filter_back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        refresh();
        return builder.create();
    }

    public void doFilter(Listener listener) {
        this.listener = listener;
        show(fragmentManager, "");
    }

    private void refresh() {
        activityScheduleDateView.setText(DateUtils.toString(selectedDate, DateUtils.Pattern.DD_MM));
    }

    private String getType(String description) {
        if("Llamada".equals(description))
            return "Call";
        else if("Visita".equals(description))
            return "Visit";
        return null;
    }
}