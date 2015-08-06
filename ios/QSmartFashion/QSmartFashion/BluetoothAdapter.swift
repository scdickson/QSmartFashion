//
//  BluetoothAdapter.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/4/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import CoreBluetooth

protocol BluetoothAdapterDelegate {
    func didReceiveMeasurement(heartrate: Double, temperature: Double)
    func deviceConnected(peripheral: CBPeripheral)
    func deviceDisconnected(peripheral: CBPeripheral)
}

class BluetoothAdapter: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    static let sharedInstance = BluetoothAdapter()
    private let discoverAllServices = true
    
    var delegate: BluetoothAdapterDelegate?
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
        
//        let scanOptions: [String : AnyObject] = [
//            CBCentralManagerScanOptionAllowDuplicatesKey : true,
//            CBCentralManagerOptionRestoreIdentifierKey: restorationIdentifier
//        ]
        
        if discoverAllServices {
            centralManager.scanForPeripheralsWithServices(nil, options: nil)
        } else {
            centralManager.scanForPeripheralsWithServices([UUID.Service], options: nil)
        }
        print("scanning for peripherals")
    }
    
    func stopScanningForPeripherals() {
        centralManager.stopScan()
    }
    
    func connectToPeripheral(peripheral: CBPeripheral) {
        if let name = peripheral.name {
            print("connecting to peripheral \(name)")
            let connectOptions: [String : AnyObject] = [
                CBConnectPeripheralOptionNotifyOnConnectionKey : true,
                CBConnectPeripheralOptionNotifyOnDisconnectionKey : true,
                CBConnectPeripheralOptionNotifyOnNotificationKey : true
            ]
            
            self.centralManager.connectPeripheral(peripheral, options: connectOptions)
        } else {
            print("not safe to connect to an unnamed peripheral") // @todo: display error
        }
    }
    
    func disconnectFromPeripheral(peripheral: CBPeripheral) {
        self.centralManager.cancelPeripheralConnection(peripheral)
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
        NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.BTLE.PeripheralStateChanged, object: nil)
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber) {
        // @todo
        
        if peripheral.name != nil {
            print("found peripheral: \(peripheral.name!) - \(peripheral.identifier)")
            
            self.discoveredPeripherals[peripheral.identifier.UUIDString] = peripheral
        }
        
        NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.BTLE.FoundPeripheral, object: self)
        
//        let connectOptions: [String : AnyObject] = [
//            CBConnectPeripheralOptionNotifyOnConnectionKey : true,
//            CBConnectPeripheralOptionNotifyOnDisconnectionKey : true,
//            CBConnectPeripheralOptionNotifyOnNotificationKey : true
//        ]
        
//        if peripheral.identifier == UUID.Descriptor {
//            print("connecting to peripheral... (\(peripheral.name) - \(peripheral.identifier.UUIDString))")
//            central.connectPeripheral(peripheral, options: connectOptions)
//            peripheral.delegate = self
//        }
    }
    
    func centralManager(central: CBCentralManager, didConnectPeripheral peripheral: CBPeripheral) {
        print("peripheral – \(peripheral.name!): connected")
        peripheral.delegate = self
        peripheral.discoverServices([UUID.Service])
        delegate?.deviceConnected(peripheral)
    }
    
    func centralManager(central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        print("peripheral – \(peripheral.name!): disconnected")
        delegate?.deviceDisconnected(peripheral)
    }
    
    func centralManager(central: CBCentralManager, didFailToConnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        print("peripheral – \(peripheral.name!): failed to connect")
    }
    
    func centralManager(central: CBCentralManager, willRestoreState dict: [String : AnyObject]) {
        print("central – restoring state")
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverServices error: NSError?) {
        guard error == nil else {
            print("peripheral – \(peripheral.name!): discover services error")
            return
        }
        
        print("peripheral – \(peripheral.name!): did discover \(peripheral.services!.count) services")
        
        for service in peripheral.services! {
            if service.UUID == UUID.Service {
                peripheral.discoverCharacteristics([UUID.RX], forService: service)
            }
        }
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverCharacteristicsForService service: CBService, error: NSError?) {
        guard error == nil else {
            print("peripheral – \(peripheral.name!): discover characteristics error")
            return
        }
        
        print("peripheral – \(peripheral.name!): service: did discover \(service.characteristics!.count) characteristics")
        
        for characteristic in service.characteristics! {
            peripheral.setNotifyValue(true, forCharacteristic: characteristic)
            print("peripheral – \(peripheral.name!): set notify value")
        }
    }
    
    func peripheral(peripheral: CBPeripheral, didUpdateNotificationStateForCharacteristic characteristic: CBCharacteristic, error: NSError?) {
        guard error == nil else {
            print("peripheral – \(peripheral.name!): characteristic notification state update error")
            return
        }
        guard characteristic.UUID == UUID.RX else {
            print("peripheral – \(peripheral.name!): unknown characteristic notification state update")
            return
        }
        
        if characteristic.isNotifying {
            print("peripheral – \(peripheral.name!): notifications began")
            peripheral.readValueForCharacteristic(characteristic)
        } else {
            print("peripheral – \(peripheral.name!): notifications ended")
            self.centralManager.cancelPeripheralConnection(peripheral)
        }
    }
    
    var dataBuffer = [UInt8]()
    func peripheral(peripheral: CBPeripheral, didUpdateValueForCharacteristic characteristic: CBCharacteristic, error: NSError?) {
        guard error == nil else {
            print("peripheral – \(peripheral.name!): did update value for characteristic error")
            return
        }
        
        if let characteristicValue = characteristic.value {
//            characteristicValue.bytes
            print("got \(characteristicValue.length) bytes of data:")
            
            let dataLength = characteristicValue.length
            var byteArray = [UInt8](count: dataLength, repeatedValue: 0x0)
            characteristicValue.getBytes(&byteArray, length: characteristicValue.length)
            
            for value in byteArray {
                dataBuffer.append(value)
            }
            
            while dataBuffer.count >= 7 {
                let bytes = Array(dataBuffer[0..<7])
                if let string = NSString(bytes: bytes, length: 7, encoding: NSASCIIStringEncoding) where string.length > 0 {
                    let seperatorIndex = string.rangeOfString(":").location
                    let heartrate = (string.substringToIndex(seperatorIndex) as NSString).doubleValue
                    let temperature = (string.substringFromIndex(seperatorIndex + 1) as NSString).doubleValue
                    delegate?.didReceiveMeasurement(heartrate, temperature: temperature)
                }
                dataBuffer.removeRange(0..<7)
            }
            
//            var hexBits = "" as String
//            for value in byteArray {
//                hexBits += NSString(format:"%2X", value) as String
//            }
//            let hexBytes = hexBits.stringByReplacingOccurrencesOfString(" ", withString: "0", options: NSStringCompareOptions.CaseInsensitiveSearch)
//            print("bytes: \(hexBytes)")

//            for var i = 0; i < characteristicValue.length; i++ {
//                let character: Character = Character(UnicodeScalar(bytes[i]))
//                print("byte \(i): \(bytes.))")
//            }
//            print(characteristicValue)
//            let value = NSString(data: characteristicValue, encoding: NSUTF8StringEncoding)?
//            print("peripheral – \(peripheral.name!): got value: '\(value!)'")
        } else {
            print("peripheral – \(peripheral.name!): no value")
        }
    }
}
