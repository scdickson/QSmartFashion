//
//  SFDataMeasurement.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class SFDataMeasurement: PFObject, PFSubclassing {
    
    @NSManaged var user: SFUser
    @NSManaged var heartrate: Double
    @NSManaged var temperature: Double
    @NSManaged var location: PFGeoPoint
    
    override init() {
        super.init()
    }
    
    init(user: SFUser) {
        super.init()
        self.user = user
    }
    
    override class func initialize() {
        struct Static {
            static var onceToken : dispatch_once_t = 0;
        }
        dispatch_once(&Static.onceToken) {
            self.registerSubclass()
        }
    }
    
    static func parseClassName() -> String {
        return "DataMeasurement"
    }
}
