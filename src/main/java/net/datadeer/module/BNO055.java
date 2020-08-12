package net.datadeer.module;

import com.pi4j.io.serial.*;
import net.datadeer.common.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

//reads sensor and abstracts output
public class BNO055 extends Module implements Observable {

    //todo allow the net.datadeer.plane to still fly manually if it loses connection to the sensor
    //main USE THIS TO VERIFY SENSOR OUTPUT
    boolean sensorWorking = false;

    Heart heart = new Heart(this::beat);

    public String getName() {
        return "BNO055";
    }

    public void onEnable() {
        heart.start();
    }

    public void onDisable() {
        heart.stop();
        close();
    }

    void beat() {
        if (readUpdatedSensorData()) {
            update();
        }
    }

    //todo enum would be hard to return as
    /*enum SensorOutput {
        acceleration(1f/100,    3),
        magnutide   (1f/16,     3),
        gyroscope   (1f/900,    3),
        euler       (1f/16,     3),
        quaternion  (1f/(1 << 14), 4);
        //todo rest of enum
        final float SCALE;
        final int SIZE;
        SensorOutput(float SCALE,   int SIZE) {
            this.SCALE = SCALE;
            this.SIZE = SIZE;
        }
    }*/

    // <editor-fold defaultstate="collapsed" desc=" Useful Variables and getters ">
    private Vector acceleration;
    Vector getAcceleration() {return acceleration;}
    private final float SCALE_ACCELERATION = 1f/100;
    private final int SIZE_ACCELERATION = 3;

    private Vector magnitude;
    Vector getMagnitude() {return magnitude;}
    private final float SCALE_MAGNITUDE = 1f/16;
    private final int SIZE_MAGNITUDE = 3;

    private Vector gyroscope;
    Vector getGyroscope() {return gyroscope;}
    private final float SCALE_GYROSCOPE = 1f/900;
    private final int SIZE_GYROSCOPE = 3;

    private Vector euler;
    Vector getEuler() {return euler;}
    private final float SCALE_EULER = 1f/16;
    private final int SIZE_EULER = 3;

    private Vector quaternion;
    Vector getQuaternion() {return quaternion;}
    private final float SCALE_QUATERNION = 1f/ (1 << 14);
    private final int SIZE_QUATERNION = 4;

    private Vector linearAcceleration;
    Vector getLinearAcceleration() {return linearAcceleration;}
    private final float SCALE_LINEAR_ACCELERATION = 1f/100;
    private final int SIZE_LINEAR_ACCELERATION = 3;

    private Vector gravity;
    Vector getGravity() {return gravity;}
    private final float SCALE_GRAVITY = 1f/100;
    private final int SIZE_GRAVITY = 3;

    private byte temperature;
    byte getTemperature() {return temperature;}

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Sensor Address Constants ">

    // I2C addresses;
    private final byte BNO055_ADDRESS_A                     = (byte)(0x28 & 0xFF );
    private final byte BNO055_ADDRESS_B                     = (byte)(0x29 & 0xFF );
    private final byte BNO055_ID                            = (byte)(0xA0 & 0xFF );
    // Page id register definition & 0xFF );
    private final byte BNO055_PAGE_ID_ADDR                  = (byte)(0X07 & 0xFF );
    // PAGE0 REGISTER DEFINITION START & 0xFF );
    private final byte BNO055_CHIP_ID_ADDR                  = (byte)(0x00 & 0xFF );
    private final byte BNO055_ACCEL_REV_ID_ADDR             = (byte)(0x01 & 0xFF );
    private final byte BNO055_MAG_REV_ID_ADDR               = (byte)(0x02 & 0xFF );
    private final byte BNO055_GYRO_REV_ID_ADDR              = (byte)(0x03 & 0xFF );
    private final byte BNO055_SW_REV_ID_LSB_ADDR            = (byte)(0x04 & 0xFF );
    private final byte BNO055_SW_REV_ID_MSB_ADDR            = (byte)(0x05 & 0xFF );
    private final byte BNO055_BL_REV_ID_ADDR                = (byte)(0X06 & 0xFF );
    // Accel data register & 0xFF );
    private final byte BNO055_ACCEL_DATA_X_LSB_ADDR         = (byte)(0X08 & 0xFF );
    private final byte BNO055_ACCEL_DATA_X_MSB_ADDR         = (byte)(0X09 & 0xFF );
    private final byte BNO055_ACCEL_DATA_Y_LSB_ADDR         = (byte)(0X0A & 0xFF );
    private final byte BNO055_ACCEL_DATA_Y_MSB_ADDR         = (byte)(0X0B & 0xFF );
    private final byte BNO055_ACCEL_DATA_Z_LSB_ADDR         = (byte)(0X0C & 0xFF );
    private final byte BNO055_ACCEL_DATA_Z_MSB_ADDR         = (byte)(0X0D & 0xFF );
    // Mag data register & 0xFF );
    private final byte BNO055_MAG_DATA_X_LSB_ADDR           = (byte)(0X0E & 0xFF );
    private final byte BNO055_MAG_DATA_X_MSB_ADDR           = (byte)(0X0F & 0xFF );
    private final byte BNO055_MAG_DATA_Y_LSB_ADDR           = (byte)(0X10 & 0xFF );
    private final byte BNO055_MAG_DATA_Y_MSB_ADDR           = (byte)(0X11 & 0xFF );
    private final byte BNO055_MAG_DATA_Z_LSB_ADDR           = (byte)(0X12 & 0xFF );
    private final byte BNO055_MAG_DATA_Z_MSB_ADDR           = (byte)(0X13 & 0xFF );
    // Gyro data registers & 0xFF );
    private final byte BNO055_GYRO_DATA_X_LSB_ADDR          = (byte)(0X14 & 0xFF );
    private final byte BNO055_GYRO_DATA_X_MSB_ADDR          = (byte)(0X15 & 0xFF );
    private final byte BNO055_GYRO_DATA_Y_LSB_ADDR          = (byte)(0X16 & 0xFF );
    private final byte BNO055_GYRO_DATA_Y_MSB_ADDR          = (byte)(0X17 & 0xFF );
    private final byte BNO055_GYRO_DATA_Z_LSB_ADDR          = (byte)(0X18 & 0xFF );
    private final byte BNO055_GYRO_DATA_Z_MSB_ADDR          = (byte)(0X19 & 0xFF );
    // Euler data registers & 0xFF );
    private final byte BNO055_EULER_H_LSB_ADDR              = (byte)(0X1A & 0xFF );
    private final byte BNO055_EULER_H_MSB_ADDR              = (byte)(0X1B & 0xFF );
    private final byte BNO055_EULER_R_LSB_ADDR              = (byte)(0X1C & 0xFF );
    private final byte BNO055_EULER_R_MSB_ADDR              = (byte)(0X1D & 0xFF );
    private final byte BNO055_EULER_P_LSB_ADDR              = (byte)(0X1E & 0xFF );
    private final byte BNO055_EULER_P_MSB_ADDR              = (byte)(0X1F & 0xFF );
    // Quaternion data registers & 0xFF );
    private final byte BNO055_QUATERNION_DATA_W_LSB_ADDR    = (byte)(0X20 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_W_MSB_ADDR    = (byte)(0X21 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_X_LSB_ADDR    = (byte)(0X22 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_X_MSB_ADDR    = (byte)(0X23 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_Y_LSB_ADDR    = (byte)(0X24 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_Y_MSB_ADDR    = (byte)(0X25 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_Z_LSB_ADDR    = (byte)(0X26 & 0xFF );
    private final byte BNO055_QUATERNION_DATA_Z_MSB_ADDR    = (byte)(0X27 & 0xFF );
    // Linear acceleration data registers & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR  = (byte)(0X28 & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_X_MSB_ADDR  = (byte)(0X29 & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_Y_LSB_ADDR  = (byte)(0X2A & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_Y_MSB_ADDR  = (byte)(0X2B & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_Z_LSB_ADDR  = (byte)(0X2C & 0xFF );
    private final byte BNO055_LINEAR_ACCEL_DATA_Z_MSB_ADDR  = (byte)(0X2D & 0xFF );
    // Gravity data registers & 0xFF );
    private final byte BNO055_GRAVITY_DATA_X_LSB_ADDR       = (byte)(0X2E & 0xFF );
    private final byte BNO055_GRAVITY_DATA_X_MSB_ADDR       = (byte)(0X2F & 0xFF );
    private final byte BNO055_GRAVITY_DATA_Y_LSB_ADDR       = (byte)(0X30 & 0xFF );
    private final byte BNO055_GRAVITY_DATA_Y_MSB_ADDR       = (byte)(0X31 & 0xFF );
    private final byte BNO055_GRAVITY_DATA_Z_LSB_ADDR       = (byte)(0X32 & 0xFF );
    private final byte BNO055_GRAVITY_DATA_Z_MSB_ADDR       = (byte)(0X33 & 0xFF );
    // Temperature data register & 0xFF );
    private final byte BNO055_TEMP_ADDR                     = (byte)(0X34 & 0xFF );
    // Status registers & 0xFF );
    private final byte BNO055_CALIB_STAT_ADDR               = (byte)(0X35 & 0xFF );
    private final byte BNO055_SELFTEST_RESULT_ADDR          = (byte)(0X36 & 0xFF );
    private final byte BNO055_INTR_STAT_ADDR                = (byte)(0X37 & 0xFF );
    private final byte BNO055_SYS_CLK_STAT_ADDR             = (byte)(0X38 & 0xFF );
    private final byte BNO055_SYS_STAT_ADDR                 = (byte)(0X39 & 0xFF );
    private final byte BNO055_SYS_ERR_ADDR                  = (byte)(0X3A & 0xFF );
    // Unit selection register & 0xFF );
    private final byte BNO055_UNIT_SEL_ADDR                 = (byte)(0X3B & 0xFF );
    private final byte BNO055_DATA_SELECT_ADDR              = (byte)(0X3C & 0xFF );
    // Mode registers & 0xFF );
    private final byte BNO055_OPR_MODE_ADDR                 = (byte)(0X3D & 0xFF );
    private final byte BNO055_PWR_MODE_ADDR                 = (byte)(0X3E & 0xFF );
    private final byte BNO055_SYS_TRIGGER_ADDR              = (byte)(0X3F & 0xFF );
    private final byte BNO055_TEMP_SOURCE_ADDR              = (byte)(0X40 & 0xFF );
    // Axis remap registers & 0xFF );
    private final byte BNO055_AXIS_MAP_CONFIG_ADDR          = (byte)(0X41 & 0xFF );
    private final byte BNO055_AXIS_MAP_SIGN_ADDR            = (byte)(0X42 & 0xFF );
    // Axis remap values & 0xFF );
    private final byte AXIS_REMAP_X                         = (byte)(0x00 & 0xFF );
    private final byte AXIS_REMAP_Y                         = (byte)(0x01 & 0xFF );
    private final byte AXIS_REMAP_Z                         = (byte)(0x02 & 0xFF );
    private final byte AXIS_REMAP_POSITIVE                  = (byte)(0x00 & 0xFF );
    private final byte AXIS_REMAP_NEGATIVE                  = (byte)(0x01 & 0xFF );
    // SIC registers & 0xFF );
    private final byte BNO055_SIC_MATRIX_0_LSB_ADDR         = (byte)(0X43 & 0xFF );
    private final byte BNO055_SIC_MATRIX_0_MSB_ADDR         = (byte)(0X44 & 0xFF );
    private final byte BNO055_SIC_MATRIX_1_LSB_ADDR         = (byte)(0X45 & 0xFF );
    private final byte BNO055_SIC_MATRIX_1_MSB_ADDR         = (byte)(0X46 & 0xFF );
    private final byte BNO055_SIC_MATRIX_2_LSB_ADDR         = (byte)(0X47 & 0xFF );
    private final byte BNO055_SIC_MATRIX_2_MSB_ADDR         = (byte)(0X48 & 0xFF );
    private final byte BNO055_SIC_MATRIX_3_LSB_ADDR         = (byte)(0X49 & 0xFF );
    private final byte BNO055_SIC_MATRIX_3_MSB_ADDR         = (byte)(0X4A & 0xFF );
    private final byte BNO055_SIC_MATRIX_4_LSB_ADDR         = (byte)(0X4B & 0xFF );
    private final byte BNO055_SIC_MATRIX_4_MSB_ADDR         = (byte)(0X4C & 0xFF );
    private final byte BNO055_SIC_MATRIX_5_LSB_ADDR         = (byte)(0X4D & 0xFF );
    private final byte BNO055_SIC_MATRIX_5_MSB_ADDR         = (byte)(0X4E & 0xFF );
    private final byte BNO055_SIC_MATRIX_6_LSB_ADDR         = (byte)(0X4F & 0xFF );
    private final byte BNO055_SIC_MATRIX_6_MSB_ADDR         = (byte)(0X50 & 0xFF );
    private final byte BNO055_SIC_MATRIX_7_LSB_ADDR         = (byte)(0X51 & 0xFF );
    private final byte BNO055_SIC_MATRIX_7_MSB_ADDR         = (byte)(0X52 & 0xFF );
    private final byte BNO055_SIC_MATRIX_8_LSB_ADDR         = (byte)(0X53 & 0xFF );
    private final byte BNO055_SIC_MATRIX_8_MSB_ADDR         = (byte)(0X54 & 0xFF );
    // Accelerometer Offset registers & 0xFF );
    private final byte ACCEL_OFFSET_X_LSB_ADDR              = (byte)(0X55 & 0xFF );
    private final byte ACCEL_OFFSET_X_MSB_ADDR              = (byte)(0X56 & 0xFF );
    private final byte ACCEL_OFFSET_Y_LSB_ADDR              = (byte)(0X57 & 0xFF );
    private final byte ACCEL_OFFSET_Y_MSB_ADDR              = (byte)(0X58 & 0xFF );
    private final byte ACCEL_OFFSET_Z_LSB_ADDR              = (byte)(0X59 & 0xFF );
    private final byte ACCEL_OFFSET_Z_MSB_ADDR              = (byte)(0X5A & 0xFF );
    // Magnetometer Offset registers & 0xFF );
    private final byte MAG_OFFSET_X_LSB_ADDR                = (byte)(0X5B & 0xFF );
    private final byte MAG_OFFSET_X_MSB_ADDR                = (byte)(0X5C & 0xFF );
    private final byte MAG_OFFSET_Y_LSB_ADDR                = (byte)(0X5D & 0xFF );
    private final byte MAG_OFFSET_Y_MSB_ADDR                = (byte)(0X5E & 0xFF );
    private final byte MAG_OFFSET_Z_LSB_ADDR                = (byte)(0X5F & 0xFF );
    private final byte MAG_OFFSET_Z_MSB_ADDR                = (byte)(0X60 & 0xFF );
    // Gyroscope Offset register s & 0xFF );
    private final byte GYRO_OFFSET_X_LSB_ADDR               = (byte)(0X61 & 0xFF );
    private final byte GYRO_OFFSET_X_MSB_ADDR               = (byte)(0X62 & 0xFF );
    private final byte GYRO_OFFSET_Y_LSB_ADDR               = (byte)(0X63 & 0xFF );
    private final byte GYRO_OFFSET_Y_MSB_ADDR               = (byte)(0X64 & 0xFF );
    private final byte GYRO_OFFSET_Z_LSB_ADDR               = (byte)(0X65 & 0xFF );
    private final byte GYRO_OFFSET_Z_MSB_ADDR               = (byte)(0X66 & 0xFF );
    // Radius registers & 0xFF );
    private final byte ACCEL_RADIUS_LSB_ADDR                = (byte)(0X67 & 0xFF );
    private final byte ACCEL_RADIUS_MSB_ADDR                = (byte)(0X68 & 0xFF );
    private final byte MAG_RADIUS_LSB_ADDR                  = (byte)(0X69 & 0xFF );
    private final byte MAG_RADIUS_MSB_ADDR                  = (byte)(0X6A & 0xFF );
    // Power modes & 0xFF );
    private final byte POWER_MODE_NORMAL                    = (byte)(0X00 & 0xFF );
    private final byte POWER_MODE_LOWPOWER                  = (byte)(0X01 & 0xFF );
    private final byte POWER_MODE_SUSPEND                   = (byte)(0X02 & 0xFF );
    // Operation mode settings & 0xFF );
    private final byte OPERATION_MODE_CONFIG                = (byte)(0X00 & 0xFF );
    private final byte OPERATION_MODE_ACCONLY               = (byte)(0X01 & 0xFF );
    private final byte OPERATION_MODE_MAGONLY               = (byte)(0X02 & 0xFF );
    private final byte OPERATION_MODE_GYRONLY               = (byte)(0X03 & 0xFF );
    private final byte OPERATION_MODE_ACCMAG                = (byte)(0X04 & 0xFF );
    private final byte OPERATION_MODE_ACCGYRO               = (byte)(0X05 & 0xFF );
    private final byte OPERATION_MODE_MAGGYRO               = (byte)(0X06 & 0xFF );
    private final byte OPERATION_MODE_AMG                   = (byte)(0X07 & 0xFF );
    private final byte OPERATION_MODE_IMUPLUS               = (byte)(0X08 & 0xFF );
    private final byte OPERATION_MODE_COMPASS               = (byte)(0X09 & 0xFF );
    private final byte OPERATION_MODE_M4G                   = (byte)(0X0A & 0xFF );
    private final byte OPERATION_MODE_NDOF_FMC_OFF          = (byte)(0X0B & 0xFF );
    private final byte OPERATION_MODE_NDOF                  = (byte)(0X0C & 0xFF );

    // </editor-fold>

    //main the serial connection
    private Serial serial;

    private void initialize() {
        //main init serial connection
        SerialConfig config = new SerialConfig();
        serial = SerialFactory.createInstance();
        config.device(RaspberryPiSerial.S0_COM_PORT)
                .baud(Baud._115200)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE);
        boolean hasOpenedSerial = false;
        do {
            try {
                serial.open(config);
                hasOpenedSerial = true;
            } catch (IOException e) {
                System.err.println("Can't open serial connection");
                e.printStackTrace();
            }
        } while (!hasOpenedSerial);
        System.out.println("Sensor serial opened");
        boolean hasStartedSensor = false;
        do {
            try {
                //main put it in config mode (just to be sure...)
                System.out.println("\tGoing to config mode");
                writeSensorByte(BNO055_OPR_MODE_ADDR, OPERATION_MODE_CONFIG);
                System.out.println("\tDone Going to config mode");

                //read current mode (should be config)
                System.out.print("\tState: " + toHex(readSensorRegisterByte(BNO055_OPR_MODE_ADDR)));

                //print chip ID
                System.out.print("\tChip ID: " + toHex(readSensorRegisterByte(BNO055_CHIP_ID_ADDR)));

                //main go to page 0 (sensor config and output)
                System.out.println("\tGoing to page 0");
                writeSensorByte(BNO055_PAGE_ID_ADDR, (byte)0x00);

                //set "power model" to normal mode
                //gave errors or whatever
                //System.out.println("\tGoing to normal power mode");
                //writeSensorByte(serial, BNO055_PWR_MODE_ADDR, POWER_MODE_NORMAL);

                //main set SYS_TRIGGER_ADDR so it's not resetting or test-ing or whatever
                System.out.println("\tSetting SYS TRIGGER off");
                writeSensorByte(BNO055_SYS_TRIGGER_ADDR, (byte)0x00);

                //main "The default operation mode after power-on is CONFIGMODE". Put it in NDOF mode.
                System.out.println("\tGoing to NDOF mode");
                writeSensorByte(BNO055_OPR_MODE_ADDR, OPERATION_MODE_NDOF);
                System.out.println("\tDone Going to NDOF mode");
                hasStartedSensor = true;
            } catch (SensorException | IOException e) {
                System.err.println("Error while configuring sensor");
                e.printStackTrace();
            }
        } while (!hasStartedSensor);
        //main wait 50 millis (datasheet recommends 19 millis, but be patient)
        Common.sleep(50);
        System.out.println("sensor successfully set up");

        sensorWorking = true;
    }

    BNO055() {
        System.out.println("Attempting to start BNO055");
        initialize();
    }

    //observer code
    ArrayList<Observer> listeners = new ArrayList<>();
    @Override public void addListener(Observer o) {listeners.add(o);}
    @Override public void removeListener(Observer o) {listeners.remove(o);}
    @Override public void update() {

        if (!sensorWorking) return;

        for(Observer o : listeners) o.observe(this);
    }

    class SensorException extends Exception {
        boolean reasonIsOverloaded = false;
        SensorException(String s) {
            super(s);
        }
    }

    private void close() {
        try {
            serial.close();
        } catch (IOException e) {
            System.out.println("Failing to close serial communications");
            e.printStackTrace();
        }
    }

    //functions relating to serial communication
    private byte[] readSensorRegisterBytes(byte sensorRegister, byte unsignedCount) throws IOException, SensorException {
        byte[] command = {
                (byte)0xAA, // start byte
                (byte)0x01, // main read
                (byte)(sensorRegister & 0xFF), // register address
                (byte)(unsignedCount & 0xFF) // consecutive bytes requested
        };
        //System.out.print("Sending: " + toHex(command));

        //testing sleep to make EE:07 less likely
        //Plane.sleep(50);

        serial.write(command,0,command.length);
        byte[] response = serial.read(2);
        if ((response[0] & 0xFF) != 0xBB) {
            SensorException ex = new SensorException("Bad read Response (BB: ...) != ("+toHex(response)+")");
            if ((response[0] & 0xFF) == 0x07) ex.reasonIsOverloaded = true;
            throw ex;
        }
        int bytesToRead = response[1] & 0xFF;
        //System.out.println("Good Response (BB). Going to read "+bytesToRead+" bytes");
        return serial.read(bytesToRead);
    }
    private byte writeSensorByte(byte sensorRegister, byte writeData) throws IOException, SensorException {
        byte[] command = {
                (byte)0xAA, // start byte
                (byte)0x00, // main write
                sensorRegister, // register address
                (byte)0x01, // length
                writeData // consecutive bytes requested
        };
        //System.out.print("Writing: "+toHex(command));

        serial.write(command,0,command.length);
        byte[] response = serial.read(2);
        if ((response[0] & 0xFF) != 0xEE) {
            throw new SensorException("Bad write Response (EE: ...) != ("+toHex(response)+")");
        }
        if ((response[1] & 0xFF) != 0x01) {
            throw new SensorException("Bad write Status (EE:01) != ("+toHex(response)+")");
        }
        return response[1];
    }
    //functions relating to abstract sensor logic
    private byte readSensorRegisterByte(byte sensorRegister) throws IOException, SensorException {
        return readSensorRegisterBytes(sensorRegister, (byte)0x01)[0];
    }

    private Vector readVectorFromBytes(byte[] sensorBytes, int shortCount) throws SensorException {
        short[] out = new short[shortCount];
        for (int i = 0; i < shortCount; i++) {
            out[i] = (short)(((sensorBytes[i*2+1] & 0xFF) << 8) | (sensorBytes[i*2] & 0xFF));
        }
        if (shortCount == 3) {
            return new Vector(out[0],out[1],out[2]);
        } else if (shortCount == 4) {
            return new Vector(out[0],out[1],out[2], out[3]);
        }
        throw new SensorException("Read vector too long of length "+shortCount);
    }
    private Vector readVector(byte firstRegister, int shortCount) throws IOException, SensorException {
        byte[] sensorBytes = readSensorRegisterBytes(firstRegister, (byte)(shortCount*2 & 0xFF));
        return readVectorFromBytes(sensorBytes, shortCount);
    }

    /** reads all sensors in one UART swoop, then sets vars
     * Takes about 100ms to run
     * @return boolean true if successful retrieval*/
    private boolean readUpdatedSensorData() {
        if (!sensorWorking) {
            System.err.println("Someone tried updating sensor data, but sensor is not yet set up");
            return false;
        }
        try {
            //gets ALL sensor information
            byte[] sensorBytes = readSensorRegisterBytes(BNO055_ACCEL_DATA_X_LSB_ADDR, (byte)(45 & 0xFF));
            acceleration = readVectorFromBytes(         Arrays.copyOfRange(sensorBytes, 0, 6), SIZE_ACCELERATION).scale(SCALE_ACCELERATION);
            magnitude = readVectorFromBytes(            Arrays.copyOfRange(sensorBytes, 6, 12), SIZE_MAGNITUDE).scale(SCALE_MAGNITUDE);
            gyroscope = readVectorFromBytes(            Arrays.copyOfRange(sensorBytes, 12, 18), SIZE_GYROSCOPE).scale(SCALE_GYROSCOPE);
            euler = readVectorFromBytes(                Arrays.copyOfRange(sensorBytes, 18, 24), SIZE_EULER).scale(SCALE_EULER);
            quaternion = readVectorFromBytes(           Arrays.copyOfRange(sensorBytes, 24, 32), SIZE_QUATERNION).scale(SCALE_QUATERNION);
            linearAcceleration = readVectorFromBytes(   Arrays.copyOfRange(sensorBytes, 32, 38), SIZE_LINEAR_ACCELERATION).scale(SCALE_LINEAR_ACCELERATION);
            gravity = readVectorFromBytes(              Arrays.copyOfRange(sensorBytes, 38, 44), SIZE_GRAVITY).scale(SCALE_GRAVITY);
            temperature = sensorBytes[44];
        } catch (SensorException e) {
            if (e.reasonIsOverloaded) {
                System.out.println("Sensor Reading overloaded");
            } else {
                System.out.println("Reading all sensors failed");
                e.printStackTrace();
            }
            return false;
        } catch (IOException e) {
            System.out.println("Reading all sensors failed IOEXception");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String toHex(byte... arr) {
        StringBuilder ret = new StringBuilder();
        for (byte b : arr) {
            ret.append(String.format("%02X",b & 0xFF)).append(":");
        }
        return ret.toString();
    }
}