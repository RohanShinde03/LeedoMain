package com.tribeappsoft.leedo.util;

import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Created by rohan on 26/4/17.
 */

public class Validation
{
    // Regular Expression
    // you can change the expression based on your need
    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PHONE_REGEX =  "^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}$";
    //private static final String PAN_REGEX =  "^[a-z]{5}[0-9]{4}[a-z]{1}$";
    private static final String PAN_REGEX =  "[A-Z]{5}[0-9]{4}[A-Z]{1}";
    private static final String GSTINFORMAT_REGEX = "[0-9]{2}[a-zA-Z]{5}[0-9]{4}[a-zA-Z]{1}[1-9A-Za-z]{1}[Z]{1}[0-9a-zA-Z]{1}";
    private static final String IFSC_CODE_REGEX = "^[A-Za-z]{4}0[A-Z0-9a-z]{6}$";   //it is first 4 characters as digit and remaining 7 characters as alphanumeric
    private static final String PIN_CODE_REGEX = "^[1-9][0-9]{5}$";
    //private static final String PHONE_REGEX = "^[789\\+]\\d{9}$";         //for without country code

    // Error Messages
    private static final String REQUIRED_MSG = "required";
    private static final String EMAIL_MSG = "Invalid Email!";
    private static final String PHONE_MSG = "Invalid Phone Number!";
    private static final String BATCH_YEAR_MSG = "Invalid Year!";
    private static final String PAN_MSG = "Invalid PAN Number!";
    private static final String GST_MSG = "Invalid GSTIN Number!";
    private static final String IFSC_MSG = "Invalid IFSC Code!";
    private static final String PINCODE_MSG = "Invalid Pin Code!";


    // call this method when you need to check email validation
    public static boolean isEmailAddress(EditText editText, boolean required)
    {
        return isValid(editText, EMAIL_REGEX, EMAIL_MSG, required);
    }

    // call this method when you need to check phone number validation
    public static boolean isPhoneNumber(EditText editText, boolean required)
    {
        return isValid(editText, PHONE_REGEX, PHONE_MSG, required);
    }

    // call this method when you need to check phone number validation
    public static boolean isValidBatchYear(EditText editText, boolean required)
    {
        return isValidBatch(editText, BATCH_YEAR_MSG, required);
    }

    //call this method when you need to check valid PAN number
    public static boolean isPANNumber(EditText editText, boolean required)
    {
        return isValid(editText, PAN_REGEX, PAN_MSG, required);
    }

    //call this method when you need to check valid PAN number
    public static boolean isGSTINNumber(EditText editText, boolean required)
    {
        return isValid(editText, GSTINFORMAT_REGEX, GST_MSG, required);
    }

    //call this method when you need to check valid PAN number
    public static boolean isIFSCCode(EditText editText, boolean required)
    {
        return isValid(editText, IFSC_CODE_REGEX, IFSC_MSG, required);
    }

    //call this method when you need to check valid Pin code number
    public static boolean isPinCode(EditText editText, boolean required)
    {
        return isValid(editText, PIN_CODE_REGEX, PINCODE_MSG, required);
    }

    // return true if the input field is valid, based on the parameter passed
    private static boolean isValid(EditText editText, String regex, String errMsg, boolean required)
    {
        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        if ( required && !hasText(editText) ) return false;

        // pattern doesn't match so returning false
        if (required && !Pattern.matches(regex, text))
        {

            //editText.setError(errMsg);
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
            return false;
        }
        return true;
    }

    // return true if the input field is valid, based on the parameter passed
    private static boolean isValidBatch(EditText editText, String errMsg, boolean required)
    {
        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        if ( required && !hasText(editText) ) return false;

        //check length
        if ( required && editText.getText().toString().trim().length() < 4 ) return false;

        // pattern doesn't match so returning false
        if (required && Integer.parseInt(editText.getText().toString()) > 2019 )
        {

            //editText.setError(errMsg);
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
            return false;
        }
        return true;
    }

    public static boolean validSecondaryMobileNumber(EditText editText, boolean required)
    {
        return ValidMobile(editText, PHONE_REGEX, PHONE_MSG, required);
    }

    // return true if the input field is valid, based on the parameter passed
    private static boolean ValidMobile(EditText editText, String regex, String errMsg, boolean required)
    {
        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        //if ( required && !hasText(editText) ) return false;

        // pattern doesn't match so returning false
        if (required && !Pattern.matches(regex, text))
        {
            editText.setError(errMsg);
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
            return false;
        }
        return true;
    }


    // check the input field has any text or not
    // return true if it contains text otherwise false
    private static boolean hasText(EditText editText)
    {
        String text = editText.getText().toString().trim();
        editText.setError(null);

        // length 0 means there is no text
        if (text.length() == 0)
        {
            //editText.setError(REQUIRED_MSG);
            //editText.requestFocus();
            editText.setSelection(editText.getText().length());
            return false;
        }
        return true;
    }

}