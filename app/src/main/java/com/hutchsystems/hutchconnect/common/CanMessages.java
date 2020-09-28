package com.hutchsystems.hutchconnect.common;

public class CanMessages {
    String TAG = "CanMessages";

    public static String protocol = ""; // J1939,J1708,OBD2, if empty then it will get every records sends by can bus
    public static int odometerSource = -1;

    public static String deviceAddress;
    public static String deviceName;

    public static boolean HeartBeat = false;
    public static int mState;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 4;  // now connected to a remote device
    public static final int STATE_SET_UNIT_ID= 5;  // now connected to a remote device

    public static String Speed = "-1";

    public static String OdometerReading = "0", EngineHours = "0", RPM = "-1", VIN = "", CoolantTemperature = "-99",
            Voltage = "0", Boost = "0", TotalFuelConsumed = "0", TotalIdleFuelConsumed = "0", TotalIdleHours = "0",
            TotalAverage = "0", WasherFluidLevel = "-99", FuelLevel1 = "0", EngineCoolantLevel = "-99", EngineOilLevel = "-99", BrakeApplicationPressure = "0",
            BrakePrimaryPressure = "0", BrakeSecondaryPressure = "0";
    public static boolean CriticalWarningFg, J1939SupportFg, J1708SupportFg, OBD2SupportFg;

    public static long diagnosticEngineSynchronizationTime = System.currentTimeMillis();

    public CanMessages() {
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }


}
