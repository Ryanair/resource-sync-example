package ryanair.com.resourcesyncexample;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ryanair.com.resourcesyncexample.model.Airport;

public class StorageManager {
    private static final String TAG = "StorageManager";
    private static final String DATABASE_NAME = "reference_data";
    private static final String DB_FILE_EXT = ".cblite";
    private static final String syncUrl = "http://192.168.56.1:4984/reference_data";
    private static final String AIRPORTS_VIEW = "getAirports";
    private static final String[] channels = new String[]{"ref_data_v1"};
    private static final String MARKETS_VIEW = "getMarkets";

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
            mDatabase = mManager.getExistingDatabase(DATABASE_NAME);

            // the database does not exist
            // copy it from the assets folder
            if (mDatabase == null) {
                InputStream assetDb = mContext.getAssets().open(DATABASE_NAME + DB_FILE_EXT);
                mManager.replaceDatabase(DATABASE_NAME, assetDb, null);

                // open the database after replacing
                mDatabase = mManager.getDatabase(DATABASE_NAME);
            }

        } catch (CouchbaseLiteException | IOException e) {
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
        replication.setChannels(Arrays.asList(channels));
        replication.setContinuous(true);
        replication.addChangeListener(changeListener);
        replication.start();
    }

    Replication.ChangeListener changeListener = new Replication.ChangeListener() {
        @Override
        public void changed(Replication.ChangeEvent event) {
            final int changeCount = event.getChangeCount();
            if (changeCount > 0) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("%d document(s) changed", changeCount), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

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
                if (airport != null) {
                    emitter.emit(airport.get("name"), airport);
                }
            }
        }, "3");

        View marketsView = mDatabase.getView(MARKETS_VIEW);
        marketsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                HashMap airport = (LinkedHashMap) document.get("airport");
                if (airport != null) {
                    emitter.emit(airport.get("code"), airport.get("markets"));
                }
            }
        }, "1");
    }

    public interface AirportsListener {
        void onChanged(List<Airport> airports);
    }
}
