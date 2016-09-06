#import <Cordova/CDVPlugin.h>

@interface Authentication :CDVPlugin

- (void) availability:(CDVInvokedUrlCommand*)command;

- (void) authenticate:(CDVInvokedUrlCommand*)command;
- (void) authenticateCustomPasswordFallback:(CDVInvokedUrlCommand*)command;
- (void) authenticateCustomPasswordFallbackAndEnterPasswordLabel:(CDVInvokedUrlCommand*)command;

@end