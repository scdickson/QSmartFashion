//
//  BirthdatePickerViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/3/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

class BirthdatePickerViewController: UIViewController {

    weak var profileViewController: ProfileViewController!
    @IBOutlet var birthdateTextField: UITextField!
    @IBOutlet var datePicker: UIDatePicker!

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let date = profileViewController.birthdate {
            birthdateTextField.text = profileViewController.birthdateTextField.text
            datePicker.setDate(date, animated: false)
        }
    }
    
    @IBAction func selectToday(sender: AnyObject) {
        datePicker.setDate(NSDate(), animated: true)
        
        datePicked(datePicker)
    }
    
    @IBAction func datePicked(sender: UIDatePicker) {
        profileViewController.birthdatePicked(sender.date)
        
        birthdateTextField.text = profileViewController.birthdateTextField.text
    }

}
