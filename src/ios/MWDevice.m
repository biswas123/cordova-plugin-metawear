#import "MWDevice.h"
#import <Cordova/CDV.h>
#import "MBLMetaWear.h"
#import "MBLMetaWearManager.h"


@implementation MWDevice {
  NSArray *scannedDevices;
}

- (void)scanForDevices:(CDVInvokedUrlCommand*)command
{
  CDVPluginResult* pluginResult = nil;
  NSMutableDictionary *boards = [NSMutableDictionary dictionaryWithDictionary:@{}];

  [[MBLMetaWearManager sharedManager] startScanForMetaWearsAllowDuplicates:YES handler:^(NSArray *array) {
      scannedDevices = array;
      [[MBLMetaWearManager sharedManager] stopScanForMetaWears];
      for (MBLMetaWear *device in array) {
        NSMutableDictionary *entry = [NSMutableDictionary dictionaryWithDictionary:@{}];
        entry[@"address"] = device.identifier.UUIDString;
        entry[@"rssi"] = [device.discoveryTimeRSSI stringValue];
        boards[device.identifier.UUIDString] = entry;
        NSLog(@"Found MetaWear: %@", device);
      }
      CDVPluginResult* pluginResult = nil;
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:boards];

      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
   }];

}

@end
