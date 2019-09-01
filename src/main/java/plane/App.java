package plane;

import com.pi4j.io.gpio.*;
import com.pi4j.io.serial.*;
import com.pi4j.wiringpi.SoftPwm;

import java.io.IOException;
import java.util.HashMap;
/*

UART help:
https://medium.com/@erleczarmantos/connecting-the-bosch-bno055-shuttleboard-to-the-raspberry-pi-72e8a86048d4


*/
public class App {

    // I2C addresses;
    private byte BNO055_ADDRESS_A                     = (byte)(0x28 & 0xFF );
    private byte BNO055_ADDRESS_B                     = (byte)(0x29 & 0xFF );
    private byte BNO055_ID                            = (byte)(0xA0 & 0xFF );
    // Page id register definition & 0xFF );
    private byte BNO055_PAGE_ID_ADDR                  = (byte)(0X07 & 0xFF );
    // PAGE0 REGISTER DEFINITION START & 0xFF );
    private byte BNO055_CHIP_ID_ADDR                  = (byte)(0x00 & 0xFF );
    private byte BNO055_ACCEL_REV_ID_ADDR             = (byte)(0x01 & 0xFF );
    private byte BNO055_MAG_REV_ID_ADDR               = (byte)(0x02 & 0xFF );
    private byte BNO055_GYRO_REV_ID_ADDR              = (byte)(0x03 & 0xFF );
    private byte BNO055_SW_REV_ID_LSB_ADDR            = (byte)(0x04 & 0xFF );
    private byte BNO055_SW_REV_ID_MSB_ADDR            = (byte)(0x05 & 0xFF );
    private byte BNO055_BL_REV_ID_ADDR                = (byte)(0X06 & 0xFF );
    // Accel data register & 0xFF );
    private byte BNO055_ACCEL_DATA_X_LSB_ADDR         = (byte)(0X08 & 0xFF );
    private byte BNO055_ACCEL_DATA_X_MSB_ADDR         = (byte)(0X09 & 0xFF );
    private byte BNO055_ACCEL_DATA_Y_LSB_ADDR         = (byte)(0X0A & 0xFF );
    private byte BNO055_ACCEL_DATA_Y_MSB_ADDR         = (byte)(0X0B & 0xFF );
    private byte BNO055_ACCEL_DATA_Z_LSB_ADDR         = (byte)(0X0C & 0xFF );
    private byte BNO055_ACCEL_DATA_Z_MSB_ADDR         = (byte)(0X0D & 0xFF );
    // Mag data register & 0xFF );
    private byte BNO055_MAG_DATA_X_LSB_ADDR           = (byte)(0X0E & 0xFF );
    private byte BNO055_MAG_DATA_X_MSB_ADDR           = (byte)(0X0F & 0xFF );
    private byte BNO055_MAG_DATA_Y_LSB_ADDR           = (byte)(0X10 & 0xFF );
    private byte BNO055_MAG_DATA_Y_MSB_ADDR           = (byte)(0X11 & 0xFF );
    private byte BNO055_MAG_DATA_Z_LSB_ADDR           = (byte)(0X12 & 0xFF );
    private byte BNO055_MAG_DATA_Z_MSB_ADDR           = (byte)(0X13 & 0xFF );
    // Gyro data registers & 0xFF );
    private byte BNO055_GYRO_DATA_X_LSB_ADDR          = (byte)(0X14 & 0xFF );
    private byte BNO055_GYRO_DATA_X_MSB_ADDR          = (byte)(0X15 & 0xFF );
    private byte BNO055_GYRO_DATA_Y_LSB_ADDR          = (byte)(0X16 & 0xFF );
    private byte BNO055_GYRO_DATA_Y_MSB_ADDR          = (byte)(0X17 & 0xFF );
    private byte BNO055_GYRO_DATA_Z_LSB_ADDR          = (byte)(0X18 & 0xFF );
    private byte BNO055_GYRO_DATA_Z_MSB_ADDR          = (byte)(0X19 & 0xFF );
    // Euler data registers & 0xFF );
    private byte BNO055_EULER_H_LSB_ADDR              = (byte)(0X1A & 0xFF );
    private byte BNO055_EULER_H_MSB_ADDR              = (byte)(0X1B & 0xFF );
    private byte BNO055_EULER_R_LSB_ADDR              = (byte)(0X1C & 0xFF );
    private byte BNO055_EULER_R_MSB_ADDR              = (byte)(0X1D & 0xFF );
    private byte BNO055_EULER_P_LSB_ADDR              = (byte)(0X1E & 0xFF );
    private byte BNO055_EULER_P_MSB_ADDR              = (byte)(0X1F & 0xFF );
    // Quaternion data registers & 0xFF );
    private byte BNO055_QUATERNION_DATA_W_LSB_ADDR    = (byte)(0X20 & 0xFF );
    private byte BNO055_QUATERNION_DATA_W_MSB_ADDR    = (byte)(0X21 & 0xFF );
    private byte BNO055_QUATERNION_DATA_X_LSB_ADDR    = (byte)(0X22 & 0xFF );
    private byte BNO055_QUATERNION_DATA_X_MSB_ADDR    = (byte)(0X23 & 0xFF );
    private byte BNO055_QUATERNION_DATA_Y_LSB_ADDR    = (byte)(0X24 & 0xFF );
    private byte BNO055_QUATERNION_DATA_Y_MSB_ADDR    = (byte)(0X25 & 0xFF );
    private byte BNO055_QUATERNION_DATA_Z_LSB_ADDR    = (byte)(0X26 & 0xFF );
    private byte BNO055_QUATERNION_DATA_Z_MSB_ADDR    = (byte)(0X27 & 0xFF );
    // Linear acceleration data registers & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR  = (byte)(0X28 & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_X_MSB_ADDR  = (byte)(0X29 & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_Y_LSB_ADDR  = (byte)(0X2A & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_Y_MSB_ADDR  = (byte)(0X2B & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_Z_LSB_ADDR  = (byte)(0X2C & 0xFF );
    private byte BNO055_LINEAR_ACCEL_DATA_Z_MSB_ADDR  = (byte)(0X2D & 0xFF );
    // Gravity data registers & 0xFF );
    private byte BNO055_GRAVITY_DATA_X_LSB_ADDR       = (byte)(0X2E & 0xFF );
    private byte BNO055_GRAVITY_DATA_X_MSB_ADDR       = (byte)(0X2F & 0xFF );
    private byte BNO055_GRAVITY_DATA_Y_LSB_ADDR       = (byte)(0X30 & 0xFF );
    private byte BNO055_GRAVITY_DATA_Y_MSB_ADDR       = (byte)(0X31 & 0xFF );
    private byte BNO055_GRAVITY_DATA_Z_LSB_ADDR       = (byte)(0X32 & 0xFF );
    private byte BNO055_GRAVITY_DATA_Z_MSB_ADDR       = (byte)(0X33 & 0xFF );
    // Temperature data register & 0xFF );
    private byte BNO055_TEMP_ADDR                     = (byte)(0X34 & 0xFF );
    // Status registers & 0xFF );
    private byte BNO055_CALIB_STAT_ADDR               = (byte)(0X35 & 0xFF );
    private byte BNO055_SELFTEST_RESULT_ADDR          = (byte)(0X36 & 0xFF );
    private byte BNO055_INTR_STAT_ADDR                = (byte)(0X37 & 0xFF );
    private byte BNO055_SYS_CLK_STAT_ADDR             = (byte)(0X38 & 0xFF );
    private byte BNO055_SYS_STAT_ADDR                 = (byte)(0X39 & 0xFF );
    private byte BNO055_SYS_ERR_ADDR                  = (byte)(0X3A & 0xFF );
    // Unit selection register & 0xFF );
    private byte BNO055_UNIT_SEL_ADDR                 = (byte)(0X3B & 0xFF );
    private byte BNO055_DATA_SELECT_ADDR              = (byte)(0X3C & 0xFF );
    // Mode registers & 0xFF );
    private byte BNO055_OPR_MODE_ADDR                 = (byte)(0X3D & 0xFF );
    private byte BNO055_PWR_MODE_ADDR                 = (byte)(0X3E & 0xFF );
    private byte BNO055_SYS_TRIGGER_ADDR              = (byte)(0X3F & 0xFF );
    private byte BNO055_TEMP_SOURCE_ADDR              = (byte)(0X40 & 0xFF );
    // Axis remap registers & 0xFF );
    private byte BNO055_AXIS_MAP_CONFIG_ADDR          = (byte)(0X41 & 0xFF );
    private byte BNO055_AXIS_MAP_SIGN_ADDR            = (byte)(0X42 & 0xFF );
    // Axis remap values & 0xFF );
    private byte AXIS_REMAP_X                         = (byte)(0x00 & 0xFF );
    private byte AXIS_REMAP_Y                         = (byte)(0x01 & 0xFF );
    private byte AXIS_REMAP_Z                         = (byte)(0x02 & 0xFF );
    private byte AXIS_REMAP_POSITIVE                  = (byte)(0x00 & 0xFF );
    private byte AXIS_REMAP_NEGATIVE                  = (byte)(0x01 & 0xFF );
    // SIC registers & 0xFF );
    private byte BNO055_SIC_MATRIX_0_LSB_ADDR         = (byte)(0X43 & 0xFF );
    private byte BNO055_SIC_MATRIX_0_MSB_ADDR         = (byte)(0X44 & 0xFF );
    private byte BNO055_SIC_MATRIX_1_LSB_ADDR         = (byte)(0X45 & 0xFF );
    private byte BNO055_SIC_MATRIX_1_MSB_ADDR         = (byte)(0X46 & 0xFF );
    private byte BNO055_SIC_MATRIX_2_LSB_ADDR         = (byte)(0X47 & 0xFF );
    private byte BNO055_SIC_MATRIX_2_MSB_ADDR         = (byte)(0X48 & 0xFF );
    private byte BNO055_SIC_MATRIX_3_LSB_ADDR         = (byte)(0X49 & 0xFF );
    private byte BNO055_SIC_MATRIX_3_MSB_ADDR         = (byte)(0X4A & 0xFF );
    private byte BNO055_SIC_MATRIX_4_LSB_ADDR         = (byte)(0X4B & 0xFF );
    private byte BNO055_SIC_MATRIX_4_MSB_ADDR         = (byte)(0X4C & 0xFF );
    private byte BNO055_SIC_MATRIX_5_LSB_ADDR         = (byte)(0X4D & 0xFF );
    private byte BNO055_SIC_MATRIX_5_MSB_ADDR         = (byte)(0X4E & 0xFF );
    private byte BNO055_SIC_MATRIX_6_LSB_ADDR         = (byte)(0X4F & 0xFF );
    private byte BNO055_SIC_MATRIX_6_MSB_ADDR         = (byte)(0X50 & 0xFF );
    private byte BNO055_SIC_MATRIX_7_LSB_ADDR         = (byte)(0X51 & 0xFF );
    private byte BNO055_SIC_MATRIX_7_MSB_ADDR         = (byte)(0X52 & 0xFF );
    private byte BNO055_SIC_MATRIX_8_LSB_ADDR         = (byte)(0X53 & 0xFF );
    private byte BNO055_SIC_MATRIX_8_MSB_ADDR         = (byte)(0X54 & 0xFF );
    // Accelerometer Offset registers & 0xFF );
    private byte ACCEL_OFFSET_X_LSB_ADDR              = (byte)(0X55 & 0xFF );
    private byte ACCEL_OFFSET_X_MSB_ADDR              = (byte)(0X56 & 0xFF );
    private byte ACCEL_OFFSET_Y_LSB_ADDR              = (byte)(0X57 & 0xFF );
    private byte ACCEL_OFFSET_Y_MSB_ADDR              = (byte)(0X58 & 0xFF );
    private byte ACCEL_OFFSET_Z_LSB_ADDR              = (byte)(0X59 & 0xFF );
    private byte ACCEL_OFFSET_Z_MSB_ADDR              = (byte)(0X5A & 0xFF );
    // Magnetometer Offset registers & 0xFF );
    private byte MAG_OFFSET_X_LSB_ADDR                = (byte)(0X5B & 0xFF );
    private byte MAG_OFFSET_X_MSB_ADDR                = (byte)(0X5C & 0xFF );
    private byte MAG_OFFSET_Y_LSB_ADDR                = (byte)(0X5D & 0xFF );
    private byte MAG_OFFSET_Y_MSB_ADDR                = (byte)(0X5E & 0xFF );
    private byte MAG_OFFSET_Z_LSB_ADDR                = (byte)(0X5F & 0xFF );
    private byte MAG_OFFSET_Z_MSB_ADDR                = (byte)(0X60 & 0xFF );
    // Gyroscope Offset register s & 0xFF );
    private byte GYRO_OFFSET_X_LSB_ADDR               = (byte)(0X61 & 0xFF );
    private byte GYRO_OFFSET_X_MSB_ADDR               = (byte)(0X62 & 0xFF );
    private byte GYRO_OFFSET_Y_LSB_ADDR               = (byte)(0X63 & 0xFF );
    private byte GYRO_OFFSET_Y_MSB_ADDR               = (byte)(0X64 & 0xFF );
    private byte GYRO_OFFSET_Z_LSB_ADDR               = (byte)(0X65 & 0xFF );
    private byte GYRO_OFFSET_Z_MSB_ADDR               = (byte)(0X66 & 0xFF );
    // Radius registers & 0xFF );
    private byte ACCEL_RADIUS_LSB_ADDR                = (byte)(0X67 & 0xFF );
    private byte ACCEL_RADIUS_MSB_ADDR                = (byte)(0X68 & 0xFF );
    private byte MAG_RADIUS_LSB_ADDR                  = (byte)(0X69 & 0xFF );
    private byte MAG_RADIUS_MSB_ADDR                  = (byte)(0X6A & 0xFF );
    // Power modes & 0xFF );
    private byte POWER_MODE_NORMAL                    = (byte)(0X00 & 0xFF );
    private byte POWER_MODE_LOWPOWER                  = (byte)(0X01 & 0xFF );
    private byte POWER_MODE_SUSPEND                   = (byte)(0X02 & 0xFF );
    // Operation mode settings & 0xFF );
    private byte OPERATION_MODE_CONFIG                = (byte)(0X00 & 0xFF );
    private byte OPERATION_MODE_ACCONLY               = (byte)(0X01 & 0xFF );
    private byte OPERATION_MODE_MAGONLY               = (byte)(0X02 & 0xFF );
    private byte OPERATION_MODE_GYRONLY               = (byte)(0X03 & 0xFF );
    private byte OPERATION_MODE_ACCMAG                = (byte)(0X04 & 0xFF );
    private byte OPERATION_MODE_ACCGYRO               = (byte)(0X05 & 0xFF );
    private byte OPERATION_MODE_MAGGYRO               = (byte)(0X06 & 0xFF );
    private byte OPERATION_MODE_AMG                   = (byte)(0X07 & 0xFF );
    private byte OPERATION_MODE_IMUPLUS               = (byte)(0X08 & 0xFF );
    private byte OPERATION_MODE_COMPASS               = (byte)(0X09 & 0xFF );
    private byte OPERATION_MODE_M4G                   = (byte)(0X0A & 0xFF );
    private byte OPERATION_MODE_NDOF_FMC_OFF          = (byte)(0X0B & 0xFF );
    private byte OPERATION_MODE_NDOF                  = (byte)(0X0C & 0xFF );



    private final GpioController gpio = GpioFactory.getInstance();
    private HashMap<Integer, GpioPinDigitalOutput> pinMap = new HashMap<>();

    private final static int RUDDER_GPIO = 1;
    private final static int LEFT_AILERON_GPIO = -1;
    private final static int RIGHT_AILERON_GPIO = -1;
    private final static int LEFT_ELEVATOR_GPIO = -1;
    private final static int RIGHT_ELEVATOR_GPIO = -1;
    private final static int[] flaps = new int[]{RUDDER_GPIO,LEFT_AILERON_GPIO,RIGHT_AILERON_GPIO,LEFT_ELEVATOR_GPIO,RIGHT_ELEVATOR_GPIO};
    private final static int SERVO_PWM_MIN = 3;
    private final static int SERVO_PWM_MAX = 27;
    private final static int SERVO_PWM_MID = (SERVO_PWM_MAX+SERVO_PWM_MIN)/2;


    public static void main(String[] args) {
        new App();
    }
    App() {
        //main start flight
        System.out.println("Started");
        GpioPinDigitalOutput STATUS_LIGHT = getDigitalGPIO(0);
        STATUS_LIGHT.high();
        //initializeFlaps();

        //TODO get sensor data in Java realtime
        sensorData();
        /*
        OPTIONS:
            could port deprecated python library to Pi4J
            could port new python library to Pi4J
            could have python write to ramdisk, java read from ramdisk
                Problem: would make latency some 10x as bad
        */


        //TODO have threads dedicated to server input, wings, EDF, etc.
        //main flap wings
        /*for (int i = 0; i < 5; i++) {
            SoftPwm.softPwmWrite(1, 16);
            sleep(600);
            SoftPwm.softPwmWrite(1, SERVO_PWM_MAX);
            sleep(600);
            SoftPwm.softPwmWrite(1, SERVO_PWM_MIN);
            sleep(600);
        }
        //main slowly move wings
        for (int i = SERVO_PWM_MIN; i < SERVO_PWM_MAX; i++) {
            System.out.println(i);
            setFlap(RUDDER_GPIO, i);
            sleep(100);
        }
        for (int i = SERVO_PWM_MAX; i >= SERVO_PWM_MIN; i--) {
            System.out.println(i);
            setFlap(RUDDER_GPIO, i);
            sleep(100);
        }*/


        //main done flying, deconstruct all
        //TODO make sure it's done, because if this is ran during flight, you're officially 100% fucked
        //terminateFlaps();
        STATUS_LIGHT.low();
        sleep(1000);
        System.out.println("Done... Exiting...");
        System.exit(0);
    }

    class SensorException extends Exception {
        SensorException(String s) {
            super(s);
        }
    }

    private byte writeSensorByte(Serial sensor, byte sensorRegister, byte writeData) throws IOException, SensorException {
        byte[] command = {
                (byte)0xAA, // start byte
                (byte)0x00, // main write
                sensorRegister, // register address
                (byte)0x01, // length
                writeData // consecutive bytes requested
        };
        System.out.print("Writing: "+toHex(command));

        sensor.write(command,0,command.length);
        byte[] response = sensor.read(2);
        if ((response[0] & 0xFF) != 0xEE) {
            throw new SensorException("Bad write Response (EE: ...) != ("+toHex(response)+")");
        }
        if ((response[1] & 0xFF) != 0x01) {
            throw new SensorException("Bad write Status (EE:01) != ("+toHex(response)+")");
        }
        return response[1];
    }

    private byte[] readSensorRegisterBytes(Serial sensor, byte sensorRegister) throws IOException, SensorException {
        return readSensorRegisterBytes(sensor, sensorRegister, (byte)0x01);
    }
    private byte[] readSensorRegisterBytes(Serial sensor, byte sensorRegister, byte unsignedCount) throws IOException, SensorException {
        byte[] command = {
                (byte)0xAA, // start byte
                (byte)0x01, // main read
                (byte)(sensorRegister & 0xFF), // register address
                (byte)(unsignedCount & 0xFF) // consecutive bytes requested
        };
        System.out.print("Sending: " + toHex(command));
        sensor.write(command,0,command.length);
        byte[] response = sensor.read(2);
        if ((response[0] & 0xFF) != 0xBB) {
            throw new SensorException("Bad read Response (BB: ...) != ("+toHex(response)+")");
        }
        int bytesToRead = response[1] & 0xFF;
        //System.out.println("Good Response (BB). Going to read "+bytesToRead+" bytes");
        return sensor.read(bytesToRead);
    }

    private void sensorData() {
        try {
            SerialConfig config = new SerialConfig();
            Serial serial = SerialFactory.createInstance();
            config.device(RaspberryPiSerial.S0_COM_PORT)
                    .baud(Baud._115200)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._1)
                    .flowControl(FlowControl.NONE);
            serial.open(config);

            //main put it in config mode (just to be sure...)
            System.out.println("Going to config mode");
            writeSensorByte(serial, BNO055_OPR_MODE_ADDR, OPERATION_MODE_CONFIG);
            System.out.println("Done Going to config mode");

            //read current mode (should be config)
            System.out.print("State: "+ toHex(readSensorRegisterBytes(serial, BNO055_OPR_MODE_ADDR)));

            //print chip ID
            System.out.print("Chip ID: " + toHex(readSensorRegisterBytes(serial, BNO055_CHIP_ID_ADDR)));

            //main go to page 0 (sensor config and output)
            System.out.println("Going to page 0");
            writeSensorByte(serial, BNO055_PAGE_ID_ADDR, (byte)0x00);

            //main set "power model" to normal mode
            //System.out.println("Going to normal power mode");
            //writeSensorByte(serial, BNO055_PWR_MODE_ADDR, POWER_MODE_NORMAL);

            //main set SYS_TRIGGER_ADDR so it's not resetting or test-ing or whatever
            System.out.println("Setting SYS TRIGGER off");
            writeSensorByte(serial, BNO055_SYS_TRIGGER_ADDR, (byte)0x00);

            //main "The default operation mode after power-on is CONFIGMODE". Put it in NDOF mode.
            System.out.println("Going to NDOF mode");
            writeSensorByte(serial, BNO055_OPR_MODE_ADDR, OPERATION_MODE_NDOF);
            System.out.println("Done Going to NDOF mode");
            sleep(50);

            //todo simple weather info
            for (int i = 0; i < 1; i++) {
                byte[] temp = readSensorRegisterBytes(serial, (byte)0x35);
                System.out.print("Temperature: "+toHex(temp));
                sleep(1000);
            }

            serial.close();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO restart sensor?
        } catch (SensorException e) {
            System.out.println("SensorException: "+e.toString());
            //TODO restart sensor?
        }
    }

    private void initializeFlaps() {
        //run softPwmCreate on every GPIO in flaps (unless it's -1, then it isn't yet made)
        for (int flap : flaps) {
            if (flap==-1) continue;
            //start them off in the middle
            SoftPwm.softPwmCreate(flap,SERVO_PWM_MID,SERVO_PWM_MAX);
        }
    }
    private void setFlap(int id, int value) {
        if (value < SERVO_PWM_MIN || value > SERVO_PWM_MAX) {
            System.err.println("setFlap out of bounds. Value="+value+" ID="+id);
        }
        SoftPwm.softPwmWrite(id,value);
    }
    private void terminateFlaps() {
        //run softPwmStop on every GPIO in flaps (unless it's -1, then it isn't yet made)
        for (int flap : flaps) {
            if (flap==-1) continue;
            SoftPwm.softPwmStop(flap);
        }
    }

    private String toHex(byte[] arr) {
        StringBuilder ret = new StringBuilder();
        for (byte b : arr) {
            ret.append(String.format("%02X",b & 0xFF)).append(":");
        }
        ret.append("\n");
        return ret.toString();
    }
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private GpioPinDigitalOutput getDigitalGPIO(int addr) {
        GpioPinDigitalOutput ret;
        if (pinMap.containsKey(addr)) {
            //get it from the map
            ret = pinMap.get(addr);
        } else {
            //generate it
            pinMap.put(addr,
                    ret = gpio.provisionDigitalOutputPin(
                            RaspiPin.getPinByAddress(addr),//pin number
                            ""+addr,//pin name
                            PinState.LOW//pin initial set
                    ));
            //if the program is closed, the pin will default back to LOW
            ret.setShutdownOptions(true, PinState.LOW);
        }
        return ret;
    }
}
