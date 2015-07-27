//
//  AuthenticationViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 7/26/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

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
    
    func showValidationAlert(message: String) {
        
    }

    @IBAction func logIn(sender: AnyObject) {
        dismissKeyboard()
        
    }

    @IBAction func signUp(sender: AnyObject) {
        dismissKeyboard()
        
        let user = SFUser()
        
        if let string = usernameField.text where !string.isEmpty {
            user.username = string
            user.email = string
        } else {
            print("email is empty")
            // @todo: create an alert to the user
            // and focus the email field
            return
        }
        
        if let string = passwordField.text where !string.isEmpty {
            user.password = string
        } else {
            print("password is empty")
            // @todo: create an alert to the user
            // and focus the password field
            return
        }

        user.age = 22
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
}

