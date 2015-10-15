#import "MWDevice.h"
#import <Cordova/CDV.h>
#import "MBLMetaWear.h"
#import "MBLMetaWearManager.h"


@implementation MWDevice

- (void)scanForDevices:(CDVInvokedUrlCommand*)command
{
  CDVPluginResult* pluginResult = nil;

  [[MBLMetaWearManager sharedManager] startScanForMetaWearsAllowDuplicates:NO handler:^(NSArray *array) {
      for (MBLMetaWear *device in array) {
        NSLog(@"Found MetaWear: %@", device);
      }
    }];

  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"hello"];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
