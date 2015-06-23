package ryanair.com.resourcesyncexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
    AirportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new AirportsAdapter(new ArrayList<Airport>());

        ButterKnife.inject(this);

        airportsList.setLayoutManager(new LinearLayoutManager(this));
        airportsList.setAdapter(adapter);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDataSet(airports);
            }
        });
    }

    void updateDataSet(List<Airport> airports) {
        // the adapter is empty, just add all items
        if (adapter.getItemCount() == 0 && !airports.isEmpty()) {
            airportsList.setAdapter(new AirportsAdapter(airports));
            return;
        }

        List<Airport> items = adapter.getItems();
        List<Airport> previousItems = new ArrayList<>(items);

        // remove the deleted items
        for (int i = 0; i < previousItems.size(); i++) {
            Airport previousItem = previousItems.get(i);
            if (!airports.contains(previousItem)) {
                adapter.removeItem(previousItem);
            }
        }

        // add the new items
        for (int i = 0; i < airports.size(); i++) {
            Airport currentItem = airports.get(i);
            if (!items.contains(currentItem)) {
                adapter.addItem(i, currentItem);
            }
        }
    }
}
