
'use strict';
var exec = require('cordova/exec');




var geoloc = (function () {

    return {


        initLoc: function (callback, params) {
            exec(callback, null, 'Location', 'initLoc', params);
        },

        startLoc: function (callback, params) {
            exec(callback, null, 'Location', 'startLoc', params);
        },


        stopLoc: function (successCallback) {
            exec(successCallback, null, 'Location', 'stopLoc', []);
        }
    }
}());


module.exports = geoloc;
