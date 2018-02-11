package org.usfirst.frc.team4145.robot.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4145.robot.Robot;
import org.usfirst.frc.team4145.robot.RobotMap;
import org.usfirst.team4145.robot.shared.CustomPIDSubsystem;

/**
 * Robot drive version 2
 * this version of the drive system is built based on 0 - 360 from the ground up
 * if you want the current gyro angle call getGyro()
 */

public class RobotDriveV2 extends CustomPIDSubsystem {

    //used internally for data
    private PIDController gyroLock;
    private double pidOutput = 0; //DO NOT MODIFY
    private boolean enLock = false;
    private boolean isReversed = false;
    private double[] lastInputSet = {0, 0, 0}; //last input set from joystick update

    //general use variables
    private boolean brakeMode = true; //whether to disable or enable brake mode Nominal: true
    private double deadBandVal = 0.15; //nominal deadband 0.15 percent of stick
    private double xyPercentage = 0.75; // decrease xy output to percent of full Nominal: 0.75
    private double zPercentage = 0.50; // z percentage of full stick deflection Nominal: 80
    private double frontRamp = 2.0; //ramp time on front motors Nominal: 2.0
    private double rearRamp = 2.0; //ramp time on rear motors Nominal: 2.0

    //PID variables
    private double Kp = 0.020; //stable at 0.020
    private double Ki = 0.0; //dont generally use Integral as it makes things unstable over time
    private double Kd = 0.075; //was 0.025
    private double absTol = 0.5; //tolerance on PID control Nominal: 0.5
    private double pidLimit = 0.6; //limits pid output Nominal: 0.6


    public RobotDriveV2() {
        gyroLock = new PIDController(Kp, Ki, Kd, this, this::pidWrite);
        gyroLock.setAbsoluteTolerance(absTol);
        gyroLock.setOutputRange(-1, 1);
        gyroLock.setInputRange(0, 360);
        gyroLock.setContinuous();
        setBrakeMode(brakeMode);
        setRamp(frontRamp, rearRamp);
    }

    @Override
    public void initDefaultCommand() {

    }

    @Override
    public double pidGet() {
        return getGyro();
    }

    @Override
    public void pidWrite(double output) {
        pidOutput = output;
    }

    public void periodic() {
        if (!DriverStation.getInstance().isAutonomous()) lastInputSet = getAdjStick();
        if (isReversed) {
            lastInputSet[0] *= -1;
            lastInputSet[1] *= -1;
        }
        SmartDashboard.putNumberArray("compensated stick values", lastInputSet);
        SmartDashboard.putNumber("Gyro Target", gyroLock.getSetpoint());
        SmartDashboard.putNumber("Gyro Angle", getGyro());
        if (enLock) {
            // Periodically updates while gyro locked
            setCartesianDrive(lastInputSet[0], lastInputSet[1], pidOutput * pidLimit);

        } else {
            // periodically updates drive
            setCartesianDrive(lastInputSet[0], lastInputSet[1], lastInputSet[2]);
            setTarget(getGyro()); // Safety feature in case PID gets enabled
        }
    }

    /**
     * Enables gyro-lock to a certain setpoint -- used for auto
     *
     * @param rot - rotation angle target
     * @param en  - whether to enable or disable the lock
     */

    public void enableTo(double rot, boolean en) {
        this.setTarget(rot);
        this.enableLock(en);
    }

    public void setInput(double[] inputvals) {
        lastInputSet = inputvals;
    }

    /**
     * method for activating the axial reverse that the drivers use
     * it simply tells the periodic method to multipy x and y by -1
     *
     * @param isReversed whether or not is in a reversed state
     */
    public void flipRefrence(boolean isReversed) {
        this.isReversed = isReversed;
    }

    /**
     * method for getting current gyro heading
     *
     * @return current gyro position (0-359.99999)
     */
    public double getGyro() {
        return ((RobotMap.ahrs.getYaw() + 360) % 360); //add 360 to make all positive then mod by 360 to get remainder
    }

    /**
     * modify's the gains on the PID as needed
     *
     * @param p proportional gain
     * @param i integral gain
     * @param d derivative gain
     */
    public void setPid(double p, double i, double d) {
        gyroLock.setPID(p, i, d);
    }

    /**
     * gets the current status of the PID controller
     *
     * @return boolean is enabled
     */
    public boolean getPIDStatus() {
        return gyroLock.isEnabled();
    }

    /**
     * gets whether or not the PID controller is on target
     *
     * @return boolean is on target
     */
    public boolean isOnTarget() {
        return gyroLock.onTarget();
    }

    public void setRamp(double front, double rear) {
        RobotMap.driveMotor1.configOpenloopRamp(front, 10);
        RobotMap.driveMotor2.configOpenloopRamp(rear, 10);
        RobotMap.driveMotor3.configOpenloopRamp(front, 10);
        RobotMap.driveMotor4.configOpenloopRamp(rear, 10);
    }

    public void setBrakeMode(boolean brakeMode) {
        if (brakeMode) {
            RobotMap.driveMotor1.setNeutralMode(NeutralMode.Brake);
            RobotMap.driveMotor2.setNeutralMode(NeutralMode.Brake);
            RobotMap.driveMotor3.setNeutralMode(NeutralMode.Brake);
            RobotMap.driveMotor4.setNeutralMode(NeutralMode.Brake);
        }
        RobotMap.driveMotor1.setNeutralMode(NeutralMode.Coast);
        RobotMap.driveMotor2.setNeutralMode(NeutralMode.Coast);
        RobotMap.driveMotor3.setNeutralMode(NeutralMode.Coast);
        RobotMap.driveMotor4.setNeutralMode(NeutralMode.Coast);
    }

    //private methods here
    private void setCartesianDrive(double x, double y, double z) {
        RobotMap.robotdrive.driveCartesian(y, -x, z);
    }

    private void setTarget(double target) {
        gyroLock.setSetpoint(target);
    }

    private void enableLock(boolean en) {
        enLock = en;
        if (enLock) gyroLock.enable();
        else gyroLock.disable();
    }

    private double[] getAdjStick() {
        double[] out = new double[3];
        out[0] = evalDeadBand(Robot.oi.getMasterStick().getY(), deadBandVal) * xyPercentage;
        out[1] = evalDeadBand(Robot.oi.getMasterStick().getX(), deadBandVal) * xyPercentage;
        out[2] = evalDeadBand(Robot.oi.getMasterStick().getZ(), deadBandVal) * zPercentage;
        return out;
    }

    // figures out if the stick value is within the deadband
    private double evalDeadBand(double stickInpt, double deadBand) {
        if (Math.abs(stickInpt) < deadBand) {
            return 0;
        } else {
            if (stickInpt < 0) {
                return (0 - Math.pow(stickInpt, 2));
            } else {
                return Math.pow(stickInpt, 2);
            }
        }
    }

}
