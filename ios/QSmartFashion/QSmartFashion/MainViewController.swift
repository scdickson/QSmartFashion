//
//  MainViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class MainViewController: UITableViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        NSNotificationCenter.defaultCenter().addObserver(self, selector: "showAuthenticationController",
            name: QualcommNotification.User.DidLogout, object: nil)
        // Do any additional setup after loading the view.
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

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
