package com.satelinx.satelinx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.satelinx.satelinx.events.SelectAccountEvent;
import com.satelinx.satelinx.events.SelectAccountFailedEvent;
import com.satelinx.satelinx.events.SelectAccountReadyEvent;
import com.satelinx.satelinx.helpers.SatelinxSession;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.User;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_USER_JSON = "user_json";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * The user of this activity
     */
    protected User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        processExtras(this.getIntent().getExtras());

        if (this.mUser == null) {
            Log.e(TAG, "Invalid user, logging out");
            performLogout();
        }

        setupUser();

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                this.mUser);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Account account = this.mUser.getAccount(position);
        onAccountSelected(account);
    }

    public void onAccountSelected(Account account) {
        if (account == null) {
            Log.e(TAG, "Invalid account");
            return;
        }
        Log.d(TAG, "Setting account " + account.getName());
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(account))
                .commit();
    }

    public void onSectionAttached(Account account) {
        if (account == null) {
            return;
        }
        mTitle = account.getName();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_ACCOUNT_JSON = "section_number";

        private Account mAccount;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Account account) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_ACCOUNT_JSON, Serialization.getGsonInstance().toJson(account));
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            Account account = Serialization.getGsonInstance()
                    .fromJson(getArguments()
                    .getString(ARG_ACCOUNT_JSON), Account.class);
            ((MainActivity) activity).onSectionAttached(account);
        }
    }

    protected void processExtras(Bundle extras) {
        if (extras == null) {
            return;
        }
        if (!extras.containsKey(KEY_USER_JSON)) {
            return;
        }
        this.mUser = Serialization.getGsonInstance().fromJson(extras.getString(KEY_USER_JSON), User.class);
    }

    protected void performLogout() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    protected boolean setupUser() {
        Log.d(TAG, "Setup user");
        if (this.mUser == null) {
            return false;
        }
        Account a = this.mUser.getAccount(0);
        if (a == null) {
            return false;
        }
        EventBus.getDefault().post(new SelectAccountEvent(a, this.mUser.getAuthorizationHash()));
        return true;
    }

    public void onEventBackgroundThread(SelectAccountEvent e) {
        Gson gson = Serialization.getGsonInstance();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(SatelinxSession.API_ENDPOINT)
                .build();

        SatelinxSession session = restAdapter.create(SatelinxSession.class);
        try {
            Account account = session.populate(e.account.getId(), e.authorizationHash);
            Log.d(TAG, "Printing account");
            Log.d(TAG, Serialization.getPrettyPrintedString(account));

            EventBus.getDefault().post(new SelectAccountReadyEvent(account));
        } catch (Exception exc) {
            exc.printStackTrace();

            EventBus.getDefault().post(new SelectAccountFailedEvent(e.account, exc));
        }
    }

    public void onEventMainThread(SelectAccountFailedEvent e) {
        Toast.makeText(this, "Error selecting account " + e.account.getName(), Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(SelectAccountReadyEvent e) {
        onAccountSelected(e.account);
    }
}
