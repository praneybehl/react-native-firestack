//
//  FirestackAuth.h
//  Firestack
//
//  Created by Ari Lerner on 8/23/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "Firebase.h"
#import "RCTEventEmitter.h"
#import "RCTBridgeModule.h"

@interface FirestackAuth : RCTEventEmitter <RCTBridgeModule> {
    FIRAuthStateDidChangeListenerHandle authListenerHandle;
}

@end
