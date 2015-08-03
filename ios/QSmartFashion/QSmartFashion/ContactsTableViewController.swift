//
//  ContactsTableViewController.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/2/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import ContactsUI
import Parse

class ContactsTableViewController: UITableViewController, CNContactPickerDelegate {

    var contacts = [SFEmergencyContact]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        print("loading contacts...")
        if let query = SFEmergencyContact.query() {
            query.whereKey("user", equalTo: PFUser.currentUser()!)
            query.findObjectsInBackgroundWithBlock {
                (objects: [AnyObject]?, error: NSError?) -> Void in
                
                if error == nil {
                    self.contacts = objects as! [SFEmergencyContact]
                    self.tableView.reloadData()
                    print("loaded contacts!")
                }
            }
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print("returning \(self.contacts.count) rows")
        return self.contacts.count
    }
    
//    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
//        return 55
//    }

    @IBAction func showPicker(sender: UIBarButtonItem) {
        let picker = CNContactPickerViewController()
        picker.delegate = self
        
        self.presentViewController(picker, animated: true, completion: nil)
    }
    
    func contactPickerDidCancel(picker: CNContactPickerViewController) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func contactPicker(picker: CNContactPickerViewController, didSelectContact contact: CNContact) {
        print("picked \(contact)")
        self.dismissViewControllerAnimated(true, completion: nil)
        
        var nameComponents = [String]()
        if contact.givenName.lengthOfBytesUsingEncoding(NSUTF8StringEncoding) > 0 {
            nameComponents += [contact.givenName]
        }
        if contact.middleName.lengthOfBytesUsingEncoding(NSUTF8StringEncoding) > 0 {
            nameComponents += [contact.middleName]
        }
        if contact.familyName.lengthOfBytesUsingEncoding(NSUTF8StringEncoding) > 0 {
            nameComponents += [contact.familyName]
        }

        let newContact = SFEmergencyContact(user: PFUser.currentUser()!)
        
        if nameComponents.count > 0 {
            newContact.name = " ".join(nameComponents)
        } else {
            newContact.name = "No Name"
            // @todo alert user that the contact has no name
            print("no name")
        }
        
        if let primaryPhoneNumber = contact.phoneNumbers.first?.value as? CNPhoneNumber {
            newContact.phoneNumber = primaryPhoneNumber.stringValue
        } else {
            // @todo alert user that the contact has no phone number
            print("no phone number")
        }
        
        if let imageData = contact.thumbnailImageData {
            newContact.photo = PFFile(name: "file.png", data: imageData)
        } else {
            // @todo alert user that the contact has no profile picture
            print("no profile picture")
        }
        
        newContact.saveInBackgroundWithBlock {
            (succeeded: Bool, error: NSError?) -> Void in
            if succeeded {
                print("contact successfully saved")
            } else {
                print("contact was not saved")
            }
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(TableViewCellIdentifier.Contact, forIndexPath: indexPath) as! ContactTableViewCell

        let contact = self.contacts[indexPath.row]
        cell.nameLabel.text = contact.name
        cell.phoneNumberLabel.text = contact.phoneNumber

        return cell
    }

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
