#import "MWDevice.h"
#import "MWGyroscope.h"
#import "MBLGyro.h"
#import "MBLGyroData.h"

@implementation MWGyroscope {
  MWDevice *mwDevice;
}

- (id)initWithDevice:(MWDevice*)device
{
  if(self = [super init]){
    mwDevice = device;
  }
  return self;
}

- (void)startGyroscope:(CDVInvokedUrlCommand*)command
{
  NSLog(@"startAccelerometer called");
  [mwDevice.connectedDevice.gyro.dataReadyEvent startNotificationsWithHandlerAsync:^(MBLGyroData *gyroscopeData, NSError *error){
      CDVPluginResult* pluginResult = nil;
      NSLog(@"Gyroscope callback %@", gyroscopeData);
      NSMutableDictionary *gyroscopeReading = [NSMutableDictionary dictionaryWithDictionary:@{}];
      gyroscopeReading[@"x"] = [NSNumber numberWithFloat:gyroscopeData.x];
      gyroscopeReading[@"y"] = [NSNumber numberWithFloat:gyroscopeData.y];
      gyroscopeReading[@"z"] = [NSNumber numberWithFloat:gyroscopeData.z];
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:gyroscopeReading];
      [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];

      NSLog(@"Callback id %@", command.callbackId);
      [mwDevice.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)stopGyroscope:(CDVInvokedUrlCommand*)command
{
  NSLog(@"stopGyroscope called");
  [mwDevice.connectedDevice.gyro.dataReadyEvent stopNotificationsAsync];
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"GYROSCOPE_STOPPED"];

  [mwDevice.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
@end
