package ar.gexa.app.eecc.views;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.SearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.bo.PhoneCallBO;
import ar.gexa.app.eecc.models.Account;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.models.Contact;
import ar.gexa.app.eecc.repository.AccountRepository;
import ar.gexa.app.eecc.repository.ActivityRepository;
import ar.gexa.app.eecc.repository.ContactRepository;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.constants.CallConstants;
import common.models.constants.CommonConstants;

public class CallSuccess extends AppCompatActivity implements  SearchView.OnQueryTextListener{

    public AlertWidget widget = AlertWidget.create();
    public String eightNumbers;

    private RecyclerViewAdapter recyclerViewAdapter;

    private RecyclerViewAdapterAccount recyclerViewAdapterAccount;

    private Account selectedItemAccount;
    public ArrayList<Account> accountsFindByNumber;
    public ArrayList<Contact> contactsSelect;

    private String codeDate;
    private String orderDate;

    public Activity activity;
    final File from = new File(Environment.getExternalStorageDirectory() + "/gexa/calls/last.3gp");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        codeDate = DateUtils.toString(new Date(), DateUtils.Pattern.CODE);
        orderDate = DateUtils.toString(new Date(), DateUtils.Pattern.DEFAULT);

        dialogBind();
        PhoneCallBO.getInstance().copyFileFromACR(this,"last");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 3000);
    }

    public void dialogBind(){
        widget.showLoading(this, "Espere" , "Aguarde unos instantes...",null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_search, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    public void init(){
        widget.hide();

        String number = PhoneCallBO.getInstance().getNumber();
        eightNumbers = number.substring(number.length() - 8);

        activity = new Activity();

        activity.setOrderDate(orderDate);
        activity.setUser(UserRepository.getInstance().find().getUsername());
        activity.setType("Call");
        activity.setStateType(CallConstants.StateType.COMPLETED.name());
        activity.setPriorityType(CommonConstants.PriorityType.HIGH.name());
        activity.setPriority(CommonConstants.PriorityType.getDescription(CommonConstants.PriorityType.HIGH));

        if(PhoneCallBO.getInstance().callIn_callOut.equals("callIn")) {
            activity.setDescription("Llamada entrante");
            activity.setCallType(CallConstants.Type.INCOMING.name());
            proccessContacts();
        }
        else if(PhoneCallBO.getInstance().callIn_callOut.equals("callOut")) {
            activity.setDescription("Llamada saliente");
            activity.setCallType(CallConstants.Type.OUTGOING.name());

            widget.showYesOrNo(this, "Llamada saliente", "¿Fue una llamada laboral efectiva?", "Continuar", "", "Ignorar", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    widget.hide();
                    proccessContacts();
                }
            }, null, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (from.exists())
                        from.delete();
                    widget.hide();
                    finishAndRemoveTask();
                }
            });
        }
    }

    public void proccessContacts(){
        try {

            List<Contact> contacts = ContactRepository.getInstance().findAllByNumberTwo("%"+eightNumbers);
            accountsFindByNumber = new ArrayList<>();
            contactsSelect = new ArrayList<>();
            if(contacts.size() > 1) {
                //Varios contactos con el mismo numero
                String cuitContact = contacts.get(0).getAccountCuit();
                for (final Contact contact : contacts) {

                    if(cuitContact.equals(contact.getAccountCuit())){
                        cuitContact = contact.getAccountCuit();
                        if(contact == contacts.get(contacts.size()-1)) {
                            activity.setContactId(contact.getCode());
                            activity.setContactPhone(contact.getPhone());
                            activity.setContactDescription(contact.getDescription());
                            activity.setContactMail(contact.getMail());
                            activity.setCode("C:" + contact.getAccountCuit() + ":" + codeDate);
                            activity.setAccountCuit(contact.getAccountCuit());
                            activity.setAccountName(contact.getAccountName());

                            ActivityRepository.getInstance().save(activity);

                            ignoredOrComplete(contact.getAccountCuit(),contact);
                        }
                    }else{
                        for(Contact contactCuit : contacts) {
                            Account account = AccountRepository.getInstance().findByCUIT(contactCuit.getAccountCuit());
                            if (accountsFindByNumber.size() != 0){
                                if (!account.getCuit().equals(accountsFindByNumber.get(accountsFindByNumber.size() - 1).getCuit())) {
                                    accountsFindByNumber.add(account);
                                    contactsSelect.add(contactCuit);
                                }
                            }else {
                                accountsFindByNumber.add(account);
                                contactsSelect.add(contactCuit);
                            }
                        }

                        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                        View view = this.getLayoutInflater().inflate(R.layout.list_account, null);
                        builderSingle.setView(view);

                        final AlertDialog alertDialog = builderSingle.create();
                        recyclerViewAdapter = new RecyclerViewAdapter();
                        final RecyclerView recyclerView = view.findViewById(R.id.list);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerViewAdapter.update(accountsFindByNumber);
                        recyclerView.setAdapter(recyclerViewAdapter);
                        alertDialog.show();

                        AndroidUtils.findViewById(R.id.yesView, Button.class, view).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                                Contact contactContinue = null;
                                for (Contact contactSelect : contactsSelect){
                                    if(contactSelect.getAccountCuit().equals(selectedItemAccount.getCuit())){
                                        contactContinue = contactSelect;
                                        activity.setContactId(contactSelect.getCode());
                                        activity.setContactPhone(contactSelect.getPhone());
                                        activity.setContactDescription(contactSelect.getDescription());
                                        activity.setContactMail(contactSelect.getMail());
                                        break;
                                    }
                                }
                                activity.setCode("C:" + selectedItemAccount.getCuit() + ":" + codeDate);
                                activity.setAccountCuit(selectedItemAccount.getCuit());
                                activity.setAccountName(selectedItemAccount.getName());

                                try {
                                    ActivityRepository.getInstance().save(activity);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                ignoredOrComplete(selectedItemAccount.getCuit(),contactContinue);
                            }
                        });
                        break;
                    }
                }
            }else if (contacts.size() == 1){
                //un solo contacto con el numero
                Contact contact = contacts.get(0);
                activity.setContactId(contact.getCode());
                activity.setContactPhone(contact.getPhone());
                activity.setContactDescription(contact.getDescription());
                activity.setContactMail(contact.getMail());
                activity.setCode("C:" + contact.getAccountCuit() + ":" + codeDate);
                activity.setAccountCuit(contact.getAccountCuit());
                activity.setAccountName(contact.getAccountName());

                ActivityRepository.getInstance().save(activity);

                ignoredOrComplete(contact.getAccountCuit(),contact);
            }else {
                bindListAccounts();
                if(PhoneCallBO.getInstance().callIn_callOut.equals("callIn")) {
                    isIncomingCallForGexa();
                }
                else if(PhoneCallBO.getInstance().callIn_callOut.equals("callOut")) {
                    isOutcomingCallForGexa();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindListAccounts() {
        setContentView(R.layout.account_search);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerViewAdapterAccount = new RecyclerViewAdapterAccount();
        final RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapterAccount);
    }

    public void isIncomingCallForGexa() {

        final AlertWidget widget = AlertWidget.create();
        widget.showYesOrNo(this, "Llamada entrante", "Este contacto no está relacionado con ninguna cuenta. ¿Que queres hacer?","Relacionar","Ignorar","", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilter();
                widget.hide();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from.exists())
                    from.delete();
                widget.hide();
                finishAndRemoveTask();
            }
        },null);
        AndroidUtils.findViewById(R.id.postponeView, Button.class, widget.parentView).setVisibility(View.GONE);
    }

    public void isOutcomingCallForGexa() {

        final AlertWidget widget = AlertWidget.create();
        widget.showYesOrNo(this, "Llamada saliente", "Este contacto no está relacionado con ninguna cuenta","Agendar","","", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilter();
                widget.hide();
            }
        }, null,null);
        AndroidUtils.findViewById(R.id.postponeView, Button.class, widget.parentView).setVisibility(View.GONE);
    }

    private void onFilter() {
        try {
            recyclerViewAdapterAccount.update(AccountRepository.getInstance().findAll());
        }catch (Exception e) {
            Log.e(ActivitySearchView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    private void ignoredOrComplete(String accountCuit, final Contact contact) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("doCall" , true);
        editor.apply();

        final Activity activityPending;
        String callIn_callOut = "Sin resultado";
        if(PhoneCallBO.getInstance().callIn_callOut.equals("callIn")){
            callIn_callOut = "Llamada entrante";
        }else if (PhoneCallBO.getInstance().callIn_callOut.equals("callOut")){
            callIn_callOut = "Llamada saliente";
        }
        try {
            activityPending = ActivityRepository.getInstance().findByAccountCUITAndStateType(accountCuit, "PENDING");

            if(activityPending != null && activityPending.getType().equals("Call")){

                widget.showYesOrNo(this, callIn_callOut, "CUENTA: " + activityPending.getAccountName() + "\nTiene una actividad pendiente" + "\n¿Usas esta llamada para completar la actividad, o solo dejas la llamada registrada?", "Completar ACT", "","Registrar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity = activityPending;
                        if(PhoneCallBO.getInstance().callIn_callOut.equals("callIn")){
                            activity.setCallType(CallConstants.Type.INCOMING.name());
                        }else if (PhoneCallBO.getInstance().callIn_callOut.equals("callOut")){
                            activity.setCallType(CallConstants.Type.OUTGOING.name());
                        }
                        if(contact.getCode() != null)
                            activity.setContactId(contact.getCode());
                        else activity.setContactId(null);
                        activity.setContactDescription(contact.getDescription());
                        activity.setContactPhone(contact.getPhone());
                        activity.setContactMail(contact.getMail());
                        try {
                            ActivityRepository.getInstance().save(activity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        final Intent intent = new Intent(CallSuccess.this, CallCompleteView.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("activityCode", activity.getCode());
                        startActivity(intent);
                    }
                }, null, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        widget.hide();
                        //registrar
                        final Intent intent = new Intent(CallSuccess.this, CallCompleteView.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("activityCode", activity.getCode());
                        startActivity(intent);
                    }
                });
            }else{
                //registrar
                final Intent intent = new Intent(CallSuccess.this, CallCompleteView.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("activityCode", activity.getCode());
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


/////////////////////////////////////////////////////////////////
///////RECYCLER VIEW FROM ACCOUNTS WITH EQUALS NUMBER////////////
/////////////////////////////////////////////////////////////////

    private class RecyclerViewAdapter extends RecyclerView.Adapter<AccountItem> {

        private List<Account> filtered = new ArrayList<>();

        public void update(final List<Account> data) {
            if (data != null) {

                this.filtered.clear();
                this.filtered.addAll(data);

                notifyDataSetChanged();
            }
        }

        @Override
        public AccountItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.account_search_item, parent, false);
            return new AccountItem(view);
        }

        @Override
        public void onBindViewHolder(final AccountItem holder, int position) {
            holder.account = filtered.get(position);
            holder.accountNameView.setText(holder.account.name);
            holder.relationshipView.setText(holder.account.nr);
            holder.cuitView.setText(holder.account.cuit);
            if(holder.account.addressDescription != null) {
                holder.addressDescriptionView.setText(holder.account.addressDescription);
            }else {
                holder.addressTitle.setVisibility(View.GONE);
                holder.addressDescriptionView.setVisibility(View.GONE);
            }


            if (selectedItemAccount != null) {
                if (holder.account.id.equals(selectedItemAccount.id)) {
                    holder.view.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_dark));
                    holder.accountNameView.setTextColor(getResources().getColor(android.R.color.white));
                    holder.addressDescriptionView.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    holder.view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    holder.accountNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    holder.addressDescriptionView.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                }
            }

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedItemAccount != holder.account) {
                        selectedItemAccount = holder.account;
                        recyclerViewAdapter.notifyDataSetChanged();
                    }else {
                        selectedItemAccount = null;
                        holder.view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        holder.accountNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        holder.addressDescriptionView.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return filtered.size();
        }

    }


/////////////////////////////////////////////////////////////////
///////RECYCLER VIEW FROM ACCOUNTS FOR ADD CONTACT////////////
/////////////////////////////////////////////////////////////////

    @Override
    public boolean onQueryTextSubmit(String s) {return false;}

    @Override
    public boolean onQueryTextChange(String query) {
        recyclerViewAdapterAccount.getFilter().filter(TextUtils.isEmpty(query) ? null : query);
        return true;
    }

    private class RecyclerViewAdapterAccount extends RecyclerView.Adapter<AccountItem> {

        private FilterAccount filter;
        private List<Account> accounts = new ArrayList<>();
        private List<Account> filtered = new ArrayList<>();

        public void update(final List<Account> data) {
            if (data != null) {
                this.accounts.clear();
                this.accounts.addAll(data);

                this.filtered.clear();
                this.filtered.addAll(data);

                notifyDataSetChanged();
            }
        }

        @Override
        public AccountItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.account_search_item, parent, false);
            return new AccountItem(view);
        }

        @Override
        public void onBindViewHolder(final AccountItem holder, int position) {
            holder.account = filtered.get(position);
            holder.accountNameView.setText(holder.account.name);
            holder.relationshipView.setText(holder.account.nr);
            holder.cuitView.setText(holder.account.cuit);
            if(holder.account.addressDescription != null) {
                holder.addressDescriptionView.setText(holder.account.addressDescription);
            }else {
                holder.addressTitle.setVisibility(View.GONE);
                holder.addressDescriptionView.setVisibility(View.GONE);
            }

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCreateContact(holder.account);
                }
            });
        }

        @Override
        public int getItemCount() {
            return filtered.size();
        }

        public Filter getFilter() {
            if(filter == null)
                filter = new FilterAccount();
            return filter;
        }
        //
        private class FilterAccount extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults filterResults = new FilterResults();

                if (constraint != null && constraint.length() > 0) {
                    final List<Account> filtered = new ArrayList<>();
                    for (Account account : accounts){
                        if (account.name.toLowerCase().contains(constraint.toString().toLowerCase()))
                            filtered.add(account);
                        if(account.cuit.toLowerCase().contains(constraint.toString().toLowerCase()))
                            filtered.add(account);
                    }
                    filterResults.count = filtered.size();
                    filterResults.values = filtered;
                } else {
                    filterResults.count = accounts.size();
                    filterResults.values = accounts;
                }
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (List<Account>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    private void onCreateContact(final Account account) {
        final AlertWidget alertWidget  = AlertWidget.create();

        alertWidget.showContactNew(this, account, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact contactNew = new Contact();
                activity.setAccountName(account.getName());
                activity.setAccountCuit(account.getCuit());

                activity.setContactPhone(PhoneCallBO.getInstance().getNumber());
                activity.setContactDescription(alertWidget.contactDescription);
                activity.setContactMail(alertWidget.mailDescription);

                activity.setCode("C:" + activity.getAccountCuit() + ":" + codeDate);

                contactNew.setMail(alertWidget.mailDescription);
                contactNew.setDescription(alertWidget.contactDescription);
                contactNew.setPhone(PhoneCallBO.getInstance().getNumber());
                try {
                    ActivityRepository.getInstance().save(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ignoredOrComplete(account.getCuit(), contactNew);

                alertWidget.hide();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertWidget.hide();
            }
        });
    }
}
