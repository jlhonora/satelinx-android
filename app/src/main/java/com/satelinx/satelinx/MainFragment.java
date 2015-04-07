package com.satelinx.satelinx;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.satelinx.satelinx.adapters.CoordinateInfoAdapter;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.Coordinate;
import com.satelinx.satelinx.models.Trackable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    private Polyline mPolyline;

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

        // Custom layout for infoWindows
        mMap.setInfoWindowAdapter(new CoordinateInfoAdapter(LayoutInflater.from(this.getActivity())));

        List<Coordinate> boundsCoordinates = new ArrayList<Coordinate>();
        for (Trackable t : trackables) {
            Coordinate c = setupTrackable(t);
            if (c == null) {
                continue;
            }
            boundsCoordinates.add(c);
        }
        setupCamera(boundsCoordinates);
    }

    protected double[] getBounds(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }

        double[] bounds = {180.0, 180.0, -180.0, -180.0}; // south, west, north, east
        for (Coordinate c : coordinates) {
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
        return bounds;
    }

    protected void setupCamera(List<Coordinate> coordinates) {
        double[] bounds = getBounds(coordinates);
        if (bounds != null) {
            setupCamera(bounds);
        }
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
        if (t == null || mMap == null || !t.hasValidLastCoordinate()) {
            return null;
        }

        Coordinate lastCoordinate = t.getLastCoordinate();

        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(lastCoordinate.latitude, lastCoordinate.longitude))
                .title(t.identifier)
                .snippet(t.getSnippet(this.getActivity().getBaseContext()))
                .icon(getCustomMarker(t))
                .anchor(0.5f, 0.5f)
                .draggable(false);

        mMap.addMarker(marker);

        return lastCoordinate;
    }

    public BitmapDescriptor getCustomMarker(final Trackable t) {
        Iconify.IconValue iconValue_t = Iconify.IconValue.fa_car;
        switch (t.type) {
            case "AirVehicle":
                iconValue_t = Iconify.IconValue.fa_plane;

                // No helicopter icon for now
                if (t.sub_type != null && t.sub_type.contains("Heli")) {
                    iconValue_t = Iconify.IconValue.fa_map_marker;
                }
                break;
            default:
                iconValue_t = Iconify.IconValue.fa_car;
        }

        final Iconify.IconValue iconValue = iconValue_t;

        IconDrawable id = new IconDrawable(this.getActivity(), iconValue) {

            @Override
            public void draw(Canvas canvas) {
                TextPaint paint = new TextPaint();
                paint.setTypeface(Iconify.getTypeface(getActivity()));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setUnderlineText(false);
                paint.setColor(Color.parseColor(t.path_color));
                paint.setAntiAlias(true);
                paint.setTextSize(getBounds().height());
                Rect textBounds = new Rect();
                String textValue = String.valueOf(iconValue.character());
                paint.getTextBounds(textValue, 0, 1, textBounds);
                float textBottom = (getBounds().height() - textBounds.height()) / 2f + textBounds.height() - textBounds.bottom;
                canvas.drawText(textValue, getBounds().width() / 2f, textBottom, paint);
            }

        }.actionBarSize();
        Drawable d = id.getCurrent();
        Bitmap bm = Bitmap.createBitmap(id.getIntrinsicWidth(), id.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);

        d.draw(c);

        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public BitmapDescriptor getCustomMarker(final Coordinate coordinate) {
        Iconify.IconValue iconValueTemp = Iconify.IconValue.fa_arrow_circle_up;
        if (coordinate.heading == 0) {
            iconValueTemp = Iconify.IconValue.fa_circle_o;
        }

        final Iconify.IconValue iconValue = iconValueTemp;

        IconDrawable id = new IconDrawable(this.getActivity(), iconValue) {

            @Override
            public void draw(Canvas canvas) {
                TextPaint paint = new TextPaint();
                paint.setTypeface(Iconify.getTypeface(getActivity()));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setUnderlineText(false);
                // paint.setColor(Color.parseColor(t.path_color));
                paint.setAntiAlias(true);
                paint.setTextSize(getBounds().height());
                Rect textBounds = new Rect();
                String textValue = String.valueOf(iconValue.character());
                paint.getTextBounds(textValue, 0, 1, textBounds);
                float textBottom = (getBounds().height() - textBounds.height()) / 2f + textBounds.height() - textBounds.bottom;
                if (coordinate.heading != 0) {
                    // Rotate using the center as pivot
                    canvas.rotate(coordinate.heading, getBounds().width() / 2f, getBounds().height() / 2f);
                }
                canvas.drawText(textValue, getBounds().width() / 2f, textBottom, paint);
            }

        }.actionBarSize();
        Drawable d = id.getCurrent();
        Bitmap bm = Bitmap.createBitmap(id.getIntrinsicWidth(), id.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);

        d.draw(c);

        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public void reloadAccount(Account account) {
        this.mAccount = account;
        setupMap();
    }

    public void loadTrackable(Trackable trackable, List<Coordinate> coordinates) {
        int size = 0;
        if (coordinates != null) {
            size = coordinates.size();
        }
        Log.d(TAG, "Loading trackable " + trackable + " with " + size + " coordinates");
        if (size <= 0) {
            return;
        }
        List<LatLng> points = coordsToLatLng(coordinates);

        if (mPolyline == null) {
            PolylineOptions rectOptions = new PolylineOptions()
                    .addAll(points);
            mPolyline = mMap.addPolyline(rectOptions);
        } else {
            mPolyline.setPoints(points);
        }
        mPolyline.setColor(Color.parseColor(trackable.path_color));

        // 7 pixel width, default is 10
        mPolyline.setWidth(7.0f);

        // Remove the last point, we don't need it for the
        // polyline.
        // The list end is marked as one past the end of the sublist
        ListIterator<Coordinate> iterator = coordinates.subList(1, coordinates.size()).listIterator();
        while (iterator.hasNext()) {
            Coordinate c = iterator.next();
            if (c.heading == 0.0 && iterator.hasPrevious()) {
                Coordinate cp = iterator.previous();
                c = iterator.next();
                c.heading = cp.bearingTo(c);
            }
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(c.latitude, c.longitude))
                    .title(c.getTitle())
                    .snippet(c.getSnippet(this.getActivity()))
                    .icon(getCustomMarker(c))
                    .anchor(0.5f, 0.5f)
                    .draggable(false);

            mMap.addMarker(marker);
        }

        setupCamera(coordinates);
    }

    public static List<LatLng> coordsToLatLng(List<Coordinate> coordinates) {
        if (coordinates == null) {
            return null;
        }
        List<LatLng> latlng = new ArrayList<LatLng>();
        for (Coordinate c : coordinates) {
            latlng.add(new LatLng(c.latitude, c.longitude));
        }

        return latlng;
    }
}
