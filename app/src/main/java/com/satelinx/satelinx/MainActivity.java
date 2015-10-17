package com.satelinx.satelinx;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.satelinx.satelinx.events.SelectAccountEvent;
import com.satelinx.satelinx.events.SelectAccountFailedEvent;
import com.satelinx.satelinx.events.SelectAccountReadyEvent;
import com.satelinx.satelinx.events.SelectTrackableEvent;
import com.satelinx.satelinx.events.SelectTrackableFailedEvent;
import com.satelinx.satelinx.events.SelectTrackableReadyEvent;
import com.satelinx.satelinx.helpers.ApiManager;
import com.satelinx.satelinx.helpers.EnvironmentManager;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.Coordinate;
import com.satelinx.satelinx.models.Trackable;
import com.satelinx.satelinx.models.User;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity
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
    protected Account mSelectedAccount;

    protected MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        processExtras(this.getIntent().getExtras());

        if (this.mUser == null) {
            Log.e(TAG, "Invalid user, logging out");
            performLogout();
        }

        setContentView(R.layout.activity_main);

        setupToolbar();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                this.mUser);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onAccountSelected(int position) {
        Account account = this.mUser.getAccount(position);
        onAccountSelected(account);
    }

    @Override
    public void onItemSelected(int position) {
        onItemSelected(position, "last");
    }

    @Override
    public void onItemSelected(int position, String date) {
        Log.d(TAG, "Selected item on position " + position);
        if (this.mSelectedAccount == null) {
            return;
        }
        List<Trackable> trackables = this.mSelectedAccount.getTrackables();
        if (trackables == null || trackables.isEmpty() || position < 0 || position >= trackables.size()) {
            return;
        }
        Trackable trackable = trackables.get(position);
        if (trackable == null) {
            return;
        }
        Log.d(TAG, "Sending event");
        EventBus.getDefault().post(new SelectTrackableEvent(trackable, date));
    }

    public void onAccountSelected(Account account) {
        if (account == null) {
            Log.e(TAG, "Invalid account");
            return;
        }
        EventBus.getDefault().post(new SelectAccountEvent(account, this.mUser.getAuthorizationHash()));
        FragmentManager fragmentManager = getSupportFragmentManager();

        mMainFragment = MainFragment.newInstance(account);
        fragmentManager.beginTransaction()
                .replace(R.id.container, mMainFragment)
                .commit();
    }

    public void onAccountReady(Account account) {
        this.mSelectedAccount = account;
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.reloadAccount(account);
        }
        if (mMainFragment != null) {
            mMainFragment.reloadAccount(account);
        }
    }

    public void onTrackableReady(Trackable trackable, List<Coordinate> coordinates) {
        if (mNavigationDrawerFragment != null) {
            Date date = null;
            if (coordinates != null && !coordinates.isEmpty()) {
                Coordinate c = coordinates.get(0);
                date = c.sent_at_date;
            }
            mNavigationDrawerFragment.onTrackableReady(trackable, date);
        }
        if (mMainFragment != null) {
            mMainFragment.loadTrackable(trackable, coordinates);
        }
    }

    public void onSectionAttached(Account account) {
        if (account == null) {
            return;
        }
        mTitle = account.getName();
    }

    public void restoreActionBar() {
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            // getMenuInflater().inflate(R.menu.main, menu);
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

    /*protected boolean setupUser() {
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
    }*/

    public void onEventBackgroundThread(SelectAccountEvent e) {
        try {
            Account account = ApiManager.getSession().populate(e.account.getId(), e.authorizationHash);
            if (EnvironmentManager.isDevelopment()) {
                Log.d(TAG, "Printing account");
                Log.d(TAG, Serialization.getPrettyPrintedString(account));
            }

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
        onAccountReady(e.account);
    }

    public void onEventBackgroundThread(SelectTrackableEvent e) {
        try {
            List<Coordinate> coordinates = ApiManager.getSession().showDate(e.trackable.id, e.date, this.mUser.getAuthorizationHash());
            if (EnvironmentManager.isDevelopment()) {
                Log.d(TAG, "Printing account");
                Log.d(TAG, Serialization.getPrettyPrintedString(coordinates));
            }

            if (coordinates == null || coordinates.isEmpty()) {
                EventBus.getDefault().post(new SelectTrackableFailedEvent(e.trackable, SelectTrackableFailedEvent.STATUS_EMPTY_LIST));
                return;
            }
            EventBus.getDefault().post(new SelectTrackableReadyEvent(e.trackable, coordinates));
        } catch (Exception exc) {
            exc.printStackTrace();

            EventBus.getDefault().post(new SelectTrackableFailedEvent(e.trackable, SelectTrackableFailedEvent.STATUS_UNAUTHORIZED));
        }
    }

    public void onEventMainThread(SelectTrackableReadyEvent e) {
        onTrackableReady(e.trackable, e.coordinates);
    }

    public void onEventMainThread(SelectTrackableFailedEvent e) {
        Log.d(TAG, "Trackable failed with status: " + e.status);
        int resId = -1;
        switch (e.status) {
            case SelectTrackableFailedEvent.STATUS_EMPTY_LIST:
                resId = R.string.select_failed_no_coordinates;
                break;
            case SelectTrackableFailedEvent.STATUS_UNAUTHORIZED:
                resId = R.string.select_failed_bad_request;
                break;
        }
        if (resId != -1) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        }
    }
}
