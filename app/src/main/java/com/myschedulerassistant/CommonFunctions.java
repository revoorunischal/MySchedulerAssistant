package com.myschedulerassistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nischal on 10/14/2017.
 */

public class CommonFunctions {

    public static Double[] getDistanceAndDuration(final double lat1, final double lon1, final double lat2, final double lon2, GoogleMap mMap) {
        Double parsedDistance = null;
        Double durationInMins = null;
        String response = "";
        Double[] distanceAndDurationAnd = new Double[2];
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin="
                    + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=driving&key=AIzaSyCJxPHicXv_QtFy5cFJLVSlJizqIdSMedQ");
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            Log.i("Maps", url.toString());
            InputStream in = new BufferedInputStream(conn.getInputStream());
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = in.read(contents)) != -1) {
                response += new String(contents, 0, bytesRead);
            }
            Log.i(CommonFunctions.class.getName(), "response " + response);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("routes");
            if (array.length() > 0) {
                Log.i("Maps", "found data so parsing it ");
            } else {
                if (jsonObject.has("error_message")) {
                    Log.i("Maps", jsonObject.getString("error_message"));
                }
                if (jsonObject.has("status")) {
                    if (jsonObject.getString("status").equals("OVER_QUERY_LIMIT")) {
                        Thread.sleep(2000);
                    } else if (jsonObject.getString("status").equals("ZERO_RESULTS")) {
                        Log.i("Maps", "No route found");
                        distanceAndDurationAnd[0] = 0.0;
                        distanceAndDurationAnd[1] = 0.0;
                        return distanceAndDurationAnd;
                    }
                }
            }

            JSONObject routes = array.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject steps = legs.getJSONObject(0);
            JSONObject distance = steps.getJSONObject("distance");
            parsedDistance = Double.parseDouble(distance.getString("value"));
            Log.i(CommonFunctions.class.getName(), "parsed distance in metres " + parsedDistance);
            parsedDistance = parsedDistance / 1000;
            Log.i(CommonFunctions.class.getName(), "parsed distance in km " + parsedDistance);
            JSONObject duration = steps.getJSONObject("duration");
            durationInMins = Double.parseDouble(duration.getString("value")) / 60;
            Log.i(CommonFunctions.class.getName(), "parsed duration in mins " + durationInMins);
            distanceAndDurationAnd[0] = parsedDistance;
            distanceAndDurationAnd[1] = durationInMins;
            if (parsedDistance != null && mMap != null) {
                JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                String polyline = overviewPolyline.getString("points");

                List<LatLng> list = decodePoly(polyline);

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(list)
                        .width(10)
                        .color(Color.BLUE)
                        .geodesic(false)
                        .clickable(true)
                );
                line.setPoints(list);
                line.setTag("Distance " + parsedDistance + " kms and time to travel is " + durationInMins.intValue() + " mins");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return distanceAndDurationAnd;
    }

    public static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public static void showAlertMsg(String title, String message, Context context) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
