//
//  UnitConverter.swift
//  QSmartFashion
//
//  Created by Terrence Katzenbaer on 8/3/15.
//  Copyright © 2015 Qualcomm Incorporated. All rights reserved.
//

import UIKit

class UnitConverter {
    
    // MARK: - Length
    static func toImperial(centimeters centimeters: Double) -> (feet: Double, inches: Double) {
        let totalInches = centimeters / 2.54
        let feet = floor(totalInches / 12.0)
        let inches = totalInches - (feet * 12)
        return (feet: feet, inches: inches)
    }
    
    static func toCentimeters(feet feet: Double, inches: Double) -> Double {
        let totalInches = feet * 12 + inches
        return totalInches * 2.54
    }
    
    static func toMeters(centimeters centimeters: Double) -> Double {
        return centimeters / 100
    }
    
    static func toCentimeters(meters meters: Double) -> Double {
        return meters * 100
    }
    
    // MARK: - Weight
    static func toPounds(kilograms kilograms: Double) -> Double {
        return kilograms / 0.453592
    }
    
    static func toKilograms(pounds pounds: Double) -> Double {
        return pounds * 0.453592
    }
    
    // MARK: - Temperature
    static func toFahrenheit(celsius celsius: Double) -> Double {
        return celsius * 1.8 + 32
    }
    
    static func toCelsius(fahrenheit fahrenheit: Double) -> Double {
        return (fahrenheit - 32) / 1.8
    }
    
}
