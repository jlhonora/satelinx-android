package com.satelinx.satelinx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.satelinx.satelinx.R;

/**
 * Created by jlh on 2/25/15.
 */
public class CoordinateInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater mInflater;
    private View mView;

    public CoordinateInfoAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.coordinate_info, null, false);
        }
        TextView tv_title = (TextView) mView.findViewById(R.id.title);
        tv_title.setText(marker.getTitle());

        TextView tv_snippet = (TextView) mView.findViewById(R.id.snippet);
        tv_snippet.setText(marker.getSnippet());

        return mView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
