package com.satelinx.satelinx;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.Coordinate;
import com.satelinx.satelinx.models.Trackable;

import java.util.List;

/**
 * Created by jlh on 1/17/15.
 */
public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_ACCOUNT_JSON = "account_json";

    private Account mAccount;

    private GoogleMap mMap;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(Account account) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        Log.d(TAG, "New fragment with account " + account);
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
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.mainMapView);

        if (mapFragment == null) {
            Log.e(TAG, "Map fragment is null!");
            return rootView;
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                setupMap();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mAccount = Serialization.getGsonInstance()
                .fromJson(getArguments()
                        .getString(ARG_ACCOUNT_JSON), Account.class);
        ((MainActivity) activity).onSectionAttached(mAccount);
    }

    protected void setupMap() {
        Log.d(TAG, "Account: " + this.mAccount);
        if (mMap == null || this.mAccount == null) {
            return;
        }
        List<Trackable> trackables = this.mAccount.getTrackables();
        if (trackables == null) {
            Log.d(TAG, "Null trackables!");
            return;
        }
        Log.d(TAG, "Setting up " + trackables.size() + " trackables");

        double[] bounds = {180.0, 180.0, -180.0, -180.0}; // south, west, north, east
        boolean hasValidBounds = false;
        for (Trackable t : trackables) {
            Coordinate c = setupTrackable(t);
            if (c == null) {
                continue;
            }
            hasValidBounds = true;
            if (c.latitude < bounds[0]) {
                bounds[0] = c.latitude;
            }
            if (c.longitude < bounds[1]) {
                bounds[1] = c.longitude;
            }
            if (c.latitude > bounds[2]) {
                bounds[2] = c.latitude;
            }
            if (c.longitude > bounds[3]) {
                bounds[3] = c.longitude;
            }
        }

        setupCamera(bounds);
    }

    protected void setupCamera(double[] bounds) {
        LatLngBounds mapBounds = new LatLngBounds(new LatLng(bounds[0], bounds[1]), new LatLng(bounds[2], bounds[3]));
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 20));

        CameraPosition pos = this.mMap.getCameraPosition();
        if (pos.zoom > 14) {
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos.target, 14));
        }
    }

    protected Coordinate setupTrackable(Trackable t) {
        if (t == null || mMap == null) {
            return null;
        }
        Coordinate lastCoordinate = t.getLastCoordinate();

        // Exact 0.0 coordinates are usually an error
        if (lastCoordinate == null          ||
            lastCoordinate.latitude  == 0.0 ||
            lastCoordinate.longitude == 0.0) {
            return null;
        }

        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(lastCoordinate.latitude, lastCoordinate.longitude))
                .title(t.identifier)
                .snippet(t.driver_name)
                .draggable(false);

        mMap.addMarker(marker);

        return lastCoordinate;
    }

    public void reloadAccount(Account account) {
        this.mAccount = account;
        setupMap();
    }
}
