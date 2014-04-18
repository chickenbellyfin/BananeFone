package edu.tmpt.texttranslate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.tmpt.texttranslate.DistanceCalculator.Location;

public class MapActivity extends Activity {

	private static final String TAG = MapActivity.class.getSimpleName();

	private Location[] locations;
	private LangCode[] langs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_map);

		setProgressBarIndeterminateVisibility(true);
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				Translator trans = new Translator();
				Log.d(TAG, "msg:" + getIntent().getExtras().getString("msg"));
				trans.getTranslation(getIntent().getExtras().getString("msg"),
						0.6, 10);
				langs = trans.getLangChain();
				locations = new Location[langs.length];

				Log.d(TAG, "lcoaions = " + locations.length);
				for (int i = 0; i < langs.length; i++) {
					locations[i] = DistanceCalculator.getLocation(langs[i]);
					Log.d(TAG, "loc " + locations[i].lat + ","
							+ locations[i].lon);

				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				Log.d(TAG, "onpostexxpsdjfaio;sdf");
				GoogleMap map = ((MapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();

				PolylineOptions line_opt = new PolylineOptions().geodesic(false);
				for (int i = 0; i < locations.length; i++) {
					LatLng pos = new LatLng(locations[i].lat, locations[i].lon);
					line_opt.add(pos);
					map.addMarker(new MarkerOptions().position(pos).title(langs[i].toString()));
				}

				map.addPolyline(line_opt);
				setTitle("BananeFone: "+Math.round(DistanceCalculator.getDistance(langs))+" km");
				setProgressBarIndeterminateVisibility(false);
				return;
			}

		}.execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

}
