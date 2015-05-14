package ryanair.com.resourcesyncexample.model;

import com.couchbase.lite.support.LazyJsonObject;

public class Airport {
    private LazyJsonObject mLazy;

    public Airport(LazyJsonObject lazy) {
        mLazy = lazy;
    }

    public String getCode() {
        return (String) mLazy.get("code");
    }

    public String getName() {
        return (String) mLazy.get("name");
    }

    public int getTimezone() {
        return (int) mLazy.get("timezone");
    }

    public String getCountry() {
        return (String) mLazy.get("country");
    }
}
