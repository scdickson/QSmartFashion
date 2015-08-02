//
//  MainViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse
import MMDrawerController

class MainViewController: UITableViewController {
    
    class CellIdentifiers {
        static let HeartMeasurement = "HeartMeasurementCell"
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        NSNotificationCenter.defaultCenter().addObserver(self, selector: "showAuthenticationController",
            name: QualcommNotification.User.DidLogout, object: nil)

        self.navigationItem.setLeftBarButtonItem(
            MMDrawerBarButtonItem(target: self, action: "openDrawer"),
            animated: false)
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        if PFUser.currentUser() == nil {
            print("no user is logged in, displaying authentication controller")
            showAuthenticationController()
        }
    }
    
    func showAuthenticationController() {
        self.performSegueWithIdentifier(QualcommSegue.ShowAuthenticationController, sender: self as AnyObject)
    }
    
    @IBAction func logOut(sender: AnyObject) {
        PFUser.logOut()
        NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.User.DidLogout, object: self as AnyObject)
    }
    
    func openDrawer() {
        self.mm_drawerController.openDrawerSide(.Left, animated: true) {
            (finished: Bool) -> Void in
            
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
