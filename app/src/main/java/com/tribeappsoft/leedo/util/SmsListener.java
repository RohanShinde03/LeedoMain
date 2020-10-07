package com.tribeappsoft.leedo.util;

/*
 * Created by ${ROHAN} on 28/8/18.
 */
public interface SmsListener {

    void messageReceived(String sender, String messageText);

}
