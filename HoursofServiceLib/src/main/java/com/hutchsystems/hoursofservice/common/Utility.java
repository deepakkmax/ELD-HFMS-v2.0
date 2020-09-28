package com.hutchsystems.hoursofservice.common;

import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 20 August 2018
    /// Purpose: get time from total seconds
    /// </summary>
    /// <param name="totalSeconds">total second</param>
    /// <returns>time in HH:mm:ss format</returns>
    public static String getTimeFromSeconds(double totalSeconds) {
        String hours = "" + Math.floor(totalSeconds / 3600);
        hours = hours.length() == 1 ? "0" + hours : hours;
        String minutes = "" + Math.floor((totalSeconds % 3600) / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        String seconds = "" + Math.floor(totalSeconds % 60);
        seconds = seconds.length() == 1 ? "0" + seconds : seconds;
        return hours + ":" + minutes + ":" + seconds;
    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: get violation title and description
    /// </summary>
    /// <param name="rule">section of the violation</param>
    /// <returns>return violation detail</returns>
    public static ViolationModel violationInfoGet(String rule) {
        String vTitle = "";
        String vDetail = "";
        int ruleId = 0;

        double thresholdLimit = 0d;
        Boolean canadaFg = true;
        switch (rule) {
            case "3C":
                vTitle = "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty";
                vDetail = "According to Canadian law the aggregate of the driving time immediately preceding and immediately following the resting time in the sleeper berth does not exceed 13 hours in total.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 5;
                break;
            case "6(2a)":
                vTitle = "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only drive 13 hours after taking 8 consecutive hours off-duty. You are required to take another 8 consecutive hours off before driving is permitted.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 5;
                break;
            case "6(2b)":
                vTitle = "Must not drive after 15 hours of on-duty time unless 8 consecutive hours of off-duty time is taken.";
                vDetail = "Canadian law permits you to only be on-duty for 15 hours after taking 8 consecutive hours off-duty. You will be required to take another 8 consecutive hours off before driving is permitted.";
                thresholdLimit = 15 * 60 * 60d;
                ruleId = 5;
                break;
            case "7(1a)":
                vTitle = "Must not drive beyond 4 hours without taking 10 consecutive minutes of off duty or non-driving time.";
                vDetail = "Canadian law permits you to only drive 4 hours after taking 10 consecutive minutes of off-duty or non-driving time. You are required to take another 10 consecutive minutes off before driving is permitted.";
                thresholdLimit = 4 * 60 * 60d;
                ruleId = 5;
                break;
            case "7(1b)":
                vTitle = "Must not drive beyond 6 hours without taking 30 consecutive minutes of off duty or non-driving time.";
                vDetail = "Canadian law permits you to only drive 6 hours after taking 30 consecutive minutes of off-duty or non-driving time. You are required to take another 30 consecutive minutes off before driving is permitted.";
                thresholdLimit = 6 * 60 * 60d;
                ruleId = 5;
                break;
            case "12A":
                vTitle = "Maximum 13 hours of driving in a day";
                vDetail = "Canadian law permits you to drive for a maximum of 13 hours in any 24 hours period based on when your 24 hours period starts. Most drivers prefer mid-night to mid-night as their 24 hours period.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 1;
                break;
            case "12B":
                vTitle = "No driving after 14 hours on-duty in a day";
                vDetail = "Canadian law permits you to be on-duty (on-duty and driving) for 14 hours in a 24 hours period based on when your 24 hours period starts. You cannot drive after 14 hours of on-duty in a day. Most drivers prefer a mid-night to mid-night period.";
                thresholdLimit = 14 * 60 * 60d;
                ruleId = 1;
                break;
            case "13A":
                vTitle = "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only drive 13 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 1;
                break;
            case "13B":
                vTitle = "Must not drive after 14 hours of on-duty time unless 8 consecutive hours of off-duty time is taken.";
                vDetail = "Canadian law permits you to only be on-duty for 14 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You will be required to take another 8 consecutive hours off before driving is permitted.";
                thresholdLimit = 14 * 60 * 60d;
                ruleId = 1;
                break;
            case "13C":
                vTitle = "Must not drive beyond 16 hours of elapsed time after last 8 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only have 16 hours of elapsed time after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.";
                thresholdLimit = 16 * 60 * 60d;
                ruleId = 1;
                break;
            case "14A":
                vTitle = "Must have minimum 10 hours off-duty in a day.";
                vDetail = "Canadian law requires you to have a minimum of 10 hours off-duty or sleeper-berth or any combination of off-duty and sleeper-berth time in a 24 hours period depending on when you consider your 24 hours to start. Most drivers prefer mid-night to mid-night as their 24 hours period. ";
                thresholdLimit = 10 * 60 * 60d;
                ruleId = 1;
                break;
            case "14C":
                vTitle = "Must have minimum 2 hours off-duty in a day that is not part of 8 consecutive required hours.";
                vDetail = "Canadian law requires minimum 2 hours off-duty or sleeper-berth or any combination of off-duty and sleeper-berth that is not part of the 8 consecutive hours. Off-duty time must be in blocks of not less than 30 minutes each to be considered towards minimum of 10 hours of required off-duty in a day.";
                thresholdLimit = 2 * 60 * 60d;
                ruleId = 1;
                break;
            case "16B":
                vTitle = "Must take 20 hours off-duty in 2 days when deferring off-duty time.";
                vDetail = "Canadian law requires minimum 20 hours off-duty in 2 days if you are deferring off-duty time. This is the total of Day 1 and Day 2. You cannot defer any part of the 8 consecutive hours of required off-duty time.";
                thresholdLimit = 20 * 60 * 60d;
                ruleId = 1;
                break;
            case "16D":
                vTitle = "Must not exceed 26 hours of drive time in 2 days.";
                vDetail = "Canadian law permits you to only have 26 hours of drive time in 2 days if you are deferring off-duty time. This is your Day 1 and Day 2 total.";
                thresholdLimit = 26 * 60 * 60d;
                ruleId = 1;
                break;
            case "25":
                vTitle = "Must not drive unless 24 consecutive off-duty hours are taken in the preceding 14 days.";
                vDetail = "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 14 days. Your 24 hours can be part of cycle reset if you wish.";
                thresholdLimit = 24 * 60 * 60d;
                ruleId = 1;
                break;
            case "26":
                vTitle = "Must not drive after 70 hours on-duty in 7 days.";
                vDetail = "Canadian law does not permit you to drive after 70 hours on-duty time in any period of 7 days. You can take 36 hours off-duty or sleeper-berth or any combination of to reset your cycle 1 or you can wait until the 8th day to gain hours back.";
                thresholdLimit = 70 * 60 * 60d;
                ruleId = 1;
                break;
            case "27A":
                vTitle = "Must not drive after 120 hours on-duty in 14 days.";
                vDetail = "Canadian law does not permit you to drive after 120 hours on-duty time in any period of 14 days. You can take 72 hours off-duty or sleeper-berth or any combination thereof to reset your cycle 2 or you can wait until the 15th day to gain hours back.";
                thresholdLimit = 120 * 60 * 60d;
                ruleId = 2;
                break;
            case "27B":
                vTitle = "Must not drive after 70 hours on-duty unless 24 consecutive off-duty hours is taken.";
                vDetail = "Canadian law does not permit you to drive after 70 hours on-duty unless you have taken 24 consecutive hours off-duty or in sleeper-berth or any combination thereof.  Your 24 hours can be part of cycle reset if you wish.";
                thresholdLimit = 70 * 60 * 60d;
                ruleId = 2;
                break;
            case "29A":
                vTitle = "Must take minimum 36 consecutive hours off-duty to switch to cycle 2 from cycle 1.";
                vDetail = "Canadian law requires a minimum of 36 consecutive hours off-duty or sleeper-berth or any combination of off-duty and sleeper before switching to cycle 2 from cycle 1.";
                thresholdLimit = 36 * 60 * 60d;
                ruleId = 1;
                break;
            case "37.15(1)(a)(i)":
                vTitle = "Must not drive beyond 13 hours without taking 9 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only drive 13 hours after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 9 consecutive hours off before driving is permitted.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 6;
                break;
            case "37.15(1)(a)(ii)":
                vTitle = "Must not drive after 15 hours of on-duty time unless 9 consecutive hours of off-duty time is taken.";
                vDetail = "Canadian law permits you to only be on-duty for 15 hours after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You will be required to take another 9 consecutive hours off before driving is permitted.";
                thresholdLimit = 15 * 60 * 60d;
                ruleId = 6;
                break;
            case "37.15(1)(b)(i)":
                vTitle = "Must not drive beyond 15 hours of elapsed time after last 9 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only have 15 hours of elapsed time after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 9 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.";
                thresholdLimit = 15 * 60 * 60d;
                ruleId = 6;
                break;
            case "37.15(1)(b)(ii)":
                vTitle = "Must not drive unless 24 consecutive off-duty hours are taken in every period of 7 days.";
                vDetail = "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 7 days.";
                thresholdLimit = 24 * 60 * 60d;
                ruleId = 6;
                break;

            case "37.15(2)(a)":
                vTitle = "Must not drive after 65 hours of driving time in 7  consecutive days.";
                vDetail = "Canadian law does not permit you to drive after 65 hours of driving time in any period of 7 consecutive days. You can wait until the 8th day to gain hours back.";
                thresholdLimit = 65 * 60 * 60d;
                ruleId = 6;
                break;
            case "37.15(2)(b)":
                vTitle = "Must not drive after 80 hours of on-duty time in 7  consecutive days.";
                vDetail = "Canadian law does not permit you to drive after 80 hours of on-duty time in any period of 7 consecutive days. You can wait until the 8th day to gain hours back.";
                thresholdLimit = 80 * 60 * 60d;
                ruleId = 6;
                break;

            case "63(2)(a)":
                vTitle = "Must not drive unless at least 3 period of off-duty time each at least 24 hours long are taken in any period of 24 days.";
                vDetail = "Canadian law requires at least 3 period of consecutive off-duty hours  each at least 24 hours long or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken in any period of 24 days.";
                thresholdLimit = 24 * 60 * 60d;
                ruleId = 7;
                break;


            case "11 Hrs 395.3(a)(3)(i)":
                vTitle = "Must not drive beyond 11 hours without taking 10 consecutive hours off-duty";
                vDetail = "US law permits you to only drive 11 hours after taking 10 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted.";
                thresholdLimit = 11 * 60 * 60d;

                ruleId = 3;
                canadaFg = false;
                break;
            case "11 Hrs 395.3(a)(3)(i) ***EGREGIOUS***":
                vTitle = "Must not drive beyond 11 hours without taking 10 consecutive hours off-duty";
                vDetail = "US law permits you to only drive 11 hours after taking 10 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted.";
                thresholdLimit = 13 * 60 * 60d;
                ruleId = 3;
                canadaFg = false;
                break;
            case "Take 30 min Rest":
                vTitle = "Must not drive if more than 8 hours have passed since the end of last minimum of 30 minutes of off-duty or sleeper.";
                vDetail = "US law only permits you to drive for a maximum of 8 hours since the last 30 minutes of off-duty or sleeper-berth time. You must take a minimum of 30 consecutive minute off-duty or sleeper-berth or any combination off-duty and sleeper-berth.";
                thresholdLimit = 8 * 60 * 60d;
                ruleId = 3;
                canadaFg = false;
                break;
            case "14 Hrs 395.3(a)(2)":
                vTitle = "Must not drive after 14 hours following 10 consecutive hours off-duty";
                vDetail = "US law permits you to only have 14 hours of elapsed time after 10 consecutive hours off-duty or sleeper-berth or any combination off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.";
                thresholdLimit = 14 * 60 * 60d;
                ruleId = 3;
                canadaFg = false;
                break;
            case "60 Hrs 395.3(b)(1)":
                vTitle = "Must not drive after 60 hours on-duty within 7 days.";
                vDetail = "US law does not permit you to drive after 60 hours on-duty time in any 7 days period. You can take 34 hours off-duty or sleeper-berth or any such combination to reset or you can wait until the 9th day to gain the hours back.";
                thresholdLimit = 60 * 60 * 60d;
                ruleId = 4;
                canadaFg = false;
                break;
            case "70 Hrs 395.3(b)(2)":
                vTitle = "Must not drive after 70 hours on-duty within 8 days.";
                vDetail = "US law does not permit you to drive after 70 hours on-duty time in any 8 days period. You can take 34 hours off-duty or sleeper-berth or any such combination to reset or you can wait until the 9th day to gain the hours back.";
                thresholdLimit = 70 * 60 * 60d;
                ruleId = 3;
                canadaFg = false;
                break;
        }
        ViolationModel bean = new ViolationModel();
        bean.setRule(rule);
        bean.setTitle(vTitle);
        bean.setDescription(vDetail);
        bean.setThreshold(thresholdLimit);
        bean.setCanadaFg(canadaFg);
        bean.setRuleId(ruleId);
        return bean;
    }

    public String EncryptOrDecrypt(String input, String keyText) {
        char[] key = keyText.toCharArray(); //Your key in char array
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }

        return output.toString();
    }

    // Created By: Deepak Sharma
    // Created Date: 12 November 2018
    // purpose: convert to date only
    public static Date dateOnly(Date date) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateWithoutTime = sdf.parse(sdf.format(date));
            return dateWithoutTime;
        } catch (Exception exe) {
            return date;
        }
    }


}
