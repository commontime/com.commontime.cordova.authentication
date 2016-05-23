# Authentication Plugin

Authentication plugin for iOS and Android. This plugin comnbines two exiasting plugins to provide a complete native authentication system.

The plugin combines two existing plugins.

To enable Android native authentication via a fingerprint the following plugin was used as a base. https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth. It was then adapted to automatically use password authentication if the device does not have the required hardware for fingerprints. Otherwise, the behaviour is identical to the original plugin.

Seocnd plugin is to enable Touch ID authentication for iOS. https://github.com/EddyVerbruggen/cordova-plugin-touch-id. The code for this hasd not been adapted in any way. 

# Usage

To check for availability:

```
authentication.isAvailable(function() {
    //Yes
}, function() {
    //No
});
```

To authenticate:

```
authentication.authenticate(text, "myAppName", "a_secret_key", function(result) {
    //Success
}, function(error) {
    //Failed
});
```



