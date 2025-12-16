package com.example.myspot.service;

import com.google.openlocationcode.OpenLocationCode;

public class PlusCodeService {
    
    /**
     * Converts latitude and longitude to Plus Code
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return Plus Code string
     */
    public String encode(double latitude, double longitude) {
        try {
            OpenLocationCode code = new OpenLocationCode(latitude, longitude);
            return code.getCode();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Decodes Plus Code to latitude and longitude
     * @param plusCode The Plus Code string
     * @return Array with [latitude, longitude] or null if invalid
     */
    public double[] decode(String plusCode) {
        try {
            OpenLocationCode code = new OpenLocationCode(plusCode);
            OpenLocationCode.CodeArea area = code.decode();
            return new double[]{
                area.getCenterLatitude(),
                area.getCenterLongitude()
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Checks if a Plus Code is valid
     * @param plusCode The Plus Code string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String plusCode) {
        try {
            OpenLocationCode code = new OpenLocationCode(plusCode);
            // Try to decode to verify validity
            code.decode();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

