package ryanair.com.resourcesyncexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ryanair.com.resourcesyncexample.model.Airport;
import ryanair.com.resourcesyncexample.util.DividerItemDecoration;
import ryanair.com.resourcesyncexample.util.RecyclerItemTouchListener;


public class MainActivity extends ActionBarActivity implements StorageManager.AirportsListener {
    private StorageManager mStorageManager;
    @InjectView(R.id.airports_list)
    RecyclerView airportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        airportsList.setLayoutManager(new LinearLayoutManager(this));
        airportsList.setItemAnimator(new DefaultItemAnimator());
        airportsList.setAdapter(new AirportsAdapter(new ArrayList<Airport>()));
        airportsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        airportsList.addOnItemTouchListener(new RecyclerItemTouchListener(this, touchListener));

        mStorageManager = new StorageManager(this, this);
        mStorageManager.startLiveQuery();
    }

    RecyclerItemTouchListener.OnItemClickListener touchListener = new RecyclerItemTouchListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            AirportsAdapter adapter = (AirportsAdapter) airportsList.getAdapter();

            Airport airport = adapter.getItem(position);
            // TODO list all markets 
        }
    };

    @Override
    public void onChanged(final List<Airport> airports) {
        if (airports.size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    airportsList.setAdapter(new AirportsAdapter(airports));
                }
            });
        }
    }
}
