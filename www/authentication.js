/*global cordova, module*/

module.exports = {
    authenticate: function (message, clientId, clientSecret, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Authentication", "authenticate", [message, clientId, clientSecret]);
    },
    isAvailable: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Authentication", "isAvailable");
    }
};