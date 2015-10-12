/**
 *
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/11/2015.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 *
 */

var exec = require('cordova/exec');

module.exports.initialize = function(success, failure){
    console.log("MWDevice.js: initialize");
    exec(success,  failure,  "MWDevice","initialize",[]);
}

module.exports.connect = function(macAddress, success, failure){
    console.log("MWDevice.js: connect");
    exec(success, failure, "MWDevice", "connect", [macAddress]);
}
module.exports.disconnect = function(){
    console.log("MWDevice.js: connect");
    exec(null, null, "MWDevice", "disconnect", []);
}
