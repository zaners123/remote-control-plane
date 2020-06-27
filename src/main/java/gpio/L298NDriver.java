package gpio;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.wiringpi.SoftPwm;

public class L298NDriver {

	public final static int MAX_MOTOR_POWERS = 75;

	GpioPinDigitalOutput in1;
	GpioPinDigitalOutput in2;
	GpioPinDigitalOutput in3;
	GpioPinDigitalOutput in4;
	int enaLoc;
	int enbLoc;

	public L298NDriver(int in1Loc, int in2Loc, int in3Loc, int in4Loc, int enaLoc, int enbLoc, boolean aBackwards, boolean bBackwards) {
		if (aBackwards) {
			int swapA = in1Loc;
			in1Loc = in2Loc;
			in2Loc = swapA;
		}

		if (bBackwards) {
			int swapB = in3Loc;
			in3Loc = in4Loc;
			in4Loc = swapB;
		}

		in1 = gpio.Controller.getDigitalGPIOOutput(in1Loc);
		in2 = gpio.Controller.getDigitalGPIOOutput(in2Loc);
		in3 = gpio.Controller.getDigitalGPIOOutput(in3Loc);
		in4 = gpio.Controller.getDigitalGPIOOutput(in4Loc);
		SoftPwm.softPwmCreate(enaLoc, 0, 100);
		SoftPwm.softPwmCreate(enbLoc, 0, 100);
		this.enaLoc = enaLoc;
		this.enbLoc = enbLoc;
	}
	/**
	 * The bit of custom code this driver needs to function
	 * @param power ranges from -100 to 100 (where -100 is full negative voltage, 0 is off, 100 is full voltage)
	 * */
	private void setPowerLevel(int power, GpioPinDigitalOutput d1, GpioPinDigitalOutput d2, int pwm) {
		if (power==0) {
			d1.low();
			d2.low();
		} else {
			if (power>0) {
				d1.high();
				d2.low();
			} else {
				d1.low();
				d2.high();
			}
			int setPwmTo = clamp(Math.abs(power),0,MAX_MOTOR_POWERS);
			System.out.println("Setting PWM to "+setPwmTo);
			SoftPwm.softPwmWrite(pwm,setPwmTo);
		}
	}

	private int clamp(int v, int lo, int hi) {
		return Math.max(lo,(Math.min(v, hi)));
	}

	/**
	 * @param rightWheel true if setting right output, false if setting left output
	 * */
	public void setPowerLevel(boolean rightWheel, int power) {
		if (rightWheel) {
			setPowerLevel(power, in3, in4, enbLoc);
		} else {
			setPowerLevel(power, in1, in2, enaLoc);
		}
	}


	public void setLeftPowerLevel(int power) {setPowerLevel(false, power);}
	public void setRightPowerLevel(int power) {setPowerLevel(true, power);}

	public void stop() {
		SoftPwm.softPwmStop(enaLoc);
		SoftPwm.softPwmStop(enbLoc);
	}
}
