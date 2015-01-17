package com.satelinx.satelinx;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;

/**
 * Created by jlh on 1/17/15.
 */
public class MainFragment extends Fragment {
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
    public static MainFragment newInstance(Account account) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_JSON, Serialization.getGsonInstance().toJson(account));
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //GoogleMap googleMap = ((MapFragment) getFragmentManager().findFragmentById(
        //        R.id.mapView)).getMap();

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
