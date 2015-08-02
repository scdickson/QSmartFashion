//
//  SFUser.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class SFUser: PFUser {
    @NSManaged var age: Int
    @NSManaged var weight: Double
    @NSManaged var height: Double
    @NSManaged var profilePicture: PFFile
    @NSManaged var sex: String
}
