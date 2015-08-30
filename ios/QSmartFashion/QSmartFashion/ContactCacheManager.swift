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
    
    private class var cacheURL: NSURL {
        let paths = NSSearchPathForDirectoriesInDomains(.CachesDirectory, .UserDomainMask, true)
        
        guard let directoryPath = paths.first else {
            fatalError("there is no caches directory")
        }
        let directoryURL = NSURL(fileURLWithPath: directoryPath, isDirectory: true)
        
        return directoryURL.URLByAppendingPathComponent("contacts")
    }
    private class var contactsCacheURL: NSURL {
        return cacheURL.URLByAppendingPathComponent("contacts.plist")
    }
    private class func photoCacheURL(contactId: String) -> NSURL {
        return cacheURL.URLByAppendingPathComponent("\(contactId).png")
    }
    
    /// Throws: ContactCacheFetchError
    class func fetchContacts() throws -> [SFEmergencyContact] {
        let fileManager = NSFileManager.defaultManager()
        
        guard fileManager.fileExistsAtPath(contactsCacheURL.path!) else {
            throw ContactCacheFetchError.NotCached
        }
        
        let contacts = NSKeyedUnarchiver.unarchiveObjectWithFile(contactsCacheURL.path!) as! [SFEmergencyContact]

        for contact in contacts {
            loadPicture(forContact: contact)
        }
        
        return contacts
    }
    
    class func cacheContacts(contacts: [SFEmergencyContact]) throws {
        let fileManager = NSFileManager.defaultManager()
        if !fileManager.fileExistsAtPath(cacheURL.path!) {
            do {
                try fileManager.createDirectoryAtPath(cacheURL.path!,
                    withIntermediateDirectories: true,
                    attributes: nil
                )
            } catch {
                throw ContactCacheStoreError.UnableToCreateDirectory
            }
        }
        
        if !NSKeyedArchiver.archiveRootObject(contacts, toFile: contactsCacheURL.path!) {
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
        
        guard let photoData = NSData(contentsOfURL: photoCacheURL(contactId)) else {
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
        photoData.writeToURL(photoCacheURL(contactId), atomically: true)
    }
}
