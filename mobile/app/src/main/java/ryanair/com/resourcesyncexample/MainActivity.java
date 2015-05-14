package ryanair.com.resourcesyncexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.List;

import ryanair.com.resourcesyncexample.model.Airport;


public class MainActivity extends ActionBarActivity implements StorageManager.AirportsListener {
    private StorageManager mStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorageManager = new StorageManager(this, this);
        mStorageManager.startLiveQuery();
    }

    @Override
    public void onChanged(final List<Airport> airports) {
        if(airports.size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), airports.get(0).getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
