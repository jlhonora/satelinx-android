package com.satelinx.satelinx;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.Trackable;
import com.satelinx.satelinx.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    private static final String TAG = NavigationDrawerFragment.class.getSimpleName();

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private static final String ARG_USER_JSON = "user_json";

    /**
     * The date format to use to display the selected date
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private Spinner mDrawerSpinner;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private User mUser;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processExtras(this.getArguments());

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View drawerView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView = (ListView) drawerView.findViewById(R.id.drawer_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(parent, position);
            }
        });

        mDrawerSpinner = (Spinner) drawerView.findViewById(R.id.drawer_spinner);

        return drawerView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, User user) {
        this.mUser = user;
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerSpinner.setAdapter(new ArrayAdapter<Account>(
                getActionBar().getThemedContext(),
                R.layout.layout_list_item_active,
                R.id.text,
                this.mUser.getAccounts()));

        mDrawerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectAccountItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        // mDrawerSpinner.setItemChecked(mCurrentSelectedPosition, true);
        selectAccountItem(0);

        setupUserView(this.getView());
    }

    protected void setupUserView(View root) {
        if (this.mUser == null || root == null) {
            return;
        }
        TextView tv1 = (TextView) root.findViewById(R.id.drawer_text1);
        TextView tv2 = (TextView) root.findViewById(R.id.drawer_text2);

        tv1.setText(this.mUser.getName());
        tv2.setText(this.mUser.getUsername());
    }

    private void selectAccountItem(int position) {
        // TODO: Clear map when an invalid selection is made
        if (this.mUser == null) {
            return;
        }
        List<Account> accounts = this.mUser.getAccounts();
        if (accounts == null || accounts.isEmpty() || position < 0 || accounts.size() <= position) {
            return;
        }
        Account account = accounts.get(position);
        if (account == null) {
            return;
        }

        if (this.mCallbacks != null) {
            mCallbacks.onAccountSelected(position);
        }

        setupAccount(account);
    }

    private void setupAccount(Account account) {
        List<Trackable> trackables = account.getTrackables();
        if (trackables == null) {
            Log.d(TAG, "Null trackables");
            return;
        }

        if (trackables.isEmpty()) {
            Log.d(TAG, "Empty trackables");
            return;
        }

        mDrawerListView.setAdapter(new ArrayAdapter<Trackable>(
                getActionBar().getThemedContext(),
                R.layout.layout_list_item_active,
                R.id.text,
                trackables));
    }

    private void selectItem(AdapterView<?> parent, int position) {
        if (position == mCurrentSelectedPosition) {
            // Show date dialog
            selectItemDate(parent, position);
        } else {
            performItemSelection(parent, position);
        }
    }

    private void selectItemDate(final AdapterView<?> parent, final int position) {
        final Date preloadedDate = getPreloadDate(parent, position);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(preloadedDate);
        // Create date picker listener.
        CalendarDatePickerDialog.OnDateSetListener dateSetListener = new CalendarDatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
                String date = String.format("%d-%d-%d", year, monthOfYear, dayOfMonth);
                performItemSelection(parent, position, date);
            }
        };

        // Show date picker dialog.
        CalendarDatePickerDialog dialog = new CalendarDatePickerDialog();
        dialog.setOnDateSetListener(dateSetListener);
        dialog.initialize(dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        dialog.show(getFragmentManager(), "date_picker");
    }

    protected Date getPreloadDate(AdapterView<?> parent, int position) {
        Date preloadedDate = new Date();
        if (parent == null || parent.getChildCount() <= position) {
            return preloadedDate;
        }
        View selectedView = parent.getChildAt(position);
        if (selectedView == null) {
            return preloadedDate;
        }
        TextView dateView = (TextView) selectedView.findViewById(R.id.date_text);
        if (dateView == null || dateView.getText().length() == 0) {
            return preloadedDate;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        try {
            return sdf.parse(dateView.getText().toString());
        } catch (java.text.ParseException e) {
            return preloadedDate;
        }
    }
    protected void performItemSelection(AdapterView<?> parent, int position) {
        performItemSelection(parent, position, "last");
    }

    protected void performItemSelection(AdapterView<?> parent, int position, String date) {
        if (parent != null) {
            for (int i = parent.getFirstVisiblePosition(); i < parent.getLastVisiblePosition(); i++) {
                View v = parent.getChildAt(i);
                setListItemStatus(v, false);
            }
        }

        Log.d(TAG, "Selected item at position " + position);
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onItemSelected(position, date);
        }

        if (parent != null) {
            View v = parent.getChildAt(position);
            setListItemStatus(v, true);
        }
    }

    protected void setListItemStatus(View v, boolean active) {
        if (v == null) {
            return;
        }
        View indicator = v.findViewById(R.id.indicator);
        TextView textView = (TextView) v.findViewById(R.id.text);
        TextView dateView = (TextView) v.findViewById(R.id.date_text);
        if (active) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            indicator.setVisibility(View.VISIBLE);
        } else {
            textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
            indicator.setVisibility(View.INVISIBLE);
            dateView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            // inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onItemSelected(int position);
        void onItemSelected(int position, String date);
        void onAccountSelected(int position);
    }

    public void processExtras(Bundle extras) {
        if (extras == null) {
            return;
        }
        if (!extras.containsKey(ARG_USER_JSON)) {
            return;
        }
        this.mUser = Serialization.getGsonInstance().fromJson(extras.getString(ARG_USER_JSON), User.class);
    }

    public void reloadAccount(Account a) {
        this.setupAccount(a);
    }

    public void onTrackableReady(Trackable t) {
        onTrackableReady(t, null);
    }

    public void onTrackableReady(Trackable t, Date d) {
        Log.d(TAG, "Trackable Ready: " + t);
        if (t == null) {
            return;
        }

        View v = mDrawerListView.getChildAt(mCurrentSelectedPosition);
        if (v == null) {
            return;
        }

        TextView dateView = (TextView) v.findViewById(R.id.date_text);
        if (dateView == null) {
            return;
        }

        if (d == null) {
            dateView.setVisibility(View.GONE);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            dateView.setText(sdf.format(d));
            dateView.setVisibility(View.VISIBLE);
        }
    }

}
