//
//  DevicesViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/5/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

class DevicesViewController: UITableViewController {

    let bluetoothAdapter = BluetoothAdapter.sharedInstance
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "refreshDevices", name: QualcommNotification.BTLE.FoundPeripheral, object: bluetoothAdapter)
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return bluetoothAdapter.discoveredPeripherals.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(TableViewCellIdentifier.Device,
            forIndexPath: indexPath) as! DeviceTableViewCell
        
        let peripheralUUID = bluetoothAdapter.discoveredPeripheralUUIDs[indexPath.row]
        if let peripheral = bluetoothAdapter.discoveredPeripherals[peripheralUUID] {
            if let name = peripheral.name {
                cell.deviceName = name
            } else {
                cell.deviceName = "Unknown Device"
            }
            cell.deviceIdentifier = peripheral.identifier.UUIDString
            switch peripheral.state {
            case .Connected:
                cell.actionText = "Disconnect"
            case .Disconnected:
                cell.actionText = "Connect"
            case .Connecting:
                cell.actionText = "Connecting"
            case .Disconnecting:
                cell.actionText = "Disconnecting"
            }
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        let peripheralUUID = bluetoothAdapter.discoveredPeripheralUUIDs[indexPath.row]
        if let peripheral = bluetoothAdapter.discoveredPeripherals[peripheralUUID] {
            if peripheral.state == .Connected {
                bluetoothAdapter.disconnectFromPeripheral(peripheral)
            } else if peripheral.state == .Disconnected {
                bluetoothAdapter.connectToPeripheral(peripheral)
            }
        }
    }
    
    func refreshDevices() {
        tableView.reloadData()
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
