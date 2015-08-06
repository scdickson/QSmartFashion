//
//  SimulateReadingViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/5/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class SimulateReadingViewController: UIViewController {
    
    @IBOutlet var heartRateLabel: UITextField!
    @IBOutlet var temperatureLabel: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    @IBAction func sendSimulatedReading(sender: AnyObject) {
        guard let heartrateString = heartRateLabel.text else {
            print("there was no heartrate")
            return
        }
        guard let temperatureString = temperatureLabel.text else {
            print("there was no heartrate")
            return
        }
        guard let heartrate = Double(heartrateString) else {
            print("heartrate could not convert to a number")
            return
        }
        guard let temperature = Double(temperatureString) else {
            print("temperature could not convert to a number")
            return
        }
        
        func sendMeasurement(withLocation location: PFGeoPoint?) {
            let dataMeasurement = SFDataMeasurement(user: PFUser.currentUser()!)
            dataMeasurement.heartrate = heartrate
            dataMeasurement.temperature = UnitConverter.toCelsius(fahrenheit: temperature)
            if let location = location {
                print("got location")
                dataMeasurement.lat = location.latitude
                dataMeasurement.lng = location.longitude
            } else {
                print("unable to get location")
            }
            
            dataMeasurement.saveInBackgroundWithBlock {
                (succeeded: Bool, error: NSError?) -> Void in
                if succeeded {
                    print("saved simulated reading")
                } else {
                    print("unable to save simulated reading")
                }
            }
        }
        
        let useLocationServices = false
        
        if useLocationServices {
            print("trying to get location...")
            PFGeoPoint.geoPointForCurrentLocationInBackground {
                (geoPoint: PFGeoPoint?, error: NSError?) -> Void in
                if error == nil {
                    sendMeasurement(withLocation: geoPoint)
                } else {
                    sendMeasurement(withLocation: nil)
                }
            }
        } else {
            sendMeasurement(withLocation: nil)
        }
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
