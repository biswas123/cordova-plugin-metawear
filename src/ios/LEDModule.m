#import "MWDevice.h"
#import "MWAccelerometer.h"
#import "MBLLED.h"

@implementation LEDModule {
  MWDevice *mwDevice;
}

- (id)initWithDevice:(MWDevice*)device
{
  if(self = [super init]){
    mwDevice = device;
  }
  return self;
}

- (void)playLED:(CDVInvokedUrlCommand*)command
{
  NSLog(@"playLED called");
  NSMutableDictionary* arguments = [command.arguments objectAtIndex:0];
  NSString *stringChannel = arguments[@"channel"];
  NSInteger *highIntensity = arguments[@"highIntensity"] ? arguments[@"highIntensity"] : 0;
  NSInteger *lowIntensity = arguments[@"lowIntensity"] ? arguments[@"lowIntensity"] : 0;
  NSInteger *repeatCount = arguments[@"repeatCount"] ? arguments[@"repeatCount"] : 0;
  NSInteger *pulseDuration = arguments[@"pulseDuration"] ? arguments[@"pulseDuration"] : 0;
  NSInteger *riseTime = arguments[@"riseTime"] ? arguments[@"riseTime"] : 0;
  NSInteger *highTime = arguments[@"highTime"] ? arguments[@"highTime"] : 0;
  NSInteger *fallTime = arguments[@"fallTime"] ? arguments[@"fallTime"] : 0;

  UIColor *channel = nil;
  if ([stringChannel isEqualToString:@"RED"]){
    channel = redColor;
  } else if ([stringChannel isEqualToString:@"GREEN"]){
    channel = greenColor;
  } else if ([stringChannel isEqualToString:@"BLUE"]){
    channel = blueColor;
  }

  [mwDevice.connecedDevice.led [UIColor channel] onIntensity:highIntensity,
                                                offIntensity:lowIntensity,
                                                    riseTime:riseTime,
                                                    fallTime:fallTime,
                                                      onTime:highTime,
                                                      offset:pulseDuration,
                                                 repeatCount:repeatCount];

  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"LED_STOPPED"];

  [mwDevice.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

  /*channel, highIntensity, lowIntensity,
    repeatCount, pulseDuration, riseTime,
    highTime, fallTime*/
}

- (void)stopLED:(CDVInvokedUrlCommand*)command
{
  NSLog(@"stopLED called");
  [mwDevice.connectedDevice.led setLEDOnAsync:no withOptions:1];
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"LED_STOPPED"];

  [mwDevice.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
@end
