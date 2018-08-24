package rgpioserverjbh;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import rgpio.*;

import java.io.BufferedReader;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import tcputils.WSServerListener;
import tcputils.WSServer;



class Event {
    int hour,min;
    boolean weekly;
    boolean active;
    boolean[] current=new boolean[7];
    boolean[] dagen = new boolean[7];
    int[] rang = new int [7];
    int value;

    public void Event(){
        System.out.println("New event created");
      
    }
    
    void updateRang(){
        System.out.println("Updating rang");
        for(int i=0; i<=6; i++){
            System.out.println("Bij controle update rang dag: "+i+" : "+this.dagen[i]);
            if (this.dagen[i]){
                this.rang[i]=i*1440+hour*60+min;
                System.out.println(rang[i]);
            }
        }
        System.out.println("Updating rang completed");
    }
    
    public void updateTijd(int newhour, int newmin){
        System.out.println("Upadting tijd");
        hour=newhour;
        min=newmin;
        this.updateRang();
        System.out.println("De tijd van het event is aangepast.");
        System.out.println("Upadting tijd completed");
    }
    
    public void updateDagen (String dag){
        if (dag.equals("Zondag")){
            dagen[0] = !dagen[0];
            System.out.println("Zondag has been inverted");
            System.out.println("Zondag is now " + dagen[0]);
        }else if(dag.equals("Maandag")){
                dagen[1] = !dagen[1];
                System.out.println("Maandag has been inverted");
                System.out.println("Maandag is now " + dagen[1]);   
        }
        else if(dag.equals("Dinsdag")){
                dagen[2] = !dagen[2];
                System.out.println("Dinsdag has been inverted");
                System.out.println("Dinsdag is now " + dagen[2]);   
        }
        else if(dag.equals("Woensdag")){
                dagen[3] = !dagen[3];
                System.out.println("Woensdag has been inverted");
                System.out.println("Woensdag is now " + dagen[3]);   
        }
        else if(dag.equals("Donderdag")){
                dagen[4] = !dagen[4];
                System.out.println("Donderdag has been inverted");
                System.out.println("Donderdag is now " + dagen[4]);   
        }
        else if(dag.equals("Vrijdag")){
                dagen[5] = !dagen[5];
                System.out.println("Vrijdag has been inverted");
                System.out.println("Vrijdag is now " + dagen[5]);   
        }
        else if(dag.equals("Zaterdag")){
                dagen[6] = !dagen[6];
                System.out.println("Zaterdag has been inverted");
                System.out.println("Zaterdag is now " + dagen[6]);   
        }
        this.updateRang();
    }
}

class Elements  extends Thread{
    public int tecontrolerenRang = -1;
    static public Event kleinsteEvent = new Event();
    int rangnow= -1;
    private String fileName= "";
    Calendar calnow;
    public List<Event> Events = new ArrayList<>();

    Elements(String fileName) {
        this.fileName= fileName;
        System.out.println(new Date()+" The filename is set for: "+this.fileName);
       // this.calnow = Calendar.getInstance();
    }
    void CreateJsonFile (){
        //try (FileWriter file = new FileWriter("d:\\test.json")) {
        try (FileWriter file = new FileWriter("/home/pi/RGPIO/"+fileName)) {
        for (Event event: Events){
        JSONObject obj= new JSONObject();
        JSONArray dagen= new JSONArray();
        obj.put("value",event.value);
        obj.put("hour",event.hour);
        obj.put("min",event.min);
        obj.put("weekly",event.weekly);
        for (int i=0; i<=6; i++){
           dagen.add(event.dagen[i]);  
        }
        obj.put("dagen", dagen);
        System.out.println(obj);
                //try (FileWriter file = new FileWriter("d:\\test.json")) {

                file.write(obj.toJSONString()+String.format("%n"));
                file.flush();
        }  
                } catch (IOException e) {
                e.printStackTrace();
        }
        
    }
    
    void ReadJsonFile (){
        
        JSONParser parser = new JSONParser();
        
        try
        {
           
            //FileReader infile = new FileReader("d:\\test.json");
            FileReader infile = new FileReader("/home/pi/RGPIO/"+fileName);            
            BufferedReader inStream = new BufferedReader (infile);
            String inString ;
            this.Events.clear();
            //System.out.println("inString= "+inString);
            while ((inString=inStream.readLine())!=null){
                
                Event event= new Event();
                Object object = parser.parse(inString);
                JSONObject jsonObject = (JSONObject)object;
                System.out.println("het jsonObenct is : "+jsonObject);
                long hour = (Long) jsonObject.get("hour");  
                long min = (Long) jsonObject.get("min");
                long value = (Long) jsonObject.get("value");
                //event.updateTijd((int)hour, (int)min);
                event.value=(int)value;
                boolean weekly = (Boolean) jsonObject.get("weekly");
                event.weekly=weekly;
                JSONArray dag1 = (JSONArray)jsonObject.get("dagen");
                System.out.println("hour read from file: "+hour);
                System.out.println("min read from file: "+min);
                System.out.println("dagen:");
                int teller=0;
                for (Object dag:dag1) {
                    event.dagen[teller] = "true".equals(dag.toString());
                    teller++;
                    System.out.println("\t"+dag.toString());
                    }
                event.updateTijd((int)hour, (int)min);
                System.out.println("weekly read from file: "+weekly);
                this.Events.add(event);
        }
            
            

            /*Reading the array
            
            
            //Printing all the values
            System.out.println("hour: " + hour);
            System.out.println("min: " + min);
            
            }*/
        }
        catch(FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    
    public void run (){
     try{
       while(true){
       Thread.sleep(10000);
       this.calnow = Calendar.getInstance();
       //calnow.getTime();
       System.out.print("day of week: "+(calnow.get(Calendar.DAY_OF_WEEK)-1) );
       System.out.print("  Hour: "+ calnow.get(Calendar.HOUR_OF_DAY));
       System.out.println("  Minute: "+ calnow.get(Calendar.MINUTE));
       tecontrolerenRang= 1440*(calnow.get(Calendar.DAY_OF_WEEK)-1)+60*calnow.get(Calendar.HOUR_OF_DAY)+calnow.get(Calendar.MINUTE)+1;
       if (tecontrolerenRang==-1){
       System.out.println(new Date()+ " Date and Time Run: Controleren Rang heeft een verkeerde waarde");
       }
       else{
           System.out.println(new Date() +"DateanTime run: De te controleren rang is: "+tecontrolerenRang);
           kleinsteEvent = this.vindkleinsteVerschil(tecontrolerenRang);
       }
       
       }
     }catch (InterruptedException ex) {
            }
    }
    
    
    
    public void addEvent(Event event){
    //create a new instance of event
    boolean match=false;
    Event eventnew = new Event();
    eventnew.min=event.min;
    eventnew.hour=event.hour;
    eventnew.active= event.active;
    eventnew.weekly=event.weekly;
    eventnew.value=event.value;
    for (int i=0; i<=6;i++){
      eventnew.current[i]=event.current[i]; 
      eventnew.dagen[i]=event.dagen[i];
      eventnew.rang[i]=event.rang[i];
    }
        // checking if a rang is already used. Double use of rangs is not allowed.
        for (int i=0; i<=6;i++){
        for (Event event1 : Events){
            if (event1.rang[i]==event.rang[i]) {
                //System.out.println(event1);
                //System.out.println("event1.rang["+i+"] = "+event1.rang[i]);
                //System.out.println("match");
                match= true;
            }
        
        }
    }
    
    if (match){
        System.out.println(new Date()+"addEvent event not added time conflict");
            }else{
        Events.add(eventnew);
        System.out.println(new Date()+"add Event event was added");
                }
    }
    void printAllRanges(){
                            
        for(Event event : Events){
            System.out.println("event= "+event);
            System.out.println("value ="+event.value);
            for (int i=0; i<=6; i++){
                System.out.println("rang("+i+") ="+event.rang[i]);
                
                }
                
            }
        }
        
    

    Event vindkleinsteVerschil(int rang){
        int kleinsteVerschilRang= 100000;
        int verschil, kleinsteVerschilDag=-1,restRang;
        Event kleinsteVerschil = new Event();
        Event isCurrentEvent = new Event();
        boolean erIsCurrentEvent = false;
        
        // Find the event that is closed to the current range back in time.        
        for(Event event : Events){
            for (int i=0; i<=6; i++){
                restRang=event.rang[i]-rang+10080;
                if (restRang>=10080)restRang=restRang-10080;
                verschil = 10080 - restRang; 
                if (verschil <= kleinsteVerschilRang&& verschil>0){
                kleinsteVerschilRang = verschil;
                kleinsteVerschil = event;
                kleinsteVerschilDag=i;
                if (event.current[i]){
                    erIsCurrentEvent = true;
                    isCurrentEvent=event; // Find the event that is current.
                    }
                }
                
            }
        }
        
        System.out.println("KleinsteVerschil Event is: "+ kleinsteVerschil);
        System.out.println("erIsCurrentEvent: "+erIsCurrentEvent);
        System.out.println("isCurrentEvent is: "+ isCurrentEvent);
        
        
        System.out.println("Het kleinste verschil is "+kleinsteVerschilRang);
        System.out.println(kleinsteVerschilDag);
        System.out.println("De value vanuit de timer thread is: "+ kleinsteVerschil.value);
        return kleinsteVerschil;
    }
}
class TempControle extends Thread{
    public void run (){
        System.out.println(new Date()+ " TempControle: Thread Started");
     try{
       while(true){
       System.out.println(new Date()+ " TempControle: Thread paused");
       Thread.sleep(9000);
       System.out.println(new Date() +" TempControle: Thread active");
       System.out.println(new Date()+" TempControle: The target Value is: "+ Elements.kleinsteEvent.value);
       BlauweZwaanRun.T1.get();
       System.out.println(new Date()+" TempControle: The measured value is: "+ BlauweZwaanRun.T1.avg());
       BlauweZwaanRun.mijnWSServer.sendToAll("Target Value = "+ Elements.kleinsteEvent.value);
       if (BlauweZwaanRun.T1.avg()==null){
           System.out.println(new Date()+" TempControle: Error No mesured value");
       } else {
           System.out.println(new Date()+" TempControle: Heating.value: "+ BlauweZwaanRun.Heating.value);
           //if (BlauweZwaanRun.Heating.value!=null){
            if (Elements.kleinsteEvent.value >= BlauweZwaanRun.T1.avg()){
              System.out.println(new Date()+" TempContole: Heating On");
              if (BlauweZwaanRun.Heating.value=="High"){
                    System.out.println(new Date()+" TempControle: Heating is already ON no instuction send");
                } else {            
                    BlauweZwaanRun.Heating.set("High");
                    System.out.println(new Date()+" TempControle: Heating was OFF is set ON");     
                }
           } else {
             System.out.println(new Date()+" TempContole: Heating OFF");    
              if (BlauweZwaanRun.Heating.value=="High"){
                System.out.println(new Date()+ " TempControle: Heating was ON is now OFF");
                BlauweZwaanRun.Heating.set("Low");
           } else{
              System.out.println(new Date()+" TempControle: Heating is already OFF no instuction send"); 
           }                
           } 
       //}
       }
     
       }
     }catch (InterruptedException ie) {
            }
    }
}


 class DateandTime {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Elements events=new Elements("calendar.json");
        events.ReadJsonFile ();
        events.printAllRanges();
        TempControle tempControle = new TempControle();
        tempControle.start();
        events.start();        
        Event event=new Event();
        
        
                
        Calendar cal = Calendar.getInstance();
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int hh = cal.get(Calendar.HOUR_OF_DAY);
        int mm = cal.get(Calendar.MINUTE);
        System.out.println("Hello World");
        System.out.println(dd);
        System.out.println(hh);
        System.out.println(mm);
        cal.set(Calendar.MINUTE, 56);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        dd = cal.get(Calendar.DAY_OF_MONTH);
        hh = cal.get(Calendar.HOUR_OF_DAY);
        mm = cal.get(Calendar.MINUTE);
        System.out.println("Hello World");
        System.out.println(dd);
        System.out.println(hh);
        System.out.println(mm);
        
       // events.ReadJsonFile ();
       events.CreateJsonFile();
        
        
        
        // TODO code application logic here
    }
    
}

class MijnClientHandler implements WSServerListener{
       
           public void MijnClientHandler () {
           }
           
         
             public ArrayList<String> onClientRequest(String clientID, String request) {
                ArrayList<String> reply = new ArrayList<>(); 
                 try{
                JSONParser parser = new JSONParser();
                Object object = parser.parse(request);
                JSONObject jsonObject = new JSONObject();
                jsonObject = (JSONObject)object;
                String command = (String) jsonObject.get("Command");
                System.out.println("MijnClientHandler: command: "+ command);
                System.out.println("MijnClientHandler: clientID= "+clientID);
                System.out.println("MijnClientHandler: request= "+request);
                if ("EVENTS".equals(command)){
                    System.out.println("The comand EVENTS is herkend");
                    for (Event ev : BlauweZwaanRun.events.Events){
                        
                            JSONObject obj= new JSONObject();
                            JSONArray dagen= new JSONArray();
                            obj.put("object", "EVENTS");
                            obj.put("value",ev.value);
                            obj.put("hour",ev.hour);
                            obj.put("min",ev.min);
                            obj.put("weekly",ev.weekly);
                            for (int i=0; i<=6; i++){
                                dagen.add(ev.dagen[i]);  
                            }
                            obj.put("dagen", dagen);
                            System.out.println(obj);
                            BlauweZwaanRun.mijnWSServer.sendToAll(obj.toJSONString());
                    }
                        
                    }
                    
                    
                }
                
                catch (Exception e) {
                    System.out.println(e);
                }
                return reply;
             }
       }



class BlauweZwaanRun  implements VInputListener, MessageListener {


    VDevice allDevices;
    static VAnalogInput H1,H2,H3,H4;
    static VAnalogInput T1,T2,T3,T4;
    static VDigitalOutput Heating;
    static WSServer mijnWSServer;
    static Elements events;
    public void onMessage(MessageEvent e) throws Exception{
      
    System.out.println(e.toString());

    }

       
    
    public void onInputEvent(VInput vinput) {

        if (vinput == T1){
            System.out.print("The temperatue is: ");
            System.out.println (T1.avg());
            if (T1.avg() >=28){
            
            } else {
            
        }
    }
    }

    
    public  void start() {
        RGPIO.addMessageListener(this);
        RGPIO.initialize();
        ClientHandler mijnClientHandler = new ClientHandler();
        RGPIO.webSocketServer.addListener(mijnClientHandler);
        events=new Elements("calendar.json");
        //Elements calendarHeatingKamerSeppe = new Elements();
        events.ReadJsonFile ();
        //calendarHeatingKamerSeppe.ReadJsonFile();
        events.printAllRanges();
        //calendarHeatingKamerSeppe.printAllRanges();
        TempControle tempControle = new TempControle();
        tempControle.start();
        events.start();   
        //  calendarHeatingKamerSeppe.start();
        Event event=new Event();

        //allDevices = RGPIO.VDevice("allDevices");

       T1 = RGPIO.VAnalogInput("T1");
       H1 = RGPIO.VAnalogInput("H1");
       Heating = RGPIO.VDigitalOutput("Heating");
       Heating.value="low";
     
       
       MijnClientHandler mijnclientHandler = new MijnClientHandler();       
       mijnWSServer = new WSServer(1302);
       mijnWSServer.addListener(mijnclientHandler);
       mijnWSServer.start();
      
       System.out.println("Main thread send to All started");
       int mijnteller= 0;
        while (true) {
            try {


                System.out.println("getting temp");
                T1.get();
                System.out.println(T1.avg());
                H1.get();
                System.out.println(H1.avg());
                //mijnWSServer.sendToAll("Dit is een vette test");
                Thread.sleep(10000);
                System.out.println("Main thread send to All started");
                mijnWSServer.sendToAll("Dit is een goede test"+ mijnteller);
                mijnteller++;
                System.out.println("Main thread send to All completed");
        
                
            } catch (InterruptedException ie) {
            }
        }
    } 
}


public class RGPIOServerJBH {

    public static void main(String[] args) {

        BlauweZwaanRun blauweZwaanRun =new BlauweZwaanRun();
        blauweZwaanRun.start();
    }
}
