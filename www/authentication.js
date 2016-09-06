/*global cordova, module*/

module.exports = {
    authenticate: function (message, clientId, clientSecret, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Authentication", "authenticate", [message, clientId, clientSecret]);
    },
    availability: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Authentication", "availability", []);
    }
};