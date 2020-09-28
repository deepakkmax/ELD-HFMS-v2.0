package com.hutchsystems.hutchconnect.common;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.db.AlertDB;
import com.hutchsystems.hutchconnect.db.LogDB;

/**
 * Created by Deepak on 12/1/2016.
 */

public class AlertMonitor {
    public static boolean SpeedVLFg;
    public static long SpeedVLDate;
    public static double MaxSpeed = 0d;
    public static double PostedSpeed = 120d;
    public static double PostedSpeedThreshold = 10d;

    public static boolean HOSVLFg;
    public static long HOSVLDate;

    public static boolean NoTripInspectionVL;

    Thread thAlertMonitor = null;
    public static boolean HighRPMVL;
    public static long HighRPMVLDate;
    public static double MaxRPM = 0;

    public static boolean IdlingVLFg;
    public static long IdlingVLDate;

    public static boolean CriticalWarningVLFg;
    public static long CriticalWarningVLDate;

    public static void EngineStartAlerts() {
        LowWasherFluidViolationGet();
        LowCoolantTemperatureViolationGet();
        LowEngineOilViolationGet();
        LowCoolantLevelViolationGet();
    }

    public static void FuelEconomyViolationGet() {
        double distanceTravelled = Double.parseDouble(CanMessages.OdometerReading) - Double.parseDouble(Utility.OdometerReadingStart);
        double fuelUsed = Double.parseDouble(CanMessages.TotalFuelConsumed) - Double.parseDouble(Utility.FuelUsedStart);
        if (fuelUsed > 0) {
            double average = distanceTravelled / fuelUsed;
            double threshold = 2.25;
            if (average < threshold) {

                int driverId = Utility.activeUserId;

                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }
                AlertDB.Save("FuelEconomyVL", Utility.context.getString(R.string.low_fuel_economy), Utility.getCurrentDateTime(), 15, 0, driverId, String.valueOf(threshold));
            }
        }
    }

    private static void LowWasherFluidViolationGet() {
        double WasherFluidLevel = Double.parseDouble(CanMessages.WasherFluidLevel);
        double threshold = 80d;
        if (WasherFluidLevel != -99 && WasherFluidLevel < threshold) {

            int driverId = Utility.activeUserId;

            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }
            AlertDB.Save("LowWasherFluidVL", Utility.context.getString(R.string.low_washer_fluid), Utility.getCurrentDateTime(), 5, 0, driverId, String.valueOf(threshold));
        }

    }

    private static void LowCoolantTemperatureViolationGet() {
        double CoolantTemperature = Double.parseDouble(CanMessages.CoolantTemperature);
        double threshold = 80d;
        if (CoolantTemperature != -99 && CoolantTemperature < threshold) {

            int driverId = Utility.activeUserId;

            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }
            AlertDB.Save("LowCoolantTemperatureVL", Utility.context.getString(R.string.failure_to_warm_up_engine), Utility.getCurrentDateTime(), 20, 0, driverId, String.valueOf(threshold));
        }

    }

    private static void LowEngineOilViolationGet() {
        double EngineOilLevel = Double.parseDouble(CanMessages.EngineOilLevel);
        double threshold = 80d;
        if (EngineOilLevel != -99 && EngineOilLevel < threshold) {

            int driverId = Utility.activeUserId;

            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }
            AlertDB.Save("LowEngineOilVL", Utility.context.getString(R.string.low_engine_oil), Utility.getCurrentDateTime(), 5, 0, driverId, String.valueOf(threshold));
        }

    }

    private static void LowCoolantLevelViolationGet() {
        double EngineCoolantLevel = Double.parseDouble(CanMessages.EngineCoolantLevel);
        double threshold = 80d;
        if (EngineCoolantLevel != -99 && EngineCoolantLevel < threshold) {

            int driverId = Utility.activeUserId;

            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }
            AlertDB.Save("LowCoolantLevelVL", Utility.context.getString(R.string.low_coolant_level), Utility.getCurrentDateTime(), 5, 0, driverId, String.valueOf(threshold));
        }

    }

    private void CriticalWarningViolationGet() {
        if (!CriticalWarningVLFg) {
            if (CanMessages.CriticalWarningFg) {
                if (Utility.motionFg) {
                    CriticalWarningVLFg = true;
                    CriticalWarningVLDate = System.currentTimeMillis();

                    AlertDB.Save("CriticalWarningVL", Utility.context.getString(R.string.driving_with_critical_warning), Utility.getCurrentDateTime(), 50, 0, Utility.activeUserId, "0");
                }
            }
        } else {
            if (!CanMessages.CriticalWarningFg) {
                CriticalWarningVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - CriticalWarningVLDate) / (1000 * 60));
                int score = 5;
                AlertDB.Update("CriticalWarningVL", duration, score);
            }
        }
    }

    private void IdlingViolationGet() {
        if (!IdlingVLFg) {
            if (GPSData.CurrentStatus == 2) {
                IdlingVLFg = true;
                IdlingVLDate = System.currentTimeMillis();
                int driverId = Utility.activeUserId;

                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                AlertDB.Save("IdlingVL", "Idling", Utility.getCurrentDateTime(), 5, 0, driverId, "0");
            }
        } else {
            if (GPSData.CurrentStatus != 2) {
                IdlingVLFg = false;

                int duration = (int) ((System.currentTimeMillis() - IdlingVLDate) / (1000 * 60)) + 10;
                int score = 5;
                AlertDB.Update("IdlingVL", duration, score);
            }
        }
    }

    public static void NoTripInspectionGet() {
      /*  if (!NoTripInspectionVL) {*/
        if (GPSData.TripInspectionCompletedFg == 0) {
                /*if (Utility.motionFg) {*/
            NoTripInspectionVL = true;
            String currentDate = Utility.getCurrentDate();
            boolean isDuplicate = AlertDB.getDuplicate(Utility.activeUserId, "NoTripInspectionVL", currentDate);
            if (!isDuplicate)
                AlertDB.Save("NoTripInspectionVL", Utility.context.getString(R.string.failure_to_conduct_trip_inspection), Utility.getCurrentDateTime(), 50, 0, Utility.activeUserId, "0");
              /*  }*/
        }

       /* }*/
    }

    private void HighRPMGet() {
        double RPM = Double.parseDouble(CanMessages.RPM);
        if (!HighRPMVL) {
            if (RPM > 1600d) {
                HighRPMVL = true;
                HighRPMVLDate = System.currentTimeMillis();

                Utility.saveAlerts("HighRPMVL", HighRPMVL, "HighRPMVLDate", HighRPMVLDate);
            }
        } else {
            double threshold = 1600d;
            if (RPM < threshold) {
                HighRPMVL = false;
                int duration = (int) ((System.currentTimeMillis() - HighRPMVLDate) / (1000 * 60));
                int score = 0;
                if (duration > 60) {
                    score += Math.ceil((duration - 60) / 60) * 6 + 8;
                } else if (duration >= 20 && duration <= 60) {
                    score += 8;
                } else if (duration > 5 && duration < 20) {
                    score += 2;
                }
                if (duration >= 5) {
                    if (MaxRPM > 2000) {
                        score += 20;
                    } else if (MaxRPM >= 1800 && MaxRPM <= 2000) {
                        score += 8;
                    } else {
                        score += 2;
                    }

                    int driverId = Utility.activeUserId;
                    if (driverId == 0) {
                        driverId = Utility.unIdentifiedDriverId;
                    }
                    AlertDB.Save("HighRPMVL", "High RPM", Utility.getCurrentDateTime(), score, duration, driverId, String.valueOf(threshold));
                }
                HighRPMVLDate = 0;
                Utility.saveAlerts("HighRPMVL", HighRPMVL, "HighRPMVLDate", HighRPMVLDate);
                // high raving 2000-2200,2200-2400 and >2400
                //  AlertDB.Update("HighRPMVL", duration, score);

            } else {
                if (RPM > MaxRPM) {
                    MaxRPM = RPM;
                }
            }
        }
    }

    private void HOSViolationGet() {
        if (!HOSVLFg) {
            if (GPSData.NoHOSViolationFgFg == 0) {
                HOSVLFg = true;
                HOSVLDate = System.currentTimeMillis();

                int driverId = Utility.activeUserId;
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }
                Utility.saveAlerts("HOSVLFg", HOSVLFg, "HOSVLDate", HOSVLDate);
                AlertDB.Save("HOSVL", "Hours Of Service", Utility.getCurrentDateTime(), 5, 0, driverId, "0");
            }
        } else {
            if (GPSData.NoHOSViolationFgFg == 1) {
                HOSVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - HOSVLDate) / (1000 * 60));
                int score = 5;
                if (duration > 30) {
                    score += Math.ceil((duration - 30) / 10) * 30 + 58;
                } else if (duration >= 10 && duration <= 30) {
                    score += 15;
                } else if (duration > 5 && duration < 10) {
                    score += 8;
                } else {
                    score += 5;
                }
                AlertDB.Update("HOSVL", duration, score);
                HOSVLDate = 0;
                Utility.saveAlerts("HOSVLFg", HOSVLFg, "HOSVLDate", HOSVLDate);
            }
        }
    }

    private void SpeedViolationGet() {
        double speed = Double.parseDouble(CanMessages.Speed);
        if (!SpeedVLFg) {
            if (speed > (PostedSpeed + PostedSpeedThreshold)) {
                SpeedVLFg = true;
                SpeedVLDate = System.currentTimeMillis();

                int driverId = Utility.activeUserId;
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                Utility.saveAlerts("SpeedVLFg", SpeedVLFg, "SpeedVLDate", SpeedVLDate);
                AlertDB.Save("SpeedVL", "Speed Violation", Utility.getCurrentDateTime(), 0, 0, driverId, String.valueOf((PostedSpeed + PostedSpeedThreshold)));

            }
        } else {
            if (speed <= (PostedSpeed + PostedSpeedThreshold)) {
                SpeedVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - SpeedVLDate) / (1000 * 60));
                int score = 0;
                if (duration > 10) {
                    score += ((duration / 10)) * 4 + 2;
                } else if (duration > 2) {
                    score += 2;
                }

                if (MaxSpeed > PostedSpeed + 40) {
                    score += 32;
                } else if (MaxSpeed > PostedSpeed + 30 && MaxSpeed <= PostedSpeed + 40) {
                    score += 14;
                } else if (MaxSpeed > PostedSpeed + 20 && MaxSpeed <= PostedSpeed + 30) {
                    score += 5;
                } else if (MaxSpeed >= PostedSpeed + 10 && MaxSpeed <= PostedSpeed + 20) {
                    score += 1;
                }
                AlertDB.Update("SpeedVL", duration, score);

                SpeedVLDate = 0;
                Utility.saveAlerts("SpeedVLFg", SpeedVLFg, "SpeedVLDate", SpeedVLDate);
            } else {
                if (speed > MaxSpeed) {
                    MaxSpeed = speed;
                }
            }
        }
    }

    // Deepak Sharma
    // 3 Aug 2016
    // send request to bluetooth device every 5 seconds
    public void startAlertMonitor() {
        SpeedVLFg = Utility.getvAlert("SpeedVLFg");
        if (SpeedVLFg) {
            SpeedVLDate = Utility.gettAlert("SpeedVLDate");
        }
        HighRPMVL = Utility.getvAlert("HighRPMVL");
        if (HighRPMVL) {
            HighRPMVLDate = Utility.gettAlert("HighRPMVLDate");
        }

        HOSVLFg = Utility.getvAlert("HOSVLFg");
        if (HOSVLFg) {
            HOSVLDate = Utility.gettAlert("HOSVLDate");
        }

        Utility.OdometerReadingStart = Utility.getPreferences("OdometerReadingStart", CanMessages.OdometerReading);
        Utility.FuelUsedStart = Utility.getPreferences("FuelUsedStart", CanMessages.TotalFuelConsumed);

        if (thAlertMonitor != null) {
            thAlertMonitor.interrupt();
            thAlertMonitor = null;
        }
        thAlertMonitor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.interrupted())
                        break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exe) {
                        break;
                    }
                    if (CanMessages.mState == CanMessages.STATE_CONNECTED) {
                        try {

                            CriticalWarningViolationGet();
                            IdlingViolationGet();
                            HighRPMGet();
                            HOSViolationGet();
                            // monitor speed violation
                            SpeedViolationGet();
                        } catch (Exception exe) {
                            LogFile.write(AlertMonitor.class.getName() + "::thAlertMonitor Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                            LogDB.writeLogs(AlertMonitor.class.getName(),"::thAlertMonitor Error:",exe.getMessage(),Utility.printStackTrace(exe));

                        }
                    }


                }
            }
        });
        thAlertMonitor.setName("thAlertMonitor");
        thAlertMonitor.start();
    }


    public void stopAlertMonitor() {
        if (thAlertMonitor != null) {
            thAlertMonitor.interrupt();
            thAlertMonitor = null;
        }
    }
}
