package com.satelinx.satelinx.models;

import android.content.Context;
import android.location.Location;

import com.satelinx.satelinx.R;
import com.satelinx.satelinx.helpers.DateHelper;

import java.util.Date;
import java.util.Locale;

/**
 * Created by jlh on 1/17/15.
 */
public class Coordinate {

    public float latitude;
    public float longitude;
    public float speed;
    public double altitude;
    public float heading;
    public Date sent_at_date;

    public String getTitle() {
        String title = "";
        if (this.sent_at_date != null) {
            title += DateHelper.getFormattedString(this.sent_at_date) + "\r\n";
        }
        return title;
    }

    public String getSnippet(Context ctx) {
        String snippet = "";

        if (this.sent_at_date != null) {
            snippet += DateHelper.getFormattedString(this.sent_at_date) + "\r\n";
        }
        snippet += String.format(Locale.ENGLISH, "%.3f, %.3f\r\n", this.latitude, this.longitude);

        if (this.speed != 0) {
            snippet += String.format(Locale.ENGLISH, "%s: %d\r\n", ctx.getString(R.string.speed), (int) this.speed);
        }
        if (this.altitude != 0) {
            snippet += String.format(Locale.ENGLISH, "%s: %d\r\n", ctx.getString(R.string.altitude), (int) this.altitude);
        }
        if (this.heading != 0) {
            snippet += String.format(Locale.ENGLISH, "%s: %d\r\n", ctx.getString(R.string.heading), (int) this.heading);
        }

        return snippet;
    }

    public float bearingTo(Coordinate other) {
        Location thisLocation = new Location("");
        thisLocation.setLatitude(this.latitude);
        thisLocation.setLongitude(this.longitude);

        Location otherLocation = new Location("");
        otherLocation.setLatitude(other.latitude);
        otherLocation.setLongitude(other.longitude);

        return thisLocation.bearingTo(otherLocation);
    }

}
