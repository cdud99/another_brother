//
//  PrintPdfFileMethodCall.m
//  another_brother
//
//  Created by admin on 4/15/21.
//

#import <Foundation/Foundation.h>
#import "PrintPdfFileMethodCall.h"

@implementation PrintPdfFileMethodCall
static NSString * METHOD_NAME = @"printPdfFile";

- (instancetype)initWithCall:(FlutterMethodCall *)call
                      result:(FlutterResult) result {
    self = [super init];
        if (self) {
            _call = call;
            _result = result;
            
        }
        return self;
}

+ (NSString *) METHOD_NAME {
    return METHOD_NAME;
}
- (void)execute {
    // TODO Move to background thread.
    // Get printInfo dart params from call
    NSDictionary<NSString *, NSObject *> * dartPrintInfo = _call.arguments[@"printInfo"];
    // Get file path from call
    NSString * filePath = _call.arguments[@"filePath"];
    // Get page number
    NSNumber * pageNum = _call.arguments[@"pagenum"];
    
    
    // TODO Get channel from printInfo
    BRLMChannel *channel = [BrotherUtils printChannelWithPrintSettingsMap:dartPrintInfo];
    
    // TODO Generate printer driver
    BRLMPrinterDriverGenerateResult * driverGenerateResult = [BRLMPrinterDriverGenerator openChannel:channel];
        if (driverGenerateResult.error.code != BRLMOpenChannelErrorCodeNoError ||
            driverGenerateResult.driver == nil) {
            
            // On Error report error
            NSDictionary<NSString *, NSObject *> * printStatus = [BrotherUtils printerStatusToMapWithError:BRLMPrintErrorCodePrinterStatusErrorCommunicationError  status:nil];
            _result(printStatus);
            return;
        }

    
    BRLMPrinterDriver *printerDriver = driverGenerateResult.driver;
    
    // Get printer settings.
    id<BRLMPrintSettingsProtocol>  printerSettings = [BrotherUtils printSettingsFromMapWithValue:dartPrintInfo];
    
    // If no error create URL from path
    
    NSURL * url = [NSURL fileURLWithPath:filePath];
    
    // Call print method
    BRLMPrintError * printError = [printerDriver printPDFWithURL:url pages:[NSArray arrayWithObject:pageNum] settings:printerSettings];

    
    [printerDriver closeChannel];
    
    // Notify status to Flutter.
    NSDictionary<NSString *, NSObject *> * printStatus = [BrotherUtils printerStatusToMapWithError:printError.code  status:nil];
    
    _result(printStatus);
   
}


@end
