//
//  ContainerViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/2/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import MMDrawerController
import Parse

class ContainerViewController: MMDrawerController {
    
    enum ControllerState: Int {
        case Dashboard = 0
        case Profile
        case Devices
        case EmergencyContacts
        case Logout
    }
    
    private var drawerController: DrawerTableViewController!
    
    private var dashboardController: UINavigationController!
    private var profileController: UINavigationController!
    private var devicesController: UINavigationController!
    private var contactsController: UINavigationController!

    init() {
        let drawerController = orphanStoryboard.instantiateViewControllerWithIdentifier(ViewControllerIdentifier.Drawer) as! DrawerTableViewController
        
        super.init(
            centerViewController: UIViewController(),
            leftDrawerViewController: drawerController,
            rightDrawerViewController: nil
        )
        
        self.drawerController = drawerController
        drawerController.containerViewController = self
        
        // Instantiate view controllers
        self.dashboardController = mainStoryboard.instantiateViewControllerWithIdentifier(ViewControllerIdentifier.Dashboard) as! UINavigationController
        
        self.profileController = orphanStoryboard.instantiateViewControllerWithIdentifier(ViewControllerIdentifier.Profile) as! UINavigationController
        
        self.devicesController = orphanStoryboard.instantiateViewControllerWithIdentifier(ViewControllerIdentifier.Devices) as! UINavigationController
        
        self.contactsController = orphanStoryboard.instantiateViewControllerWithIdentifier(ViewControllerIdentifier.EmergencyContacts) as! UINavigationController
        
        // Configure MMDrawerController
        self.openDrawerGestureModeMask = .BezelPanningCenterView
        self.closeDrawerGestureModeMask = .All
        self.centerHiddenInteractionMode = .None
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        switchToController(.Dashboard)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func switchToController(state: ControllerState) {
        switch state {
        case .Dashboard:
            centerViewController = dashboardController
        case .Profile:
            centerViewController = profileController
        case .Devices:
            centerViewController = devicesController
        case .EmergencyContacts:
            centerViewController = contactsController
        case .Logout:
            PFUser.logOut()
            NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.User.DidLogout, object: self as AnyObject)
        }
        
        if state != .Logout {
            if let viewController = centerViewController.childViewControllers.first {
                viewController.navigationItem.setLeftBarButtonItem(
                    MMDrawerBarButtonItem(target: self, action: "openDrawer"),
                    animated: false)
            }
        }
        
        closeDrawer()
    }
    
    func openDrawer() {
        openDrawerSide(.Left, animated: true) {
            (finished: Bool) in
            // @stub
        }
    }
    
    func closeDrawer() {
        closeDrawerAnimated(true) { (finished: Bool) in
            // @stub
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
