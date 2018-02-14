package org.usfirst.frc.team4145.robot.autocommandgroups;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.usfirst.frc.team4145.robot.commands.autoonly.*;
import org.usfirst.team4145.robot.shared.QueueGroup;

public class MeinekeSwitch extends CommandGroup {
	public MeinekeSwitch(int autonomous) {

		if (autonomous == 0) {
			//Forward 109 inches, raise lift, drop forks
			addParallel(new Command[] {new DriveTo(19*109), new liftToPosition(1000), new DropForks()}, 2500);
			addSequential(new GyroToAngle(-15), 1500);// turn left 15 degrees
			addSequential(new DropCube(), 500); // launch cube 
		}
		else
		{
			addSequential(new GyroToAngle(10), 1500);//turn right 10 degrees
			addSequential(new DriveTo(19*199), 2000);//forward 199 inches
			addSequential(new GyroToAngle(-90), 2000);//turn left 90 degrees
			// Forward 189 inches, raise lift, drop forks
			addParallel(new Command[] {new DriveTo(19*189), new liftToPosition(1000), new DropForks()}, 2000);
			addSequential(new GyroToAngle(-90), 2000);//turn left 90 degrees
			addSequential(DropCube(), 500);//launch cube 
		}
	}
}