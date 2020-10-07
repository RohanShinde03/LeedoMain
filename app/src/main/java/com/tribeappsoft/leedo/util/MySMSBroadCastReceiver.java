package com.tribeappsoft.leedo.util;

/**
 * Created by rohan on 28/8/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MySMSBroadCastReceiver extends BroadcastReceiver
{
    private SmsListener mSmsListener;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        Log.e("onReceive", " "+ intent.getExtras().toString());
        // Get Bundle object contained in the SMS intent passed in
        Bundle bundle = intent.getExtras();
        SmsMessage[] smsm = null;
        String sms_str ="";

        if (bundle != null)
        {
            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsm = new SmsMessage[pdus.length];
            for (int i=0; i<smsm.length; i++)
            {
                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                sms_str += "\r\nMessage: ";
                sms_str += smsm[i].getMessageBody();
                sms_str+= "\r\n";

                String mystring = smsm[i].getMessageBody();
                String arr[] = mystring.split(" ", 2);

                String otpval = arr[0];   //the
                //String theRest = arr[1];
                //String Sender = smsm[i].getOriginatingAddress();
                //Check here sender is yours
                Intent smsIntent = new Intent("otp");
                smsIntent.putExtra("message",otpval);

                LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);

            }
        }



        /*if (bundle != null)
        {
            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsm = new SmsMessage[pdus.length];
            for (int i=0; i<smsm.length; i++)
            {
                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                sms_str += "\r\nMessage: ";
                sms_str += smsm[i].getMessageBody().toString();
                sms_str+= "\r\n";

                String Sender = smsm[i].getOriginatingAddress();
                //Check here sender is yours
                Intent smsIntent = new Intent("otp");
                smsIntent.putExtra("message",sms_str);

                LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);

            }
        }*/

        /*if (bundle != null)
        {

            final Object[] pdusObj = (Object[]) bundle.get("pdus");

            for (int i = 0; i < pdusObj.length; i++)
            {

                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                String senderNum = phoneNumber;
                String message = currentMessage.getDisplayMessageBody().split(":")[1];

                message = message.substring(0, message.length()-1);
                Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                Intent myIntent = new Intent("otp");
                myIntent.putExtra("message",message);
                LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent);
                // Show Alert

            } // end for loop
        } // bundle is null*/





        /*if (bundle != null)
        {
            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                smsm = new SmsMessage[pdus.length];
            }
            assert smsm != null;
            for (int i = 0; i<smsm.length; i++)
            {
                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                sms_str += "\r\nMessage: ";
                sms_str += smsm[i].getMessageBody();
                sms_str+= "\r\n";


                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String sender = smsMessage.getDisplayOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();

                String mystring = smsm[i].getMessageBody();
                String arr[] = mystring.split(" ", 2);

                String otpval = arr[0];   //the
                //String theRest = arr[1];
                //String Sender = smsm[i].getOriginatingAddress();
                //Check here sender is yours
                Intent smsIntent = new Intent("otp");
                smsIntent.putExtra("message",otpval);

                Log.e("onReceive", "otpval "+otpval);
                LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);

                //Pass on the text to our listener.
                mSmsListener.messageReceived(sender, messageBody);

            }
        }*/
    }

    void bindListener(SmsListener listener){
        mSmsListener = listener;
    }

}
