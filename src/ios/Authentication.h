#import <Cordova/CDVPlugin.h>

@interface Authentication :CDVPlugin

- (void) isAvailable:(CDVInvokedUrlCommand*)command;

- (void) authenticate:(CDVInvokedUrlCommand*)command;
- (void) authenticateCustomPasswordFallback:(CDVInvokedUrlCommand*)command;
- (void) authenticateCustomPasswordFallbackAndEnterPasswordLabel:(CDVInvokedUrlCommand*)command;

@end