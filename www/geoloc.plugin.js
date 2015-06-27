/* AeroGear Cordova Plugin
 * https://github.com/aerogear/aerogear-pushplugin-cordova
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';
var exec = require('cordova/exec');

/**
 the global geofencing object is the entry point for all geofencing methods
 @status Experimental
 @class
 @returns {object} geofencing - The geofencing api
 */


var geoloc = (function () {

    return {


        initLoc: function (callback) {
            exec(callback, null, 'Location', 'initLoc', []);
        },

        startLoc: function (callback) {
            exec(callback, null, 'Location', 'startLoc', []);
        },


        stopLoc: function (successCallback) {
            exec(successCallback, null, 'Location', 'stopLoc', []);
        }
    }
}());


module.exports = geoloc;
