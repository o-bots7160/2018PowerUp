/*******************************************\
 ****@author LudingtonRobotics, Team 7160***
 ****Coders: Jordan Lake, David Scott******* 
\*******************************************/

package org.usfirst.frc.team7160.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	// Joysticks
	Joystick joy1 = new Joystick(0);
	Joystick joy2 = new Joystick(1);
	// Limit switches
	//DigitalInput grabberSwitch = new DigitalInput(9);
	DigitalInput liftUpSwitch = new DigitalInput(8);
	// DigitalInput liftDownSwitch = new DigitalInput(7);
	//
// Auton Selector
	DigitalInput leftRotary = new DigitalInput(1);
	DigitalInput centerRotary = new DigitalInput(2);
	DigitalInput rightRotary = new DigitalInput(3);
	//
// Creates objects for the motor controllers for driving
	WPI_TalonSRX frontLeft = new WPI_TalonSRX(2);
	WPI_TalonSRX frontRight = new WPI_TalonSRX(1);
	WPI_TalonSRX backLeft = new WPI_TalonSRX(4);
	WPI_TalonSRX backRight = new WPI_TalonSRX(3);
	//
	// This object handles the motor controllers making it easier to control them
	// all at once
	MecanumDrive mainDrive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);
	// Initial code for lift/grabber
	// Creates the object for the motor controllers that control the lift system
	WPI_TalonSRX lift1 = new WPI_TalonSRX(5);
	WPI_TalonSRX lift2 = new WPI_TalonSRX(6);
	Spark grabber1 = new Spark(0);
	Spark grabber2 = new Spark(1);
	Spark grabberAngleController = new Spark(2);
	//
	// The object that handles the gyroscope
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();
	//
	// Control groups for the lift system
	SpeedControllerGroup lift = new SpeedControllerGroup(lift1, lift2);
	SpeedControllerGroup grabber = new SpeedControllerGroup(grabber1, grabber2);
	//
// Timer object
		Timer autonTimer = new Timer();
		Timer timer = new Timer();
		// Auton info things
		// Object for getting info from the driver station/game
		DriverStation fms = DriverStation.getInstance();
		
// The string used to store the game data pertaining to the location of our
// switch
		String gameData;
		String position = null;
		String autoRun = null;
		double rot = 0;
		
		int step = 0;
		AutonType autonMode;

/**************** robotInit ****************/
		public void robotInit() {
			grabber1.setInverted(false);
			grabber2.setInverted(false);
			// Camera code //
			new Thread(() -> {
				UsbCamera camera = CameraServer.getInstance().startAutomaticCapture("cam1", 0);
				camera.setVideoMode(PixelFormat.kMJPEG, 320, 240, 15);
				// computer vision code not used
				/*
				 * CvSink cvSink = CameraServer.getInstance().getVideo(); CvSource outputStream
				 * = CameraServer.getInstance().putVideo("Blur", 640, 480);
				 * 
				 * Mat source = new Mat(); Mat output = new Mat();
				 * 
				 * while (!Thread.interrupted()) { cvSink.grabFrame(source);
				 * Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
				 * outputStream.putFrame(output); }
				 */
			}).start();

			mainDrive.setSafetyEnabled(false);
			SmartDashboard.putNumber("Gyro", gyro.getAngle());
			gyro.calibrate();
			read();
		}

		/**************** Auton Enumerator ****************/	
		enum AutonType { //This is used in selecting which Auton to run.
			posLeftswitchLeft, posRightswitchRight, posCenterswitchLeft, posCenterswitchRight, drvStraight
		}
		
	/**************** autonomousInit ****************/
		
		public void autonomousInit() {
			gameData = fms.getGameSpecificMessage(); //Pull game info from field or DriverSation
			autonTimer.reset();
			autonTimer.start();
			timer.reset();
			timer.start();
			gyro.reset();
			step = 0;
			read();  //Call function that displays info on SmartDashBoard
			if (gameData.charAt(0) == 'L' && (!leftRotary.get())) { //Checks if game switch is left and if robot Rotary is left
				autonMode = AutonType.posLeftswitchLeft;
				
		}	else if (gameData.charAt(0) == 'R' && (!rightRotary.get())) { //Checks if game switch is right and if robot Rotary is right
				autonMode = AutonType.posRightswitchRight;
				
		}	else if (gameData.charAt(0) == 'L' && (!centerRotary.get())) { //Checks if game switch is leftand if robot Rotary is center
				autonMode = AutonType.posCenterswitchLeft;
				
		}	else if (gameData.charAt(0) == 'R' && (!centerRotary.get())) { //Checks if game switch is right and if robot Rotary is center
				autonMode = AutonType.posCenterswitchRight;
				
		}	else { //Do this if everything else fails
				autonMode = AutonType.drvStraight;
			}
		}
		
	/**************** autonomousPeriodic ****************/	

		public void autonomousPeriodic() {
			read();  //Call function that displays info on SmartDashBoard
			switch(autonMode) {
			case posLeftswitchLeft:
				autoRun = "around boxes"; //Set what the following line displays on SmartDashBoard
				SmartDashboard.putString("Auto Mode: ", autoRun); //Put info about autorun on SmartDashBoard
				posLeftswitchLeft(); //Call function to go  to the left side of the switch 
				break;
			case posRightswitchRight:
				autoRun = "Right!"; //Set what the following line displays on SmartDashBoard
				SmartDashboard.putString("Auto Mode: ", autoRun); //Put info about autorun on SmartDashBoard
				posRightswitchRight(); //Call function to go  to the right side of the switch
				break;
			case posCenterswitchLeft:
				autoRun = "Left!"; //Set what the following line displays on SmartDashBoard
				SmartDashboard.putString("Auto Mode: ", autoRun); //Put info about autorun on SmartDashBoard
				posCenterswitchLeft(); //Call function to go to the front left side of the switch
				break;
			case posCenterswitchRight:
				autoRun = "Right!"; //Set what the following line displays on SmartDashBoard
				SmartDashboard.putString("Auto Mode: ", autoRun); //Put info about autorun on SmartDashBoard
				posCenterswitchRight(); //Call function to go to the front right side of the switch
				break;
			case drvStraight:
				autoRun = "a cruise"; //Set what the following line displays on SmartDashBoard
				SmartDashboard.putString("Auto Mode: ", autoRun); //Put info about autorun on SmartDashBoard
				drvStraight(); //Call function to go straight
				break;

			}
			
		}
		
/**************** autonomousFunctions ****************/		
		void posLeftswitchLeft() {
			switch(step) {
				case 0:
					if(autonTimer.get() <= .3) {
						grabberAngleController.set(.8);
					}else {
						grabberAngleController.set(0);
						autonTimer.reset();
						step = 1;
					}
					break;
				case 1:
					if(autonTimer.get() <= 4) {  //Do the following thing for this amount of time
						gyroStraight(); // Call function to keep robot pointing forward
						mainDrive.driveCartesian(0, -0.3, rot); //Y speed Right is positive, X speed Forward is positive, Z speed Clockwise is positive 
					} 
					else {
						mainDrive.driveCartesian(0, 0, 0, 0);
						autonTimer.reset();
						step = 2;
					}
					break;

				case 2:
					if(gyro.getAngle() <= 80) {
						mainDrive.driveCartesian(0, 0, 0.3);
						lift.set(0.55);
					} else {
						mainDrive.driveCartesian(0, 0, 0);
						autonTimer.reset();
						lift.set(0);
						step = 3;
					}
					break;
				case 3:
					if(autonTimer.get() < 1) {
						mainDrive.driveCartesian(0, -0.3, rot);
					} else {
						mainDrive.driveCartesian(0, 0, 0);
						autonTimer.reset();
						step = 4;
					}
					break;
				case 4:
					if(autonTimer.get() < 1.5) {
						grabber.set(1);
					} else {
						grabber.set(0);
						autonTimer.reset();
						step = 5;
					}
					break;
				case 5:
					if(autonTimer.get() < .8) {
						mainDrive.driveCartesian(0, .3, 0);
					}else {
						mainDrive.driveCartesian(0, 0, 0);
						autonTimer.reset();
						step = 6;
					}
					break;
			}
		}
		
		void posRightswitchRight() {
			switch(step) {
			case 0:
				if(autonTimer.get() <= .3) {
					grabberAngleController.set(.8);
				} else {
					grabberAngleController.set(0);
					autonTimer.reset();
					step = 1;
				}
				break;
			case 1:
				if(autonTimer.get() <= 4) {  //Do the following thing for this amount of time
					gyroStraight(); // Call function to keep robot pointing forward
					mainDrive.driveCartesian(0, -0.3, rot); //Y speed Right is positive, X speed Forward is positive, Z speed Clockwise is positive 
				} 
				else {
					mainDrive.driveCartesian(0, 0, 0, 0);
					autonTimer.reset();
					step = 2;
				}
				break;
			case 2:
				if(gyro.getAngle() >= -80) {
					mainDrive.driveCartesian(0, 0, -0.3);
					lift.set(0.55);
				} else {
					mainDrive.driveCartesian(0, 0, 0);
					autonTimer.reset();
					lift.set(0);
					step = 3;
				}
				break;
			case 3:
				if(autonTimer.get() < 1) {
					gyroStraight();
					mainDrive.driveCartesian(0, -0.3, rot);
				} else {
					mainDrive.driveCartesian(0, 0, 0);
					autonTimer.reset();
					step = 4;
				}
				break;
			case 4:
				if(autonTimer.get() < 1.5) {
					grabber.set(1);
				} else {
					grabber.set(0);
					autonTimer.reset();
					step = 5;
				}
				break;
			case 5:
				if(autonTimer.get() < .8) {
					mainDrive.driveCartesian(0, .3, 0);
				}else {
					mainDrive.driveCartesian(0, 0, 0);
					autonTimer.reset();
					step = 6;
				}
				break;			
			}
		}
		
		
		
		
		void posCenterswitchLeft() {
			switch(step) {
			case 0:
				if(autonTimer.get() <= .3) {
					grabberAngleController.set(.8);
				}else {
					grabberAngleController.set(0);
					autonTimer.reset();
					step = 1;
				}
				break;
			case 1:
				if(autonTimer.get() <= 1) {
					gyroStraight();
					mainDrive.driveCartesian(0, -0.3, rot);
				} else {
					mainDrive.driveCartesian(0, 0, 0);
					autonTimer.reset();
					step = 2;
				}
				break;
			case 2:
				if(autonTimer.get() <= 5) {
					gyroStraight();
					mainDrive.driveCartesian(-0.3, 0, rot); //TODO check sign
				} else {
					mainDrive.driveCartesian(0, 0, 0);
					autonTimer.reset();
					step = 3;
				}
				break;
			case 3:
				if(autonTimer.get() <= 2.5) {
					gyroStraight();
					mainDrive.driveCartesian(0, -0.3, rot);
					lift.set(0.55);
				} else {
					mainDrive.driveCartesian(0, 0, 0);
					lift.set(0);
					autonTimer.reset();
					step = 4;
				}
				break;
			case 4:
				if(autonTimer.get() < 2) {
					grabber.set(1);
				} else {
					grabber.set(0);
					step = 5;
				}
				break;
			}

		}
		
		void posCenterswitchRight() {
			switch(step) {
			case 0:
				if(autonTimer.get() <= .3) {
					grabberAngleController.set(.8);
				}else {
					grabberAngleController.set(0);
					autonTimer.reset();
					step = 1;
				}
				break;
			case 1:
				if(autonTimer.get() <= 3) {  //Do the following thing for this amount of time
					gyroStraight(); // Call function to keep robot pointing forward
					lift.set(0.55);
					mainDrive.driveCartesian(0, -0.3, rot); //Y speed Right is positive, X speed Forward is positive, Z speed Clockwise is positive 
				}  else {
					mainDrive.driveCartesian(0, 0, 0, 0);
					autonTimer.reset();
					lift.set(0);
					step = 2;
				}
				break;
			case 2:
				if(autonTimer.get() < 2) {
					grabber.set(1);
				} else {
					grabber.set(0);
					step = 3;
				}
			}
		}
		
		void drvStraight() {
			switch(step) {
			case 0:
				if(autonTimer.get() <= .3) {
					grabberAngleController.set(.8);
				}else {
					grabberAngleController.set(0);
					autonTimer.reset();
					step = 1;
				}
				break;
			case 1:
				if(autonTimer.get() <= 4) {  //Do the following thing for this amount of time
					gyroStraight(); // Call function to keep robot pointing forward
					mainDrive.driveCartesian(0, -0.3, rot); //Y speed Right is positive, X speed Forward is positive, Z speed Clockwise is positive 
				} 
				else {
					mainDrive.driveCartesian(0, 0, 0, 0);
					step = 2;
				}
				break;
			}	
		}

		void gyroStraight() {
			if(gyro.getAngle() >= 1) {
				rot = -.05; 
			}else if(gyro.getAngle() <= -1) {
				rot = .05;
							
			}else {
				rot = 0;
			}
			
		}
		
		


	public void teleopPeriodicInit() {
	}

	public void teleopPeriodic() {
		// Used to slow the acceleration of the lift so it doesn't move to hectectly
		// lift1.configOpenloopRamp(.2, 0);
		// lift2.configOpenloopRamp(.2, 0);
		//
		// Speed values for the angle controller on the grabber
		double grabberAngleDownSpeed = .5;
		double grabberAngleUpSpeed = 1;
		//
		// Drive code
		// Values to hold the values from the joysticks
		double x = joy1.getRawAxis(1);
		double y = joy1.getRawAxis(0);
		double rot1 = joy1.getRawAxis(2);
		//
		// Speed slower
		double speed = 2;
		//
		// Mecanum wheel controller
		if (joy1.getRawButton(1))
			speed = 1;
		else if(joy1.getRawButton(2))
			speed = 3;
		
		if (Math.abs(y) >= 0.3 || Math.abs(x) >= 0.3 || Math.abs(rot1) >= 0.4) {
			mainDrive.driveCartesian(y / speed, x / speed, rot1 / speed);
		} else {
			mainDrive.driveCartesian(0, 0, 0);
		}
		//

		// The code used for for the lift System
		double liftUpSpeed = .65;
		double liftDownSpeed = -.3;
		if (liftUpSwitch.get())
			liftUpSpeed = 0;

		if (joy2.getRawButton(2)) {
			lift.set(liftUpSpeed);

		} else if (joy2.getRawButton(1)) {
			lift.set(liftDownSpeed);
		} else {
			lift.set(0);
		}
		//

		// The code used for our grabber
		int grabberIn = -1;
		//if (grabberSwitch.get())
		//	grabberIn = 0;
		if (joy2.getRawButton(5)) {
			grabber1.setInverted(false);
			grabber2.setInverted(false);
			grabber.set(1);
		} else if (joy2.getRawButton(6)) {
			grabber1.setInverted(false);
			grabber2.setInverted(false);
			grabber.set(-1);
		} else if(Math.abs(joy2.getRawAxis(2)) >= 0.3) {
			grabber1.setInverted(true);
			grabber2.setInverted(false);
			grabber.set(-0.8);
		} else if(Math.abs(joy2.getRawAxis(3)) >= 0.3) {
			grabber1.setInverted(false);
			grabber2.setInverted(true);
			grabber.set(-0.8);
		} else {
			grabber1.setInverted(false);
			grabber2.setInverted(false);
			grabber.set(0);
		}

		if (joy2.getRawButton(3)) {
			grabberAngleController.set(grabberAngleDownSpeed);
		} else if (joy2.getRawButton(4)) {
			grabberAngleController.set(-grabberAngleUpSpeed);
		} else {
			grabberAngleController.set(0);
		}
	}

	void read() {
		if (!leftRotary.get()) {
			position = "Left";
		} else if (!rightRotary.get()) {
			position = "Right";
		} else if (!centerRotary.get()) {
			position = "Middle";
		} else {
			position = "NONE!";
		}
		//SmartDashboard.putNumber("Rotation", rot);
		SmartDashboard.putNumber("Gyro", Math.round(gyro.getAngle()));
		SmartDashboard.putString("Auton position", position);

	}
	public void disabledPeriodic() {
		read();
	}
	
	@Override
	public void testInit() {
	}

	@Override
	public void testPeriodic() {

	}
}