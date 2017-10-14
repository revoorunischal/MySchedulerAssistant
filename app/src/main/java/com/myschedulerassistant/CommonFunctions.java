package com.myschedulerassistant;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by nischal on 10/14/2017.
 */

public class CommonFunctions {

    public static Double getDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        Double parsedDistance = null;
        String response = "";

        try {
            URL url = new URL("http://maps.googleapis.com/maps/api/directions/json?origin="
                    + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=driving");
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = in.read(contents)) != -1) {
                response += new String(contents, 0, bytesRead);
            }
            Log.i(CommonFunctions.class.getName(), "response " + response);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("routes");
            JSONObject routes = array.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject steps = legs.getJSONObject(0);
            JSONObject distance = steps.getJSONObject("distance");
            parsedDistance = Double.parseDouble(distance.getString("value"));
            Log.i(CommonFunctions.class.getName(), "parsed distance in metres " + parsedDistance);
            parsedDistance = parsedDistance / 1000;
            Log.i(CommonFunctions.class.getName(), "parsed distance in km " + parsedDistance);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return parsedDistance;
    }
}
