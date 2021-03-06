# Recource caching using Sync Gateway + Couchbase Lite

This example project illustrates how to implement smart recource caching using [**Sync gateway**](https://github.com/couchbase/sync_gateway) + [**Couchbase lite**](https://github.com/couchbase/couchbase-lite-android). The technique is very useful if you can't modify your resource repository (for ex. using a third party or a legacy system and your team has not enough time to implement the sync API)

## Architecture
![](http://i284.photobucket.com/albums/ll17/Vlado_Atanasov/node_resource_update_fixed_zpsf3gzjozf.png)

A nodejs service retrieves all resource documents from the REST API and compares them with the documents stored in the couchbase bucket, if differences are detected, the service updates the couchbase bucket via the sync API, which increments the revision number and triggers a sync to the mobile client. This technique allows us to save a significant amount of traffic and still gives us the ability to update resources without releasing an application update

## Project structure
[**Mobile**](https://github.com/Ryanair/resource-sync-example/tree/master/mobile) - Sample Android application that listens for changes from the [**sync gateway**](https://github.com/couchbase/sync_gateway)

[**Nodejs**](https://github.com/Ryanair/resource-sync-example/tree/master/nodejs) - Sample nodejs service that compares REST API documents with cached couchbase documents and updates them

[**Resources**](https://github.com/Ryanair/resource-sync-example/tree/master/resources) - Sample JSON data

## Usage
To run the example, you need to have a running instance of the sync gateway with a set up bucket. First step is to configure the nodejs service, the endpoints are set in the file node_modules/globals.js, you can create channels by adding them to the channels array of the global.syncAPI object, this will set the channels to the newly created documents, it won't update the channel of any existing documents (note that this is dependant on your sync function, please refer to the [**documentation**](http://developer.couchbase.com/mobile/develop/guides/sync-gateway/sync-function-api-guide/index.html) for more info).

Next step is to setup what you want to sync, this is done in main.js by calling the data.getData method 
```
data.getData("airports.json", "airport", "code", "airports");

@param1 - REST API method
@param2 - document id preffix
@param3 - document key
@param4 - REST API document root node
```

if everything is setup correctly and you execute `node main.js` you should start seing the documents being created, if you change a document in your REST API and re-run the service, you will create a new revision of the document

To set up the android example project, you need to edit `StorageManager.java` and set DATABASE_NAME and syncUrl according your set up. Once this is set up, the application will start listening for changes through the sync gateway. The application will send only the latest revision number to the sync gateway and work with the local clone of the data, thus saving the client from generating unnesesarry traffic. When the node service updates the bucket, you will see the data transferred to the device 

## Sync gateway wrapper
The directory sg-wrapper contains a sync gateway wrapper. Execute `./sg.sh start` to run the sync gateway locally, without the need to set up couchbase server + sync gateway. The service will run on http://localhost:4985 (admin port) and http://localhost:4984 (mobile port). You can configure the data bucket and the sync funtion in script/sync-gateway-config.json

## Using a pre-built database
You can include a pre-built copy of your database in your application. The database will be shipped with the application. This can be good for big databases, it's faster to download 100mb of bulk data, via google play, than waiting for the replication to transfer all data. To do that, you first need to create a clean snapshot of your database in an android emulator. Run the replication on a clean database and wait for it to finish, this will download all the documents with the proper revision metadata, so next time you run the replication, couchbase lite will download only the new documents, saving you bandwith and initialization times. Then download the generated clbite file from the emulator using adb `adb pull /data/data/com.packagename/files/db_name.cblite`. In the current example, this looks like that: `adb pull /data/data/ryanair.com.resourcesyncexample/files/reference_data.cblite`. After you retrieve the database, you need to include it in your project. Place it in your assets folder. To inflate the database, you can use the following code snippet:
```java
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
```

For more information, you can consult the [**couchbase developer portal**](http://developer.couchbase.com/mobile/develop/guides/couchbase-lite/native-api/database/index.html)

