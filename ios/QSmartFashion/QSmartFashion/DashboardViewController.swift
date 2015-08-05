//
//  DashboardViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/4/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

class DashboardViewController: UITableViewController {
    
    @IBOutlet var heartHistoryView: UIWebView!
    @IBOutlet var temperatureHistoryView: UIWebView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
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
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
