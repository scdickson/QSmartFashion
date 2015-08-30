//
//  AppDelegate.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class QualcommSegue {
    static let ShowBirthdatePickerController = "com.qualcomm.sf.segue.profile.selectbirthdate"
    static let ShowAuthenticationController = "com.qualcomm.segue.requireauthentication"
    static let ShowSettingsController = "com.qualcomm.segue.showsettings"
}

class QualcommNotification {
    class User {
        static let DidLogout = "com.qualcomm.notification.user.didlogout"
        static let DidLogin = "com.qualcomm.notification.user.didlogin"
    }
    class BTLE {
        static let FoundPeripheral = "com.qualcomm.qsf.notification.btle.foundperipheral"
        static let PeripheralStateChanged = "com.qualcomm.qsf.notification.btle.statechanged"
    }
    class Data {
        static let NewMeasurement = "com.qualcomm.qsf.notification.data.newmeasurement"
    }
}

class ViewControllerIdentifier {
    static let Authentication = "com.qualcomm.viewcontroller.authentication"
    static let Dashboard = "com.qualcomm.viewcontroller.dashboard"
    static let Profile = "com.qualcomm.viewcontroller.profile"
    static let Devices = "com.qualcomm.viewcontroller.devices"
    static let EmergencyContacts = "com.qualcomm.viewcontroller.contacts"
    static let Drawer = "com.qualcomm.viewcontroller.drawer"
}

class TableViewCellIdentifier {
    static let Contact = "com.qualcomm.tableviewcell.contact"
    static let Device = "com.qualcomm.qsf.tableviewcell.device"
}

let mainStoryboard = UIStoryboard(name: "Main", bundle: nil)
let orphanStoryboard = UIStoryboard(name: "Orphan", bundle: nil)

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var containerViewController: ContainerViewController!
    
    class ParseAPICredentials {
        static let ApplicationId = "<PARSE_APP_KEY>"
        static let ClientKey = "<PARSE_CLIENT_KEY>"
    }

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {

        SFUser.registerSubclass()
        Parse.setApplicationId(ParseAPICredentials.ApplicationId, clientKey: ParseAPICredentials.ClientKey)
        
        self.containerViewController = ContainerViewController()
        
        self.window = UIWindow(frame: UIScreen.mainScreen().bounds)
        if let window = window {
            window.rootViewController = self.containerViewController
            window.makeKeyAndVisible()
        } else {
            fatalError("where is our window?")
        }
        
        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

