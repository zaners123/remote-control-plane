package plane;

import com.pi4j.wiringpi.SoftPwm;

import java.util.Scanner;

class PropController {
    //values calibrated as the literal max and min of signal input
    private final static int CALIBRATED_UNSAFE_MIN = 0;
    private final static int CALIBRATED_UNSAFE_MAX = 24;

    //values that are the safe input to use
    private final static int SAFE_MIN   = 12;
    private final static int SAFE_MAX   = 20;
    private final static int SAFE_RANGE = SAFE_MAX - SAFE_MIN;

    //pins to use as props
    final static int GPIO_LEFT_PROP     = 26;//pins 26(1)  are PWM0
    final static int GPIO_RIGHT_PROP    = 23;//pins 23(24) are PWM1

    public enum PropPin {
        LEFT_PROP(26),
        RIGHT_PROP(-1);

        private final int gpio;
        PropPin(int gpio) {
            this.gpio=gpio;
        }
    }

    private void initialize(PropPin pin) {
        if (pin.gpio < 0) {
            System.err.println("Pin "+pin.name()+" has bad GPIO "+pin.gpio);
        } else {
            SoftPwm.softPwmCreate(pin.gpio, 0,100);
        }
        SoftPwm.softPwmWrite(pin.gpio, 0);

    }

    PropController() {
        initialize(         PropPin.LEFT_PROP);
//        initialize(         PropPin.RIGHT_PROP);
        calibrateManually(  PropPin.LEFT_PROP);
//        calibrateManually(  PropPin.RIGHT_PROP);
    }
    //main calibrateManually propController. Currently done manually
    private void calibrateManually(PropPin pin) {
        if (pin.gpio < 0) {
            System.out.println("Failed at manually calibrating "+pin.name()+" because its GPIO is "+pin.gpio);
            return;
        }
        Scanner s = new Scanner(System.in);
        System.out.println("Calibrating "+pin.name()+". Press enter when ready?");
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
            SoftPwm.softPwmWrite(pin.gpio, out);
            System.out.println("Sending "+out);
        }
    }

    private void calibrateAutomatic() {
        //todo automatic calibration (not required)
    }

    void writeProp(PropPin pin, float percent) {
        if (percent < 0) {
            System.err.println("writeProp called with percent < 0 ("+percent+")");
            percent = 0;
        } else if (percent > 100) {
            System.err.println("writeProp called with percent > 100 ("+percent+")");
            percent = 100;
        }

        int power;
        if (percent==0) {
            //use actual zero on off
            power = 0;
        } else {
            power = (int)(SAFE_MIN + ((percent/100) * (SAFE_MAX-SAFE_MIN)));
        }
        System.out.println("writeProp percent "+percent+" turned into power "+power);
        SoftPwm.softPwmWrite(pin.gpio, power);
    }


    void close() {
        for (PropPin p : PropPin.values()) {
            if (p.gpio >= 0) {
                SoftPwm.softPwmStop(p.gpio);
            }
        }
    }
}
