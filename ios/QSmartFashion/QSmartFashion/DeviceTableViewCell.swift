//
//  DeviceTableViewCell.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/5/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

class DeviceTableViewCell: UITableViewCell {

    @IBOutlet private var deviceNameLabel: UILabel!
    @IBOutlet private var deviceIdentifierLabel: UILabel!
    @IBOutlet private var actionButton: UIButton!
    var deviceName: String? {
        get {
            return deviceNameLabel.text
        }
        set(value) {
            deviceNameLabel.text = value
        }
    }
    var deviceIdentifier: String? {
        get {
            return deviceIdentifierLabel.text
        }
        set(value) {
            deviceIdentifierLabel.text = value
        }
    }
    var actionText: String? {
        get {
            return actionButton.titleLabel!.text
        }
        set(value) {
            actionButton.titleLabel!.text = value
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    @IBAction func deviceAction(sender: AnyObject) {
        print("connecting to device...")
    }
}
