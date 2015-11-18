#import "MWDevice.h"
#import <Cordova/CDV.h>
#import "MBLMetaWear.h"
#import "MBLMetaWearManager.h"


@implementation MWDevice {
  NSArray *scannedDevices;
  MBLMetaWear *connectedDevice;
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
        [device rememberDevice];
      }
      CDVPluginResult* pluginResult = nil;
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:boards];

      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}

- (void)connect:(CDVInvokedUrlCommand*)command
{
  NSString* uUIDString = [command.arguments objectAtIndex:0];

  [[MBLMetaWearManager sharedManager] retrieveSavedMetaWearsWithHandler:^(NSArray *array) {
      __block CDVPluginResult *pluginResult = nil;

      bool foundDevice = false;
      for (MBLMetaWear *device in array){
        if([device.identifier.UUIDString isEqualToString:uUIDString]){
          foundDevice = true;
          [device connectWithTimeout:20 handler:^(NSError *error) {
              if ([error.domain isEqualToString:kMBLErrorDomain] &&
                  error.code == kMBLErrorConnectionTimeout) {
                NSLog(@"Connection Timeout");
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"ERROR"];
              }
              else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"CONNECTED"];
                connectedDevice = device;
              }
              [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }];
        }
      }
      if(foundDevice == false){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
      }
    }];
}

- (void)disconnect:(CDVInvokedUrlCommand*)command
{
  __block CDVPluginResult* pluginResult = nil;

  NSLog(@"disconnecting from metawear");
  [connectedDevice disconnectWithHandler:^(NSError *error) {
      if ([error.domain isEqualToString:kMBLErrorDomain] &&
          error.code == kMBLErrorConnectionTimeout) {
        NSLog(@"Disconnect Problem");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"ERROR"];
      }
      else {
        NSLog(@"disconnecting");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DISCONNECTED"];
        connectedDevice = nil;
      }
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

@end
