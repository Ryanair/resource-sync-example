package ryanair.com.resourcesyncexample;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.support.LazyJsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ryanair.com.resourcesyncexample.model.Airport;

public class StorageManager {
    private static final String TAG = "StorageManager";
    private static final String DATABASE_NAME = "reference_data";
    private static final String syncUrl = "http://192.168.56.1:4984/reference_data";
    private static final String AIRPORTS_VIEW = "getAirports";

    private Context mContext;
    private Manager mManager;
    private Database mDatabase;
    private AirportsListener mAirportsListener;

    public StorageManager(Context context, AirportsListener airportsListener) {
        mContext = context;
        mAirportsListener = airportsListener;

        openDb();
        registerViews();
    }

    private void openDb() {
        if (!Manager.isValidDatabaseName(DATABASE_NAME)) {
            Log.e(TAG, "Bad database name");
            return;
        }

        try {
            mManager = new Manager(new AndroidContext(mContext), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create manager object");
            return;
        }

        try {
            mDatabase = mManager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        startSync();
    }

    private void startSync() {
        URL url;

        try {
            url = new URL(syncUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        Replication replication = mDatabase.createPullReplication(url);
        replication.setContinuous(true);
        replication.start();
    }

    public void startLiveQuery() {
        View airportsView = mDatabase.getView(AIRPORTS_VIEW);
        LiveQuery liveQuery = airportsView.createQuery().toLiveQuery();
        liveQuery.setDescending(false);

        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                List<Airport> airports = new ArrayList<>();
                for (Iterator<QueryRow> it = event.getRows(); it.hasNext(); ) {
                    QueryRow row = it.next();

                    airports.add(new Airport((LazyJsonObject) row.getValue()));
                }

                if (mAirportsListener != null) {
                    mAirportsListener.onChanged(airports);
                }
            }
        });

        liveQuery.start();
    }

    private void registerViews() {
        View airportsView = mDatabase.getView(AIRPORTS_VIEW);
        airportsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                HashMap airport = (LinkedHashMap) document.get("airport");
                if(airport != null) {
                    emitter.emit(airport.get("name"), airport);
                }
            }
        }, "3");
    }

    public interface AirportsListener {
        void onChanged(List<Airport> airports);
    }
}
