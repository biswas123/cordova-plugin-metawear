#import <Cordova/CDVPlugin.h>
#import "MBLMetaWear.h"
#import "MBLMetaWearManager.h"


@interface MWDevice : CDVPlugin

- (void)scanForDevices:(CDVInvokedUrlCommand*)command;
- (void)connect:(CDVInvokedUrlCommand*)command;

@end
