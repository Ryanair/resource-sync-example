require("./globals.js");

module.exports = {
    getDocument: function (documentId, cb) {
        var http = require('http');

        var options = {
            host: syncAPI.endpoint,
            port: syncAPI.port,
            path: '/' + syncAPI.bucket + '/' + documentId
        };

        callback = function (response) {
            var str = '';

            response.on('data', function (chunk) {
                str += chunk;
            });

            response.on('end', function () {
                if (str !== "") {
                    cb(JSON.parse(str));
                }
            });
        }
        http.request(options, callback).end();
    },

    updateDocument: function (documentId, newDocument) {
        var http = require('http');

        var options = {
            host: syncAPI.endpoint,
            port: syncAPI.port,
            path: '/' + syncAPI.bucket + '/' + documentId,
            method: "PUT"
        };

        callback = function (response) {
            var str = '';

            response.on('data', function (chunk) {
                str += chunk;
            });

            response.on('end', function () {
                console.log("updated " + documentId);
            });
        }
        var req = http.request(options, callback);
        req.write(JSON.stringify(newDocument));
        req.end();
    }

};