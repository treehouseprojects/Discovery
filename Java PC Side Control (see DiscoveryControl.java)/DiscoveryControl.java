//for input controller stuff

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

//for serial comm stuff

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiscoveryControl{

	ControllerEnvironment ce;
	Controller[] ca;
	Controller controller;
	
	boolean missionModeOn; //if mission button pressed
	
	boolean readyToRead = false;
	OutputStream out;
	InputStream in;

	public DiscoveryControl() {
		
		//First, setup COM port
		try {
			connect("COM8"); //used to connect, and get the in/out streams for the COM port
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
		
		//Next, setup the controller stuff, choose betop controller
		ce = ControllerEnvironment.getDefaultEnvironment();
		ca = ce.getControllers();
		
		for(int i =0;i<ca.length;i++){
			if (ca[i].getName().equals("BETOP CONTROLLER"))	{
				controller = ca[i];
				
				Component[] components = ca[i].getComponents();
				System.out.println("Component Count: "+components.length);
				
				for(int j=0;j<components.length;j++){
					System.out.println("Component "+j+": "+components[j].getName());
					System.out.println("    Identifier: "+ components[j].getIdentifier().getName());
				}
			}
		}
		
		missionModeOn = false;
	}
	
	void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),5000);
			commPort.enableReceiveTimeout(1000);																//EXTREMELY important line for the bluetooth comm
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
				
				readyToRead = true;
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
	 
	public void pollEventData()	{
		controller.poll();
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		
		while(queue.getNextEvent(event)) {
            Component comp = event.getComponent();
            float value = event.getValue(); 
			String eventName = comp.getName();
               
			if(comp.isAnalog()) {
				
				if (eventName.equals("Z Axis") || eventName.equals("Z Rotation"))	{
					float valueToSend = Math.abs(value); //this will end up being the speed of movement
					
					if (eventName.equals("Z Axis"))	{
						if (value > 0)
							motorCommand('R', valueToSend, false);
						else if (value < 0)
							motorCommand('L', valueToSend, false);
					}		
					else if (eventName.equals("Z Rotation"))	{
						if (value < 0)
							motorCommand('F', valueToSend, false);
						else if (value > 0)
							motorCommand('B', valueToSend, false);
					}
				}
				
				else if (eventName.equals("X Axis") || eventName.equals("Y Axis"))	{
					if (eventName.equals("X Axis"))	{
						if (value > 0)
							cameraCommand('>', value);	//look right
						else if (value < 0)
							cameraCommand('<', value);	//look left
					}		
					else if (eventName.equals("Y Axis"))	{
						if (value < 0)
							cameraCommand('^', value);	//look up
						else if (value > 0)
							cameraCommand('v', value);	//look down
					}
				}
            } 
			else {
				//handle rotation commands
				if (eventName.equals("Button 4") || eventName.equals("Button 5"))	{
					if (value == 1.0f)	{
						if (eventName.equals("Button 4"))
							rotationCommand("ccw");
						else if (eventName.equals("Button 5"))
							rotationCommand("cw");
					}
					else
						rotationCommand("stop rotation");
				}
				
				//determine whether mission mode engaged
				if (eventName.equals("Button 8"))	{
					if(value==1.0f) {
						missionModeOn = true;
					} 
					else {
						missionModeOn = false;
					}
				}
				
				//if mission mode engaged, carry out designated action provided button is pressed (i.e. value = 1)
				if (missionModeOn && (value == 1.0f) && (eventName.equals("Button 0") || eventName.equals("Button 1") || eventName.equals("Button 2") || eventName.equals("Button 3") || eventName.equals("Button 9")))	{
					if (eventName.equals("Button 2"))
						missionCommand("move arm");
					else if (eventName.equals("Button 1"))
						missionCommand("actuate gripper");
					else if (eventName.equals("Button 0"))
						missionCommand("press button");
					else if (eventName.equals("Button 3"))
						missionCommand("lights");
					else if (eventName.equals("Button 9"))
						missionCommand("release sensor");
				}
            }
        }
         
        try {
            Thread.sleep(20);
        } 
		catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
	//send command to move robot
	public void motorCommand(char direction, float value, boolean turbo)	{
		try	{
			this.out.write(direction);
			this.out.write((int) (value * 127 + 127)); //this sequence to ensure delivered value is larger than 125 so that it does not conflict with the direction command
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
		
		System.out.println("Mot:" + direction + " - " + (int) (value * 127 + 127));
	}
	
	//send command to actuate mission mechanisms (servo actuation)
	public void missionCommand(String command)	{
		System.out.println(command);
		
		try	{
			this.out.write('S');
			
			if (command.equals("move arm"))
				this.out.write('m');
			else if (command.equals("actuate gripper"))
				this.out.write('g');
			else if (command.equals("press button"))
				this.out.write('p');
			else if (command.equals("lights"))
				this.out.write('l');
			else if (command.equals("release sensor"))
				this.out.write('r');
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
	}
	
	//send command to pan/tilt camera
	public void cameraCommand(char direction, float value)	{
		try	{
			this.out.write(direction);
			this.out.write((int) (Math.abs(value * 90) + 125)); //+35 to ensure delivered value is larger than 125 so that it does not conflict with the direction command
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
		
		System.out.println("Cam:" + direction + " - " + (int) (Math.abs(value * 90) + 125) + "value = " + value);
	}
	
	//send command to rotate robot on spot
	public void rotationCommand(String command)	{
		System.out.println(command);
		
		try {
			this.out.write('T');
			
			if (command.equals("cw"))
				this.out.write('c');
			else if (command.equals("ccw"))
				this.out.write('w');
			else if (command.equals("stop rotation"))
				this.out.write('s');
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DiscoveryControl discoveryController = new DiscoveryControl();
		
		if (discoveryController.readyToRead) {
			while (true) {
				discoveryController.pollEventData();
			}
		}
	}

}
