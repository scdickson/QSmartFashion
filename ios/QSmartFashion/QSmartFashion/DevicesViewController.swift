//
//  DevicesViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/5/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import CoreBluetooth
import Parse

class DevicesViewController: UITableViewController, BluetoothAdapterDelegate {

    let bluetoothAdapter = BluetoothAdapter.sharedInstance
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "refreshDevices", name: QualcommNotification.BTLE.FoundPeripheral, object: bluetoothAdapter)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "refreshDevices", name: QualcommNotification.BTLE.PeripheralStateChanged, object: bluetoothAdapter)
        
        bluetoothAdapter.delegate = self
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
    
    // MARK: - BluetoothAdapterDelegate
    var counter: Int = 10
    func didReceiveMeasurement(heartrate: Double, temperature: Double) {
        guard let user = PFUser.currentUser() else {
            print("don't have user")
            counter = 0
            return
        }
        
        if counter > 0 {
            print("discarding \(counter--)th measurement")
        } else {
            print("got measurement: \(heartrate), \(temperature)")
            let measurement = SFDataMeasurement(user: user)
            measurement.heartrate = heartrate
            measurement.temperature = temperature
            measurement.saveInBackgroundWithBlock {
                (succeeded: Bool, error: NSError?) in
                if succeeded {
                    print("saved btle measurement")
                    NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.Data.NewMeasurement, object: nil)
                } else {
                    print("unable to save btle measurement")
                }
            }
        }
    }
    
    func deviceConnected(peripheral: CBPeripheral) {
        let alertController = UIAlertController(title: "Device", message: "Device was connected", preferredStyle: UIAlertControllerStyle.Alert)
        alertController.addAction(UIAlertAction(title: "Dismiss", style: UIAlertActionStyle.Default, handler: { (action: UIAlertAction) -> Void in
            //
        }))
        self.presentViewController(alertController, animated: true, completion: nil)
    }
    
    func deviceDisconnected(peripheral: CBPeripheral) {
        let alertController = UIAlertController(title: "Device", message: "Device was disconnected", preferredStyle: UIAlertControllerStyle.Alert)
        alertController.addAction(UIAlertAction(title: "Dismiss", style: UIAlertActionStyle.Default, handler: { (action: UIAlertAction) -> Void in
            //
        }))
        self.presentViewController(alertController, animated: true, completion: nil)
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
