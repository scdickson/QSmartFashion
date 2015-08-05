//
//  AuthenticationViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class AuthenticationViewController: UIViewController {

    @IBOutlet var usernameField: UITextField!
    @IBOutlet var passwordField: UITextField!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func dismissKeyboard() {
        if usernameField.isFirstResponder() {
            usernameField.resignFirstResponder()
        }
        if passwordField.isFirstResponder() {
            passwordField.resignFirstResponder()
        }
    }
    
    private func showValidationAlert(message: String) {
        
    }
    
    func validate(inout username: String, inout password: String) -> Bool { // @todo make this throw an exception
        if let string = usernameField.text where !string.isEmpty {
            username = string
        } else {
            print("email is empty")
            // @todo: create an alert to the user
            // and focus the email field
            return false
        }
        
        if let string = passwordField.text where !string.isEmpty {
            password = string
        } else {
            print("password is empty")
            // @todo: create an alert to the user
            // and focus the password field
            return false
        }
        return true
    }

    @IBAction func logIn(sender: AnyObject) {
        dismissKeyboard()
        
        var username = "", password = ""
        if validate(&username, password: &password) {
            PFUser.logInWithUsernameInBackground(username, password: password) {
                (user: PFUser?, logInError: NSError?) -> Void in
                if let _ = user {
                    self.presentingViewController!.dismissViewControllerAnimated(true) {
                        NSNotificationCenter.defaultCenter().postNotificationName(QualcommNotification.User.DidLogin, object: self)
                    }
                } else {
                    print("login error: \(logInError!.description)")
                }
            }
        }
    }

    @IBAction func signUp(sender: AnyObject) {
        dismissKeyboard()
        
        let user = SFUser()
        
        var _username = "", _password = ""
        if validate(&_username, password: &_password) {
            user.username = _username
            user.email = _username
            user.password = _password
        } else {
            print("signup error")
            return
        }

        user.birthdate = NSDate(timeIntervalSince1970: NSTimeInterval(0))
        user.weight = 195
        user.height = 176
        
        user.signUpInBackgroundWithBlock {
            (succeeded: Bool, error: NSError?) -> Void in
            if let error = error {
                let errorString = error.userInfo["error"] as? NSString
                print(errorString)
                // Show the errorString somewhere and let the user try again.
            } else {
                print("successfully created user.")
                
                self.performSegueWithIdentifier(QualcommSegue.ShowSettingsController, sender: self as AnyObject)
                
//                // Creating dummy classes
//                let dataMeasurement = SFDataMeasurement(user: user)
//                dataMeasurement.temperature = 76
//                dataMeasurement.heartrate = 43
//                dataMeasurement.location = PFGeoPoint(latitude: 88, longitude: 88)
//                
//                let emergencyContact = SFEmergencyContact(user: user)
//                emergencyContact.name = "John Doe"
//                emergencyContact.phoneNumber = "18008675309"
//                
//                dataMeasurement.save()
//                emergencyContact.save()
            }
        }
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return .LightContent
    }
}

