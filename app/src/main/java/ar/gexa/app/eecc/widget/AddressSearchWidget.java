package ar.gexa.app.eecc.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import ar.gexa.app.eecc.utils.AndroidUtils;

public class AddressSearchWidget {

    public interface AddressSearchListener{
//        void onAddressSelected(Address address);
//        void onAddressNotSelected();
    }

    public static AddressSearchWidget newInstance(Context context, Spinner spinner) {
        return new AddressSearchWidget(context, spinner);
    }

    private AddressSearchListener listener;

    private Context context;

    private Spinner spinnerView;
//    private SpinnerAdapterView spinnerAdapterView;

//    private Address address;

    public AddressSearchWidget(Context context, Spinner spinnerView) {
        this.context = context;
        this.spinnerView = spinnerView;
//        bind();
    }

//    public void populate(List<Address> addresses) {
//        spinnerAdapterView.populate(addresses);
//    }
//
//    public void setAddress(Address address) {
//        this.address = address;
//
//        if(this.address != null) {
//            for(int i = 0; i < spinnerAdapterView.getAddresses().size(); i++) {
//                final Address obj = spinnerAdapterView.getAddresses().get(i);
//                if(obj.getId().equals(this.address.getId())) {
//                    spinnerView.setSelection(i);
//                    break;
//                }
//            }
//        }
//        spinnerAdapterView.notifyDataSetChanged();
//    }
//
//    public void setAddressSearchListener(AddressSearchListener listener) {
//        this.listener = listener;
//    }
//
//    private void bind() {
//
//        spinnerAdapterView = new SpinnerAdapterView();
//
//        spinnerView.setAdapter(spinnerAdapterView);
//        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                address = spinnerAdapterView.getAddresses().get(position);
//                listener.onAddressSelected(address);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                address = null;
//                listener.onAddressNotSelected();
//            }
//        });
//    }
//
//    private class SpinnerAdapterView extends ArrayAdapter<Address> {
//
//        private List<Address> addresses = new ArrayList<>();
//
//        public SpinnerAdapterView() {
//            super(context, R.layout.address_search_item);
//        }
//
//        public void populate(List<Address> addresses) {
//            this.addresses.clear();
//            this.addresses.addAll(addresses);
//        }
//
//        public List<Address> getAddresses() {
//            return addresses;
//        }
//
//        @Override
//        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            return onBindView(position, convertView, parent);
//        }
//
//        @Override
//        public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            return onBindView(position, convertView, parent);
//        }
//
//        @Nullable
//        @Override
//        public Address getItem(int position) {
//            return addresses.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return addresses.size();
//        }
//
//        public View onBindView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//
//            final Address address = addresses.get(position);
//
//            final View view = LayoutInflater.from(context).inflate(R.layout.address_search_item, parent, false);
//
//            TextView textView = AndroidUtils.findViewById(R.id.addressNameView, TextView.class, view);
//            textView.setText(address.getFirstLine());
//
//            textView = AndroidUtils.findViewById(R.id.addressValueView, TextView.class, view);
//            textView.setText(address.getState());
//
//            textView=AndroidUtils.findViewById(R.id.addressCpView,TextView.class, view);
//            textView.setText("CP "+address.getPostalCode());
//
//            textView=AndroidUtils.findViewById(R.id.addressProvinceView,TextView.class, view);
//            textView.setText(address.getProvince());
//
//            double lat = address.getLat();
//            double lng = address.getLng();
//
//            return view;
//        }
//    }
}
