package plane;

import com.pi4j.wiringpi.SoftPwm;

class FlapController {

    private final static int PWM_SERVO_MIN = 3;
    private final static int PWM_SERVO_MAX = 27;
    private final static int PWM_SERVO_MIDDLE = (PWM_SERVO_MAX + PWM_SERVO_MIN)/2;
    private final static int PWM_SERVO_RANGE = PWM_SERVO_MAX - PWM_SERVO_MIN;

    public enum FlapPin {
        RUDDER(4),
        LEFT_AILERON(2),
        RIGHT_AILERON(3),
        LEFT_ELEVATOR(5),
        RIGHT_ELEVATOR(6);

        private final int gpio;
        FlapPin(int gpio) {
            this.gpio=gpio;
        }
    }

    private void initializeFlaps() {
        //run softPwmCreate on every GPIO in Plane.propPins (unless it's -1, then it isn't yet made)
        for (FlapPin p : FlapPin.values()) {
            if (p.gpio==-1) continue;
            //start them off in the middle
            SoftPwm.softPwmCreate(p.gpio, PWM_SERVO_MIDDLE, PWM_SERVO_MAX);
        }
    }

    void close() {
        //run softPwmStop on every GPIO in Plane.propPins (unless it's -1, then it isn't yet made)
        for (FlapPin p : FlapPin.values()) {
            if (p.gpio==-1) continue;
            SoftPwm.softPwmStop(p.gpio);
        }
    }

    FlapController() {
        initializeFlaps();
    }

    void writeFlap(FlapPin pin, float percent) {
        if (pin.gpio<0) {
            System.err.println("writeFlap bad ID. ID="+pin.gpio+" Percent="+percent);
            return;
        }

        if (percent < 0) {
            System.err.println("writeFlap too low  percent. ID="+pin.gpio+" Percent="+percent);
            percent = 0;
        } else if (percent > 100) {
            System.err.println("writeFlap too high percent. ID="+pin.gpio+" Percent="+percent);
            percent = 100;
        }

        //calculate where percent lies on spectrum
        int value =  (int)(PWM_SERVO_MIN + (percent * PWM_SERVO_RANGE));
        SoftPwm.softPwmWrite(pin.gpio,value);

    }

}