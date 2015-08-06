//
//  DashboardViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/4/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class DashboardViewController: UITableViewController, UIWebViewDelegate {
    
    enum HealthStatus {
        case Healthy
        case AtRisk
        case Unhealthy
    }
    
    var healthStatus: HealthStatus = .Healthy {
        willSet {
            switch newValue {
            case .Healthy:
                healthStatusLabel.text = "You are healthy!"
                healthStatusImageView.image = UIImage(named: "meter_happy")
                healthStatusContentView.backgroundColor = UIColor(red: 137.0/255.0, green: 166.0/255.0, blue: 71.0/255.0, alpha: 1.0)
            case .AtRisk:
                healthStatusLabel.text = "You are at risk!"
                healthStatusImageView.image = UIImage(named: "meter_meh")
                healthStatusContentView.backgroundColor = UIColor(red: 229.0/255.0, green: 161.0/255.0, blue: 58.0/255.0, alpha: 1.0)
            case .Unhealthy:
                healthStatusLabel.text = "You are unhealthy!"
                healthStatusImageView.image = UIImage(named: "meter_unhappy")
                healthStatusContentView.backgroundColor = UIColor(red: 233.0/255.0, green: 59.0/255.0, blue: 70.0/255.0, alpha: 1.0)
            }
        }
    }
    @IBOutlet private var healthStatusContentView: UIView!
    @IBOutlet private var healthStatusLabel: UILabel!
    @IBOutlet private var healthStatusImageView: UIImageView!
    
    @IBOutlet var heartrateLabel: UILabel!
    @IBOutlet var temperatureLabel: UILabel!
    @IBOutlet var heartHistoryView: UIWebView!
    @IBOutlet var temperatureHistoryView: UIWebView!

    var measurements = [SFDataMeasurement]()

    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        if PFUser.currentUser() != nil {
            loadMetricsFromParse()
        }
    }
    
    private func loadMetricsFromParse() {
        measurements.removeAll()
        let query = SFDataMeasurement.query()!
        query.whereKey("user", equalTo: PFUser.currentUser()!)
        query.orderByAscending("createdAt")
        query.limit = 15
        query.findObjectsInBackgroundWithBlock {
            (objects: [AnyObject]?, error: NSError?) in
            if error == nil {
                if let newMeasurements = objects as? [SFDataMeasurement] {
                    self.measurements += newMeasurements
                    self.updateInterface()
                } else {
                    fatalError("measurements aren't the right object?")
                }
            } else {
                print("unable to get data measurements from parse")
            }
        }
    }
    
    private func loadWebviews() {
        let baseUrl = NSBundle.mainBundle().bundleURL
        if let heartHistoryHtmlPath = NSBundle.mainBundle().pathForResource("cv", ofType: "html") {
            let temperatureHistoryHtmlPath = heartHistoryHtmlPath
            
            do {
                let heartHistoryHtml = try String(contentsOfFile: heartHistoryHtmlPath, encoding: NSUTF8StringEncoding)
                heartHistoryView.loadHTMLString(heartHistoryHtml, baseURL: baseUrl)
            } catch {
                print("unable to load heart history html")
            }
            
            do {
                let temperatureHistoryHtml = try String(contentsOfFile: temperatureHistoryHtmlPath, encoding: NSUTF8StringEncoding)
                temperatureHistoryView.loadHTMLString(temperatureHistoryHtml, baseURL: baseUrl)
            } catch {
                print("unable to load heart history html")
            }
        } else {
            fatalError("cannot find cv.html")
        }
    }
    
    private func updateInterface() {
        // update health status
        if let healthStatus = currentHealthStatus() {
            self.healthStatus = healthStatus
        }
        
        // update since last update icon @todo
        
        // update labels
        if let lastMeasurement = measurements.last {
            heartrateLabel.text = "\(lastMeasurement.heartrate) bpm"
            temperatureLabel.text = "\(UnitConverter.toFahrenheit(celsius: lastMeasurement.temperature))° F"
        } else {
            heartrateLabel.text = "-- bpm"
            temperatureLabel.text = "--° F"
        }
        
        loadWebviews() // update graphs
    }
    
    func getGraphComponents(f: (SFDataMeasurement) -> Double) -> (labels: [String], data: [String]) {
        var labels = [String]()
        var data = [String]()
        
        let nowDate = NSDate()
        let calendar = NSCalendar.currentCalendar()
        for measurement in measurements {
            // Construct Labels
            let hourComponents = calendar.components(NSCalendarUnit.Hour,
                fromDate: measurement.createdAt!, toDate: nowDate, options: NSCalendarOptions.MatchStrictly)
            let minuteComponents = calendar.components(NSCalendarUnit.Minute,
                fromDate: measurement.createdAt!, toDate: nowDate, options: NSCalendarOptions.MatchStrictly)
            let secondComponents = calendar.components(NSCalendarUnit.Second,
                fromDate: measurement.createdAt!, toDate: nowDate, options: NSCalendarOptions.MatchStrictly)
            
            if hourComponents.hour > 0 {
                labels.append("'\(hourComponents.hour) h'")
            } else if minuteComponents.minute > 0 {
                labels.append("'\(minuteComponents.minute) m'")
            } else if secondComponents.second > 0 {
                labels.append("'\(secondComponents.second) s'")
            }
            
            // Construct Data
            data.append("\(f(measurement))")
        }
        return (labels: labels, data: data)
    }
    
    func webViewDidFinishLoad(webView: UIWebView) {

        print("webview did load: \(webView)")
        
        enum MeasurementType {
            case HeartRate
            case Temperature
        }
        func javascript(forMeasurement measurementType: MeasurementType) -> String {
            let colorScheme: String
            let components: (labels: [String], data: [String])
            if measurementType == .HeartRate {
                colorScheme = "fillColor: 'rgba(233,59,70,0.4)', strokeColor: '#E2AB47', pointColor: '#fff', pointStrokeColor: '#E2AB47'"
                components = getGraphComponents {
                    (measurement: SFDataMeasurement) -> Double in
                    return measurement.heartrate
                }
            } else {
                colorScheme = "fillColor: 'rgba(17,178,178,0.4)', strokeColor: '#11b2b2', pointColor: '#fff', pointStrokeColor : '#11b2b2'"
                components = getGraphComponents {
                    (measurement: SFDataMeasurement) -> Double in
                    return UnitConverter.toFahrenheit(celsius: measurement.temperature)
                }
            }
            
            let joinedLabels = ",".join(components.labels)
            let joinedData = ",".join(components.data)
            let js = "var hrData = {" +
                "labels: [\(joinedLabels)], " +
                "datasets: [{" +
                colorScheme + ", " +
                "data : [\(joinedData)]" +
                "}]" +
                "}; " +
                "var hr = document.getElementById('chart').getContext('2d'); " +
            "new Chart(hr).Line(hrData);"

            return js
        }
        
        
        if webView == heartHistoryView {
            print("loading heart history")
            heartHistoryView.stringByEvaluatingJavaScriptFromString(javascript(forMeasurement: .HeartRate))
            temperatureHistoryView.stringByEvaluatingJavaScriptFromString(javascript(forMeasurement: .Temperature))
        } else if webView == temperatureHistoryView {
            print("loading temperature history")
            heartHistoryView.stringByEvaluatingJavaScriptFromString(javascript(forMeasurement: .HeartRate))
            temperatureHistoryView.stringByEvaluatingJavaScriptFromString(javascript(forMeasurement: .Temperature))
        } else {
            print("unknown web view")
        }
    }
    
    // MARK: - Helpers
    func currentHealthStatus() -> HealthStatus? {
        guard let user = PFUser.currentUser() as? SFUser else {
            print("currentHealthStatus: no user is logged in")
            return nil
        }
        guard let lastMeasurement = measurements.last else {
            print("currentHealthStatus: there is no last measurement")
            return nil
        }
        
        let nowDate = NSDate()
        let calendar = NSCalendar.currentCalendar()
        
        let age: Double = Double(calendar.components(NSCalendarUnit.Year,
                fromDate: user.birthdate, toDate: nowDate, options: NSCalendarOptions.MatchStrictly).year)
        
        let maxHeartrate: Double
        if user.sex == "M" {
            maxHeartrate = 190.2 / (1 + pow(M_E, 0.0453 * (age - 107.5)));
        } else {
            maxHeartrate = 203.7 / (1 + pow(M_E, 0.033 * (age - 104.3)));
        }
        let minHeartrate = maxHeartrate * 0.35
        
        if (lastMeasurement.temperature <= 36.5 - 36.5 * 0.1 ||
            lastMeasurement.temperature >= 37.5 + 37.5 * 0.1 ||
            lastMeasurement.heartrate <= minHeartrate - minHeartrate * 0.1 ||
            lastMeasurement.heartrate >= maxHeartrate + maxHeartrate * 0.1) {
                return .Unhealthy
        } else if (lastMeasurement.temperature < 36.5 && lastMeasurement.temperature > 36.5 - 36.5 * 0.1) {
            return .AtRisk
        } else if (lastMeasurement.temperature > 37.5 && lastMeasurement.temperature < 37.5 + 37.5 * 0.1) {
            return .AtRisk
        } else if (lastMeasurement.heartrate < minHeartrate && lastMeasurement.heartrate > minHeartrate - minHeartrate * 0.1) {
            return .AtRisk
        } else if (lastMeasurement.heartrate > maxHeartrate && lastMeasurement.heartrate < maxHeartrate + maxHeartrate * 0.1) {
            return .AtRisk
        } else {
            return .Healthy
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
