package plane;

import com.pi4j.wiringpi.SoftPwm;
import com.sun.istack.internal.NotNull;

import java.util.Scanner;

class PWMController {

    public enum PinType {
        PROPELLER(  8,  24, 0),
        SERVO(      3,  27, 15);

        //safe min value (or starting value)
        int SAFE_MIN;
        //safe max value (or ending value)
        int SAFE_MAX;
        //safeMax - safeMin
        int SAFE_RANGE;
        //servos start in middle, props start at off
        int STARTING_VALUE;

        PinType(int SAFE_MIN, int SAFE_MAX, int STARTING_VALUE) {
            this.SAFE_MAX = SAFE_MAX;
            this.SAFE_MIN = SAFE_MIN;
            this.STARTING_VALUE = STARTING_VALUE;
            //set range
            this.SAFE_RANGE = this.SAFE_MAX - this.SAFE_MIN;
        }

    }

    public enum PwmPin {
        RUDDER(        -1,PinType.SERVO),
        LEFT_AILERON(  -1,PinType.SERVO),
        RIGHT_AILERON( -1,PinType.SERVO),
        LEFT_ELEVATOR( -1,PinType.SERVO),
        RIGHT_ELEVATOR(-1,PinType.SERVO),
        //pins 23(24) are PWM1
        //pins 26(1)  are PWM0
        LEFT_PROP(     26,PinType.PROPELLER),
        RIGHT_PROP(    23,PinType.PROPELLER);

        private final int gpio;
        private final PinType pinType;
        PwmPin(int gpio, PinType pinType) {
            this.gpio=gpio;
            this.pinType = pinType;
        }
    }

    private void initialize() {
        for (PwmPin pin : PwmPin.values()) {
            if (pin.gpio < 0) {
                System.err.println("Prop " + pin.name() + " has bad GPIO " + pin.gpio + " ; failed to initialize");
            } else {
                SoftPwm.softPwmCreate(pin.gpio, pin.pinType.STARTING_VALUE, 100);
                SoftPwm.softPwmWrite(pin.gpio, pin.pinType.STARTING_VALUE);
            }
        }
    }

    PWMController() {
        //initialize all the props to their starting value
        initialize();
        //main use this to calibrate the props
        calibratePropsManually(  PwmPin.LEFT_PROP, PwmPin.RIGHT_PROP);
    }

    /**Calibrates all given props in sync... technically not really calibrating shit; good for tests
     *
     * */
    private static void calibratePropsManually(@NotNull PwmPin... pins) {
        for(PwmPin pin : pins) {
            if (pin.pinType != PinType.PROPELLER) {
                System.err.println("Failed at manually calibrating "+pin.name()+" because its pin type is "+pin.pinType.name());
                return;
            } else if (pin.gpio < 0) {
                System.err.println("Failed at manually calibrating "+pin.name()+" because its GPIO is "+pin.gpio);
                return;
            }
        }
        Scanner s = new Scanner(System.in);
        String names = "";
        for(PwmPin pin : pins) names+=pin.name()+", ";
        System.out.println("Calibrating "+pins.length+" pin(s) at same time ("+names+"). Press enter when ready?");
        s.nextLine();
        boolean t = true;
        while (t) {
            int out;
            try {
                String line = s.nextLine();
                if (line.equals("end")) break;
                out = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Not a number.");
                continue;
            }
            for(PwmPin pin : pins) SoftPwm.softPwmWrite(pin.gpio, out);
            System.out.println("Sending "+out);
        }
    }

    static void writePercent(@NotNull PwmPin pin, float percent) {
        if (pin.gpio < 0) {
            System.err.println("PWM Pin "+pin.name()+" has bad GPIO "+pin.gpio);
            return;
        }

        if (percent < 0) {
            System.err.println("writePercent called with percent < 0 ("+percent+") for pin" +pin.name());
            percent = 0;
        } else if (percent > 100) {
            System.err.println("writePercent called with percent > 100 ("+percent+") for pin"+pin.name());
            percent = 100;
        }

        int power = (int)(pin.pinType.SAFE_MIN + (pin.pinType.SAFE_MAX * percent/100));
        System.out.println("writePercent percent "+percent+" turned into power "+power);
        SoftPwm.softPwmWrite(pin.gpio, power);
    }

    void close() {
        for (PwmPin p : PwmPin.values()) {
            if (p.gpio >= 0) {
                //turn it off
                SoftPwm.softPwmWrite(p.gpio, 0);
                //release driver thing
                SoftPwm.softPwmStop(p.gpio);
            }
        }
    }
}
