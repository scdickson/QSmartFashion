//
//  ProfileViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/3/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

class ProfileViewController: UIViewController, UITextFieldDelegate {

    @IBOutlet var dismissKeyboardBarButtonItem: UIBarButtonItem!
    
    var currentUser: SFUser!
    @IBOutlet var nameTextField: UITextField!
    @IBOutlet var sexSegmentedControl: UISegmentedControl!
    @IBOutlet var weightTextField: UITextField!
    @IBOutlet var feetTextField: UITextField!
    @IBOutlet var inchTextField: UITextField!
    @IBOutlet var birthdateTextField: UITextField!
    var birthdate: NSDate? {
        willSet {
            if let date = newValue {
                birthdateTextField.text = date.description
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.rightBarButtonItem = nil
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let user = PFUser.currentUser() as? SFUser {
            currentUser = user
            
            nameTextField.text = user.name
            switch user.sex {
            case "M":
                sexSegmentedControl.selectedSegmentIndex = 1
            case "F":
                sexSegmentedControl.selectedSegmentIndex = 0
            default:
                break
            }
            let weight = UnitConverter.toPounds(kilograms: currentUser.weight)
            weightTextField.text = String(weight)
            let height = UnitConverter.toImperial(centimeters: currentUser.height)
            feetTextField.text = String(height.feet)
            inchTextField.text = String(height.inches)
            birthdate = user.birthdate
        } else {
            print("no user...?")
        }
    }
    
    var respondableControl: UIResponder? // The current control that's being edited.
    @IBAction func editingDidBegin(sender: UIResponder) {
        respondableControl = sender
        navigationItem.setRightBarButtonItem(dismissKeyboardBarButtonItem, animated: true)
    }
    
    @IBAction func editingDidEnd(sender: UIControl) {
        navigationItem.setRightBarButtonItem(nil, animated: true)
    }
    
    @IBAction func dismissKeyboard() {
        if let control = respondableControl {
            control.resignFirstResponder()
        }
    }
    
    func presentBirthdatePicker() {
        dismissKeyboard()
        performSegueWithIdentifier(QualcommSegue.ShowBirthdatePickerController, sender: self)
    }
    
    // MARK: - Data Hooks
    
    @IBAction func nameChanged() {
        if let newName = nameTextField.text {
            currentUser.name = newName
            
            currentUser.saveInBackgroundWithBlock{
                (succeeded: Bool, error: NSError?) -> Void in
                if succeeded {
                    print("saved name")
                } else {
                    print("unable to save name")
                }
            }
        } else {
            nameTextField.text = currentUser.name
            // @todo: prompt user that this field is required
        }
    }
    
    @IBAction func weightChanged() {
        if let newWeight = weightTextField.text {
            let weightInPounds = (newWeight as NSString).doubleValue
            currentUser.weight = UnitConverter.toKilograms(pounds: weightInPounds)
            
            currentUser.saveInBackgroundWithBlock{
                (succeeded: Bool, error: NSError?) -> Void in
                if succeeded {
                    print("saved weight")
                } else {
                    print("unable to save weight")
                }
            }
        } else {
            let weight = UnitConverter.toPounds(kilograms: currentUser.weight)
            weightTextField.text = String(weight)
            // @todo: prompt user that this field is required
        }
    }
    
    @IBAction func heightChanged() {
        if let newFeetHeight = feetTextField.text, let newInchesHeight = inchTextField.text {
            
            let feet = (newFeetHeight as NSString).doubleValue
            let inches = (newInchesHeight as NSString).doubleValue
            
            currentUser.height = UnitConverter.toCentimeters(feet: feet, inches: inches)
            
            currentUser.saveInBackgroundWithBlock{
                (succeeded: Bool, error: NSError?) -> Void in
                if succeeded {
                    print("saved height")
                } else {
                    print("unable to save height")
                }
            }
        } else {
            let height = UnitConverter.toImperial(centimeters: currentUser.height)
            
            feetTextField.text = String(height.feet)
            inchTextField.text = String(height.inches)
            // @todo: prompt user that this field is required
        }
    }
    
    func birthdatePicked(date: NSDate) {
        self.birthdate = date
        
        currentUser.birthdate = date
        currentUser.saveInBackgroundWithBlock {
            (succeeded: Bool, error: NSError?) in
            if succeeded {
                print("saved birthdate")
            } else {
                print("unable to save birthdate")
            }
        }
    }
    
    @IBAction func sexValueChanged(sender: AnyObject, forEvent event: UIEvent) {
        switch sexSegmentedControl.selectedSegmentIndex {
        case 0:
            currentUser.sex = "F"
        case 1:
            currentUser.sex = "M"
        case UISegmentedControlNoSegment:
            print("no segment was selected")
            return
        default:
            print("unknown segment was selected")
            return
        }
        currentUser.saveInBackgroundWithBlock {
            (succeeded: Bool, error: NSError?) in
            if succeeded {
                print("saved sex")
            } else {
                print("unable to save sex")
            }
        }
    }
    
    // MARK: - UITextFieldDelegate
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool {
        if (textField == birthdateTextField) {
            presentBirthdatePicker()
            return false
        }
        return true
    }

    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
        if segue.identifier == QualcommSegue.ShowBirthdatePickerController {
            let pickerViewController = segue.destinationViewController as! BirthdatePickerViewController
            pickerViewController.profileViewController = self
        }
    }

}
