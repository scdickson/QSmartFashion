//
//  BluetoothAdapter.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/4/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import CoreBluetooth

class BluetoothAdapter: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    static let sharedInstance = BluetoothAdapter()
    private let discoverAllServices = true
    
    private var centralManager: CBCentralManager!
    private var peripheral: CBPeripheral?
    var discoveredPeripherals = [String : CBPeripheral]()
    var discoveredPeripheralUUIDs: [String] {
        return Array(discoveredPeripherals.keys)
    }
    
    let restorationIdentifier = "com.qualcomm.qsf.btle.central.restoration-identifier"
    class UUID {
        static let Service = CBUUID(string: "713D0000-503E-4C75-BA94-3148F18D941E")
        static let RX = CBUUID(string: "713D0002-503E-4C75-BA94-3148F18D941E")
        static let TX = CBUUID(string: "713D0003-503E-4C75-BA94-3148F18D941E")
        static let Descriptor = CBUUID(string: "00002902-0000-1000-8000-00805f9b34fb")
    }
    
    private override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func beginScanningForPeripherals() {
        guard centralManager.state == .PoweredOn else {
            print("cannot begin scanning when bt le is not powered on")
            return
        }
        
        let scanOptions: [String : AnyObject] = [
//            CBCentralManagerScanOptionAllowDuplicatesKey : true,
            CBCentralManagerOptionRestoreIdentifierKey: restorationIdentifier
        ]
        
        if discoverAllServices {
            centralManager.scanForPeripheralsWithServices(nil, options: scanOptions)
        } else {
            centralManager.scanForPeripheralsWithServices([UUID.Service], options: scanOptions)
        }
        print("scanning for peripherals")
    }
    
    func stopScanningForPeripherals() {
        centralManager.stopScan()
    }
    
    // MARK: - CBCentralManagerDelegate
    @objc func centralManagerDidUpdateState(central: CBCentralManager) {
        // @todo
        switch central.state {
        case CBCentralManagerState.PoweredOn:
            print("bt le powered on")
            beginScanningForPeripherals()
        case CBCentralManagerState.Unauthorized:
            print("app is not authorized to use bt le")
        case CBCentralManagerState.Unsupported:
            print("bt le is unsupported")
        default:
            print("unknown state")
        }
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber) {
        // @todo
        
        print("found peripheral: \(peripheral.name) - \(peripheral.identifier)")
        
        self.discoveredPeripherals[peripheral.identifier.UUIDString] = peripheral
        
        let connectOptions: [String : AnyObject] = [
            CBConnectPeripheralOptionNotifyOnConnectionKey : true,
            CBConnectPeripheralOptionNotifyOnDisconnectionKey : true,
            CBConnectPeripheralOptionNotifyOnNotificationKey : true
        ]
        
        if peripheral.identifier == UUID.Descriptor {
            print("connecting to peripheral... (\(peripheral.name) - \(peripheral.identifier.UUIDString))")
            central.connectPeripheral(peripheral, options: connectOptions)
            peripheral.delegate = self
        }
    }
    
    func centralManager(central: CBCentralManager, didConnectPeripheral peripheral: CBPeripheral) {
        // @todo
    }
    
    func centralManager(central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        // @todo
    }
    
    func centralManager(central: CBCentralManager, didFailToConnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        // @todo
    }
    
    func centralManager(central: CBCentralManager, willRestoreState dict: [String : AnyObject]) {
        // @todo
    }
}
