
import java.io.IOException;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;
import java.util.Vector;

public class SenderReceiver extends MIDlet implements CommandListener, MessageListener{
    private List optionList;        // list of options for the user
    
    private Display display;        // canvas for our app
   
    private Form smsForm;           // form which will allow the user to input data
    private Form recForm;           // form that will contain the received data
    
    private TextField phoneNrField; // textboxes
    private TextField smsField;
    private TextBox recMessage;
    
    private Command exitCmd;        // commands inside the app
    private Command openCmd;
    private Command sendCmd;
    private Command backCmd;
    
    private String smsString;
    private String phoneNrString;
    
    private int getSelInd;
    
    MessageConnection receiveCon;
    
    public void startApp(){
        display = Display.getDisplay(this);
        
        // the List object allows the user to select either New message or the Inbox
        optionList = new List("SMS Service", List.IMPLICIT);
        
        // add the options to the list using the append() method
        optionList.append("New message", null);
        
        // define the user interface of our commands
        exitCmd = new Command("Exit", "Exit from the app", Command.EXIT, 7);
        openCmd = new Command("Open", Command.OK, 4);
        optionList.addCommand(exitCmd);
        optionList.addCommand(openCmd);
        
        // listen for commands
        optionList.setCommandListener(this);
        
        // display the form on the screen
        display.setCurrent(optionList);
        
        /*  ------------- create the New Message user interface  -------------------- */
       
        // allocate memory for the form
        smsForm = new Form("Send SMS");
        
        // create the textboxes for the phone number and sms
        phoneNrField = new TextField("To: ", "", 20, TextField.PHONENUMBER);
        smsField = new TextField("Message : ", "", 150, TextField.ANY);
               
        // add them to our form
        smsForm.append(phoneNrField);
        smsForm.append(smsField);
        
        // create the send and back commands
        sendCmd = new Command("Send", Command.OK, 4);
        backCmd = new Command("Back", Command.BACK, 2);
        smsForm.addCommand(sendCmd);
        smsForm.addCommand(backCmd);
        smsForm.addCommand(exitCmd);
        smsForm.setCommandListener(this);
        
        
        /* ------------------- Receive message ---------------------  */
        
        try {
            // "sms://:5000" is the Connection String that we have to give in the Push Registry
            receiveCon = (MessageConnection) Connector.open("sms://:5000" );
                    
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            receiveCon.setMessageListener(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void destroyApp(boolean force){
        try{
            receiveCon.setMessageListener(null);
            receiveCon.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void pauseApp(){
    }
    
    // define functionality of our commands
    public void commandAction(Command c, Displayable d) {
        if(c == exitCmd){                                   // exit to Main Menu
            notifyDestroyed();
            
        }else if(c == openCmd){                             // open command
            getSelInd = optionList.getSelectedIndex();      // get the index of the selected option

            if(getSelInd == 0){                             // opens the New message interface
                System.out.println("New message");          
                display.setCurrent(smsForm);
            }
            
        }else if(c == sendCmd){                             // send command
            phoneNrString = phoneNrField.getString();
            smsString = smsField.getString();
            
            // this thread will send the message
            new SmsSenderThread(phoneNrString, smsString, optionList, display).start();
            
        }else if(c == backCmd){                             // back command
            // return to the options screen
            display.setCurrent(optionList);
        }
    }
    
    // Method used by the MessageListener
    public void notifyIncomingMessage(MessageConnection mc) {
        new SmsReceiverThread().start();
    }
    
    // Inner class thread for receiving the message
    private class SmsReceiverThread extends Thread implements CommandListener {
        
        public void commandAction(Command c, Displayable d) {
        if(c == backCmd){                                   // back to optionList
                display.setCurrent(optionList);
            }
        }
        public void run() {
            try {
                
                // use a TextMessage object to send a message containing a java.lang.String.
                // It inherits from the Message interface and adds a text message body.
                TextMessage textMsg = (TextMessage) receiveCon.receive();

                // Get the receiving SMS phone number
                String senderNr = textMsg.getAddress();

                // Get the receiving SMS message
                String senderMsg = textMsg.getPayloadText();
                
                // Decode the received SMS
                String decodedSMS = decode(senderMsg);

                // Create a TextBox and direct the incomming message to it
                TextBox textBox = new TextBox(senderNr, decodedSMS, 160, TextField.ANY);

                System.out.println("Message received");
                System.out.println("Encoded: " + senderMsg);
                System.out.println("Decoded: " + decodedSMS);
                
                textBox.addCommand(backCmd);
                textBox.setCommandListener(this);
                display.setCurrent(textBox);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        // The method used for decoding the received SMS
        private String decode(String text){
            
            String decodedTxt = "";

            char[] alphaNum = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '=', ' ','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '{','}','|','[',']',':',';','"', '<' ,'>',',','.','/','?'};
       
            final int FW_KEY = 3;
            int backKey = alphaNum.length - FW_KEY;
        
            int indexArraySize = 0;
        
            char[] copyTxt = text.toCharArray();
        
            // count how many uppercase letter there are
            for(int i = 0; i < copyTxt.length; i++) 
                if(Character.isUpperCase(copyTxt[i]))
                    indexArraySize++;
        
            // create an array based on the nr. of uppercase letters   
            int[] letterIndex = new int[indexArraySize++];   
        
            int upperLetter = 0, txtLetter = 0;
        
            // store the indexes of the uppercase letter into letterIndex[] and convert those letter to lowercase for decoding
            while(upperLetter < letterIndex.length){
                if(Character.isUpperCase(copyTxt[txtLetter])){
                    copyTxt[txtLetter] = Character.toLowerCase(copyTxt[txtLetter]);
                    letterIndex[upperLetter] = txtLetter;
                    upperLetter++;
                }
                txtLetter++;
            }
            
            // decoding of the sms             
            for(int i = 0; i < copyTxt.length; i++){
                for(int j = 0; j < alphaNum.length; j++){
                    if(copyTxt[i] == alphaNum[j])
                        if(j >= FW_KEY){
                            copyTxt[i] = alphaNum[j - FW_KEY];
                            break;
                            
                        }else{
                            copyTxt[i] = alphaNum[j + backKey];
                            break;
                        }
                }
            }
            // reconvert to uppercase 
            for (int i = 0; i < letterIndex.length; i++){
                copyTxt[letterIndex[i]] = Character.toUpperCase(copyTxt[letterIndex[i]]); 
                }
            // create the decoded message from the char array we used for decoding
            for(int i = 0; i < copyTxt.length; i++)
                decodedTxt = decodedTxt + copyTxt[i];
        
            return decodedTxt;
        }
    }
}
