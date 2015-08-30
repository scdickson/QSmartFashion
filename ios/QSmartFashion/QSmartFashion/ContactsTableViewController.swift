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

    @IBOutlet var addBarButtonItem: UIBarButtonItem!
    var contacts = [SFEmergencyContact]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.rightBarButtonItems = [addBarButtonItem, self.editButtonItem()]
        
        do {
            try self.contacts = ContactCacheManager.fetchContacts()
            print("loaded contacts from cache!")
        } catch ContactCacheFetchError.NotCached {
            if let query = SFEmergencyContact.query() {
                query.whereKey("user", equalTo: PFUser.currentUser()!)
                query.findObjectsInBackgroundWithBlock {
                    (objects: [AnyObject]?, error: NSError?) -> Void in
                    
                    if error == nil {
                        self.contacts = objects as! [SFEmergencyContact]
                        self.tableView.reloadData()
                        print("loaded contacts from server!")
                        
                        self.cacheContacts()
                    }
                }
            }
        } catch {
            print("unknown error occurred while fetching contacts")
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
            newContact.name = nameComponents.joinWithSeparator(" ")
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
        
        self.contacts.append(newContact)
        self.tableView.reloadData()
        newContact.saveInBackgroundWithBlock {
            (succeeded: Bool, error: NSError?) -> Void in
            if succeeded {
                print("contact successfully saved")
                
                self.cacheContacts()
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
        
        if let imageFile = contact.photo {
            if imageFile.isDataAvailable {
                if let imageData = imageFile.getData(), let image = UIImage(data: imageData) {
                    cell.profileImageView.image = image
                } else {
                    print("unable to put data into image")
                }
            } else {
                print("data is not available")
            }
        }

        return cell
    }

    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }

    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            let contact = contacts[indexPath.row]
            contacts.removeAtIndex(indexPath.row)
            contact.deleteInBackgroundWithBlock {
                (succeeded: Bool, error: NSError?) -> Void in
                if succeeded {
                    print("deleted '\(contact.name)' from my contacts!")
                    self.cacheContacts()
                } else {
                    print("unable to delete contact")
                }
            }
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        }
    }
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        
        let barButtonItems: [UIBarButtonItem]
        if editing {
            barButtonItems = [self.editButtonItem()]
        } else {
            barButtonItems = [addBarButtonItem, self.editButtonItem()]
        }
        self.navigationItem.setRightBarButtonItems(barButtonItems, animated: true)
    }
    
    private func cacheContacts() {
        do { // cache the contacts
            try ContactCacheManager.cacheContacts(self.contacts)
            print("cached contacts!")
        } catch (ContactCacheStoreError.UnableToCache) {
            print("unable to cache contacts")
        } catch (ContactCacheStoreError.UnableToCreateDirectory) {
            print("unable to create contacts cache directory")
        } catch {
            print("unknown error occurred while caching contacts")
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
