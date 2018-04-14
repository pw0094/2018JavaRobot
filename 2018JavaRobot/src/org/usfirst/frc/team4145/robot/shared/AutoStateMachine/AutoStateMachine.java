package org.usfirst.frc.team4145.robot.shared.AutoStateMachine;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4145.robot.Constants;
import org.usfirst.frc.team4145.robot.RobotMap;
import org.usfirst.frc.team4145.robot.commands.autoonly.FollowPath;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoStateMachine {

    private static ConcurrentLinkedQueue<CommandQueueGroup> stateQueue;
    private static CommandQueueGroup inspectedElement, lastDrivingCommand;
    private static int autoState = 0;

    private static Runnable taskRunnable = () -> {
    	autoState = 0;
        if (stateQueue != null) {
            SmartDashboard.putString("Auto State Machine Status", "State machine preparing to start!");
            while (!stateQueue.isEmpty()) {
                SmartDashboard.putNumber("Auto State", autoState);
                inspectedElement = stateQueue.poll();
                while (inspectedElement.getQueueGroup().peek() instanceof FollowPath && (lastDrivingCommand != null && !lastDrivingCommand.checkNoTimeout())) {
                    SmartDashboard.putString("Auto State Machine Status", "Waiting for previous driving task to die");
                    Timer.delay(Constants.STATE_MACHINE_UPDATE_RATE);
                }
                if(inspectedElement.getQueueGroup().peek() instanceof FollowPath){
                    if(lastDrivingCommand != null){
                        Timer.delay(Constants.STATE_MACHINE_UPDATE_RATE * Constants.DRIVING_WAIT_CYCLES);
                    }
                    lastDrivingCommand = inspectedElement;
                }
                inspectedElement.startQueueGroup(); //starts queue group running
                while (!inspectedElement.checkQueueGroup()) { //checks status of state and whether it is or should be dead
                    SmartDashboard.putString("Auto State Machine Status", "Running state " + autoState);
                    Timer.delay(Constants.STATE_MACHINE_UPDATE_RATE);
                }
                inspectedElement.killQueueGroup(); //forcefully kills group (just in case)
                autoState++; //increment auto state
            }

            //System.out.println("Finished queue");
            SmartDashboard.putString("Auto State Machine Status", "State machine finished Queue");
        } else {
            DriverStation.reportWarning("State Machine list was NULL", false);
            SmartDashboard.putNumber("Auto State", -2);
            SmartDashboard.putString("Auto State Machine Status", "List was NULL!");
        }
    };

    public static void runMachine(ConcurrentLinkedQueue<CommandQueueGroup> queueGroups) {
        stateQueue = queueGroups;
        Thread monitor = new Thread(taskRunnable);
        //monitor.setPriority(8);
        System.out.println("Starting Machine thread at time " + RobotController.getFPGATime());
        monitor.start();
    }

}
