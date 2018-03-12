/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stroe
 */
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.List;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;


public class SmsSenderThread extends Thread {

    private String thisPhnNo;
    private String thisMsg;
    private Display thisDisplay;
    private List thisList;
    private Alert infoAlert;

    // Get the values from the Messaging midlet class
    public SmsSenderThread(String passPhnNo, String passMsg, List list, Display display) {
        this.thisPhnNo = passPhnNo;
        this.thisMsg = passMsg;
        this.thisList = list;
        this.thisDisplay = display;
    }
    
    public void run() {
        System.out.println("Running... ");
        System.out.println(thisPhnNo);
        System.out.println(thisMsg);

        // Creating the connection
        MessageConnection msgCon = null;
        
        try {
            // Open the connection with a port
            msgCon = (MessageConnection) Connector.open("sms://" + thisPhnNo + ":5000");
            System.out.println("Created connection...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Create platform to put a new text message
        TextMessage txtMsg = (TextMessage) msgCon.newMessage(MessageConnection.TEXT_MESSAGE);
        
        // Encode the SMS message
        String encodedSMS = encode(thisMsg);
        System.out.println(encodedSMS);

        // Set the aircraft (here the SMS) for which payment is received
        txtMsg.setPayloadText(encodedSMS);

        try {
            // Send the SMS
            msgCon.send(txtMsg);
            System.out.println("Success...");
            //the alert will let the user know that their message was sent successfully
            infoAlert = new Alert("Message sent!");
            thisDisplay.setCurrent(infoAlert, thisList);
            
        } catch (IOException ex) {
            ex.printStackTrace();
            
        } finally{
            // If the connection is open...
            if(msgCon != null){
                try {
                    // Close the connection
                    msgCon.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private String encode(String text){
        String encodedTxt = "";
        
        char[] alphaNum = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '=', ' ','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '{','}','|','[',']',':',';','"', '<' ,'>',',','.','/','?'};
       
        int indexArraySize = 0;
        
        final int FW_KEY = 3;
        int backKey = alphaNum.length - FW_KEY;
     
        char[] copyTxt = text.toCharArray();
        
        for(int i = 0; i < copyTxt.length; i++) 
            if(Character.isUpperCase(copyTxt[i]))
                indexArraySize++;
           
        int[] letterIndex = new int[indexArraySize++];   
        
        int upperLetter = 0, txtLetter = 0;
        
        while(upperLetter < letterIndex.length){
            if(Character.isUpperCase(copyTxt[txtLetter])){
                copyTxt[txtLetter] = Character.toLowerCase(copyTxt[txtLetter]);
                letterIndex[upperLetter] = txtLetter;
                upperLetter++;
            }
            txtLetter++;
        }
        for(int i = 0; i < copyTxt.length; i++){
            for(int j = 0; j < alphaNum.length; j++){
                if(copyTxt[i] == alphaNum[j])
                    
                        if(j < alphaNum.length - FW_KEY){
                            copyTxt[i] = alphaNum[j + FW_KEY];
                            break;
                            
                        }else{
                            copyTxt[i] = alphaNum[j - backKey];
                            break;
                        }
                }
        }
        for (int i = 0; i < letterIndex.length; i++){
            copyTxt[letterIndex[i]] = Character.toUpperCase(copyTxt[letterIndex[i]]); 
            }
        
    for(int i = 0; i < copyTxt.length; i++)
        encodedTxt = encodedTxt + copyTxt[i];
    return encodedTxt;
    }
}