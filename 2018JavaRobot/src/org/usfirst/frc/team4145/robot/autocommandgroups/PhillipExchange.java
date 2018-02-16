package org.usfirst.frc.team4145.robot.autocommandgroups;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team4145.robot.commands.autoonly.*;
import org.usfirst.frc.team4145.robot.shared.QueueGroup;

public class PhillipExchange extends QueueGroup {

	public PhillipExchange(int autonumber) {
		if (autonumber == 2) {
			// Center
			addSequential(new DriveTo(19 * 20), 1000);
			addSequential(new GyroToAngle(-90), 1000);
			addSequential(new DriveTo(19 * 67), 3000);
			addSequential(new GyroToAngle(-90), 1000);
			addParallel(new Command[] {new DriveTo(19 * 20), new DropForks()}, 500);
			addSequential(new DropCube(), 1000);
			addSequential(new GyroToAngle( 200), 2000);
			addSequential(new DriveTo(19 * 89),3000);
			addSequential(new PickCube(), 2000);
		}
		if (autonumber == 1) {
			// Far Left
			addSequential(new DriveTo(19 * 20), 1000);
			addSequential(new GyroToAngle(90), 1000);
			addSequential(new DriveTo(19 * 40), 1500);
			addSequential(new GyroToAngle(90), 1000);
			addParallel(new Command[] {new DriveTo(19 * 20), new DropForks()}, 1000);
			addSequential(new DropCube(), 1000);
			addSequential(new GyroToAngle(200), 2000);
			addSequential(new DriveTo(19 * 89),3000);
			addSequential(new PickCube(), 2000);
		}
		if (autonumber == 3) {
			// Far right
			addSequential(new DriveTo(19 * 20), 1000);
			addSequential(new GyroToAngle(-90), 1000);
			addSequential(new DriveTo(19 * 144), 4000);
			addSequential(new GyroToAngle(-90), 1000);
			addParallel(new Command[] {new DriveTo(19 * 20), new DropForks()}, 1000);
			addSequential(new DropCube(), 1000);
			addSequential(new GyroToAngle(200), 2000);
			addSequential(new DriveTo(19 * 89),3000);
			addSequential(new PickCube(), 2000);
		}
	}
}
