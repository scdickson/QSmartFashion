//
//  ContactCacheManager.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/2/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit
import Parse

enum ContactCacheFetchError: ErrorType {
    case NotCached
}

enum ContactCacheStoreError: ErrorType {
    case UnableToCache
    case UnableToCreateDirectory
}

class ContactCacheManager {
    
    private class var cachePath: String {
        let paths = NSSearchPathForDirectoriesInDomains(.CachesDirectory, .UserDomainMask, true)
        
        guard let path = paths.first else {
            fatalError("there is no caches directory")
        }
        
        return path.stringByAppendingPathComponent("contacts")
    }
    private class var contactsPath: String {
        return cachePath.stringByAppendingPathComponent("contacts.plist")
    }
    
    /// Throws: ContactCacheFetchError
    class func fetchContacts() throws -> [SFEmergencyContact] {
        let fileManager = NSFileManager.defaultManager()
        
        guard fileManager.fileExistsAtPath(contactsPath) else {
            throw ContactCacheFetchError.NotCached
        }
        
        let contacts = NSKeyedUnarchiver.unarchiveObjectWithFile(contactsPath) as! [SFEmergencyContact]

        for contact in contacts {
            loadPicture(forContact: contact)
        }
        
        return contacts
    }
    
    class func cacheContacts(contacts: [SFEmergencyContact]) throws {
        let fileManager = NSFileManager.defaultManager()
        if !fileManager.fileExistsAtPath(cachePath) {
            do {
                try fileManager.createDirectoryAtPath(cachePath,
                    withIntermediateDirectories: true,
                    attributes: nil
                )
            } catch {
                throw ContactCacheStoreError.UnableToCreateDirectory
            }
        }
        
        if !NSKeyedArchiver.archiveRootObject(contacts, toFile: contactsPath) {
            throw ContactCacheStoreError.UnableToCache
        }
        
        for contact in contacts {
            savePicture(forContact: contact)
        }
    }
    
    private class func loadPicture(forContact contact: SFEmergencyContact) {
        guard let contactId = contact.objectId else {
            print("\(contact.name) does not have a contact id?")
            return
        }
        let photoPath = cachePath.stringByAppendingPathComponent("\(contactId).png")
        guard let photoData = NSData(contentsOfFile: photoPath) else {
            print("\(contact.name) does not have a cached photo")
            return
        }
        contact.photo = PFFile(data: photoData)
    }
    
    private class func savePicture(forContact contact: SFEmergencyContact) {
        guard let contactId = contact.objectId else {
            print("\(contact.name) does not have a contact id?")
            return
        }
        guard let photoFile = contact.photo else {
            print("\(contact.name) does not have a photo to save")
            return
        }
        guard photoFile.isDataAvailable else {
            print("photo data is unavailable for \(contact.name)")
            // @todo: maybe download in background and cache when available?
            return
        }
        guard let photoData = photoFile.getData() else {
            print("unable to get data from photo??")
            return
        }
        let photoPath = cachePath.stringByAppendingPathComponent("\(contactId).png")
        photoData.writeToFile(photoPath, atomically: true)
    }
}
