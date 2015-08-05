package com.qualcomm.qsmartfashion.utils;

import com.qualcomm.qsmartfashion.Constants;

/**
 * Created by sdickson on 7/26/15.
 */
public class UnitConverter
{
    public static double poundsToKilograms(int pounds)
    {
        return pounds * Constants.KILOGRAMS_PER_POUND;
    }

    public static double cToF(double tempC)
    {
        return tempC * (9.0/5.0) + 32.0;
    }

    public static double fToC(double tempF)
    {
        return (tempF-32.0)*(5.0/9.0);
    }

    public static int kilogramsToPounds(double kilograms)
    {
        return (int) (kilograms / Constants.KILOGRAMS_PER_POUND);
    }

    public static double feetInchesToCentimeters(int feetInches[])
    {
        int feet = feetInches[0];
        int inches = feetInches[1];

        return ((feet * Constants.INCHES_PER_FOOT) + inches) * Constants.CENTIMETERS_PER_INCH;
    }

    public static int[] centimetersToFeetInches(double centimeters)
    {
        int feetInches[] = new int[2];
        double inches = centimeters / Constants.CENTIMETERS_PER_INCH;
        double feet = inches / Constants.INCHES_PER_FOOT;
        feetInches[0] = (int) feet;
        feetInches[1] = (int) ((feet - feetInches[0]) * Constants.INCHES_PER_FOOT);
        return feetInches;
    }
}
