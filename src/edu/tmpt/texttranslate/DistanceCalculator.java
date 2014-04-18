package edu.tmpt.texttranslate;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class DistanceCalculator {

        private final static String TAG = DistanceCalculator.class.getSimpleName();
        private static HashMap<LangCode, Location> locations;
        public static class Location {
                public double lat, lon;

                public Location(double lat, double lon) {
                        this.lat = lat;
                        this.lon = lon;
                }
        }
        
        private DistanceCalculator() {}

        public static void loadLocations(File file_path, Context con) {
                File f = new File(file_path, "locations");
                boolean reload = true;
                if (f.exists()) {
                        try {
                                locations = new HashMap<LangCode, Location>();
                                ObjectInputStream in = new ObjectInputStream(
                                                new FileInputStream(f));
                                for (LangCode l : LangCode.values()) {
                                        try {
                                                reload = true;
                                                if (l.code.equals(in.readUTF())) {
                                                        locations.put(
                                                                        l,
                                                                        new Location(in.readDouble(), in
                                                                                        .readDouble()));
                                                }
                                                reload = false;
                                        } catch (EOFException e) {
                                        } catch (IOException e) {
                                                reload = true;
                                        }
                                }
                                in.close();
                        } catch (IOException e) {
                                reload = true;
                        }
                }
                if (reload) {
                        ObjectOutputStream out = null;
                        try {
                                Geocoder geo = new Geocoder(con);
                                locations = new HashMap<LangCode, Location>();
                                out = new ObjectOutputStream(new FileOutputStream(f));
                                for (LangCode l : LangCode.values()) {
                                        Location loc = getGeocoderLocation(l, geo);
                                        locations.put(l, loc);
                                        out.writeUTF(l.code);
                                        out.writeDouble(loc.lat);
                                        out.writeDouble(loc.lon);
                                }
                                out.close();
                        } catch (IOException e) {
                                Log.d(TAG, e.toString());
                                if (out != null)
                                        try {
                                                out.close();
                                        } catch (IOException e1) {
                                                Log.d(TAG, e1.toString());
                                        }
                        }
                }
        }

        private static Location getGeocoderLocation(LangCode lang, Geocoder geo) throws IOException {
                Log.d(TAG, lang.capital);
                List<Address> closestLocations = geo.getFromLocationName(lang.capital, 5);
                if (closestLocations.isEmpty()) {
                        Log.d(TAG, "No Locations Found");
                        return null;
                } else {
                        Log.d(TAG, closestLocations.get(0).getLatitude() + ", "
                                        + closestLocations.get(0).getLongitude());
                        Location loc = new Location(closestLocations.get(0).getLatitude(),
                                        closestLocations.get(0).getLongitude());
                        return loc;
                }
        }
        
        public static Location getLocation(LangCode lc) {
        	return locations.get(lc);
        }

        public static double getDistance(LangCode[] langCodes) {
                double distance = 0.0;

                for (int i = 1; i < langCodes.length; i++) {
                        distance += getDistanceLocations(getLocation(langCodes[i - 1]),
                        		getLocation(langCodes[i]));
                }

                return distance;
        }

        public static double getDistanceLocations(Location loc1, Location loc2) {
                double p1 = loc1.lat / 180.0 * Math.PI;
                double p2 = loc2.lat / 180.0 * Math.PI;
                double l1 = loc1.lon / 180.0 * Math.PI;
                double l2 = loc2.lon / 180.0 * Math.PI;
                double phav = Math.sin((p2 - p1) / 2.0);
                double lhav = Math.sin((l2 - l1) / 2.0);
                phav *= phav;
                lhav *= lhav;
                return 6378.1 * 2.0 * Math.asin(Math.sqrt(phav + Math.cos(p1)
                                * Math.cos(p2) * lhav));
        }
}

