package org.usfirst.frc.team4145.robot.subsystems.RobotDriveV4;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4145.robot.Constants;
import org.usfirst.frc.team4145.robot.RobotMap;
import org.usfirst.frc.team4145.robot.shared.AutoTrajectory.*;

import java.util.Map;


public class PoseEstimator {

    private Notifier m_NotifierInstance;
    protected InterpolatingTreeMap<InterpolatingDouble, RigidTransform2d> fieldToVehicle;
    protected double leftPrevEncCount = 0;
    protected double rightPrevEncCount = 0;
    protected static final RigidTransform2d encodersToVehicle = new RigidTransform2d(new Translation2d(Constants.ENCODER_X_OFFSET, Constants.ENCODER_Y_OFFSET),
            Rotation2d.fromDegrees(Constants.ENCODER_ROTATION_OFFSET));


    public PoseEstimator(){
        m_NotifierInstance = new Notifier(loop);
        reset(0, new RigidTransform2d());
        m_NotifierInstance.startPeriodic(Constants.DRIVETRAIN_UPDATE_RATE);
    }

    public void reset(double start_time, RigidTransform2d initial_field_to_vehicle){
        fieldToVehicle = new InterpolatingTreeMap<>(Constants.OBSERVATION_BUFFER_SIZE);
        fieldToVehicle.put(new InterpolatingDouble(start_time), initial_field_to_vehicle);
        leftPrevEncCount = 0;
        rightPrevEncCount = 0;
    }

    public synchronized Map.Entry<InterpolatingDouble, RigidTransform2d> getLatestFieldToVehicle() {
        return fieldToVehicle.lastEntry();
    }


    private Runnable loop = () -> {
        double currentTime = Timer.getFPGATimestamp();
        double currentLeftEncoder = RobotMap.robotDriveV4.getLeftEncoder() * ((Constants.WHEEL_DIAMETER * 3.14159) / Constants.COUNTS_PER_REV);
        double currentRightEncoder = RobotMap.robotDriveV4.getRightEncoder() * ((Constants.WHEEL_DIAMETER * 3.14159) / Constants.COUNTS_PER_REV);
        Rotation2d gyro = Rotation2d.fromDegrees(RobotMap.robotDriveV4.getGyroContinuous());
        RigidTransform2d odometry = generateOdometryFromSensors((currentLeftEncoder - leftPrevEncCount), (currentRightEncoder - rightPrevEncCount), gyro);
        SmartDashboard.putString("Odometry", odometry.toString());
        SmartDashboard.putString("Inverse Odometry", odometry.inverse().toString());
        RigidTransform2d transOdometry = odometry.transformBy(encodersToVehicle); //transforms to encoder frame of refrence - may need inverse
        SmartDashboard.putString("Trans Odometry", transOdometry.toString());
        SmartDashboard.putString("Inverse Trans Odometry", transOdometry.inverse().toString());
        addObservations(currentTime, odometry);
        outputToSmartDashboard();
        leftPrevEncCount = currentLeftEncoder;
        rightPrevEncCount = currentRightEncoder;
    };

    public RigidTransform2d generateOdometryFromSensors(double left_encoder_delta_distance, double right_encoder_delta_distance, Rotation2d current_gyro_angle) {
        RigidTransform2d last_measurement = getLatestFieldToVehicle().getValue();
        return Kinematics.integrateForwardKinematics(last_measurement, left_encoder_delta_distance, right_encoder_delta_distance, current_gyro_angle);
    }

    public synchronized void addObservations(double timestamp, RigidTransform2d observation) {
        fieldToVehicle.put(new InterpolatingDouble(timestamp), observation);
    }


    public void outputToSmartDashboard() {
        RigidTransform2d odometry = getLatestFieldToVehicle().getValue();
        SmartDashboard.putNumber("Robot Pose X", odometry.getTranslation().getX());
        SmartDashboard.putNumber("Robot Pose Y", odometry.getTranslation().getY());
        SmartDashboard.putNumber("Robot Pose Theta", odometry.getRotation().getDegrees());
    }

}
