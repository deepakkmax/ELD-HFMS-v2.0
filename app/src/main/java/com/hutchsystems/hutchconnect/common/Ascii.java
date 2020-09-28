package com.hutchsystems.hutchconnect.common;

public class Ascii {

    public Ascii() {

    }

    // Modified By: Deepak Sharma
    // Modified Date: 16 January 2019
    // Purpose: added isletterordigit check it was not working fine
    public static int getAscii(char ch) {
        int code = 0;
        if (Character.isLetterOrDigit(ch)) {
            code = (int) ch - 48;
        }

       /* int ascii = (int) ch;
        if ((ascii >= 49 && ascii <= 57) || (ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122))
            code = ascii - 48;
        else
            code = 0;*/

        return code;
    }

    public static int getSum(String str) {
        if (str.equals(""))
            return 0;
        String temp = replaceCarriageReturn(str);
        int sum = 0;
        for (int i = 0; i < temp.length(); i++) {
            int code = getAscii(temp.charAt(i));
            sum += code;
        }
        return sum;
    }

    public static String replaceCarriageReturn(String str) {
        String result = str.replace("\r", ";");
        result = result.replace(",", ";");
        return result;
    }

    public static String calculateCheckSum(int number) {
        int checksum = number & 0xFF; //get lower 8-bit byte
        int shiftNum = checksum << 3;
        int xorNum = shiftNum ^ 195;
        checksum = xorNum & 0xFF;
        return Integer.toHexString(checksum);
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte line data check value
    public static String getLineDataCheckValue(String data) {
        int sum = getSum(data); // calculate line check sum
        int checksum = sum & 0xFF; // get lower 8-bit byte
        checksum = rotateLeft((byte) checksum, 3);// (checksum << 3); // rotate three consecutive left
        checksum = checksum ^ 150; // xor with number 150 as per document
        checksum = checksum & 0xFF; // get lower 8-bit byte
        String lineDataCheckValue = Integer.toHexString(checksum);
        lineDataCheckValue = String.format("%2s", lineDataCheckValue).replace(' ', '0'); // padding 0 if length less than 2
        return lineDataCheckValue.toUpperCase();
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte event data check value
    public static String getEventDataCheckValue(String data) {
        int sum = getSum(data); // calculate event check sumlineDataCheckValue
        int checksum = sum & 0xFF; // get lower 8-bit byte
        checksum = rotateLeft((byte) checksum, 3);//  // rotate three consecutive left
        checksum = checksum ^ 195; // xor with number 195 as per document
        checksum = checksum & 0xFF; // get lower 8-bit byte
        String eventDataCheckValue = Integer.toHexString(checksum);
        eventDataCheckValue = String.format("%2s", eventDataCheckValue).replace(' ', '0');// padding 0 if length less than 2

        return eventDataCheckValue.toUpperCase();
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte file data check value
    public static String getFileDataCheckValue(String data) {
        int sum = Integer.parseInt(data, 16);// calculate file check sum
        int checksum = sum & 0xFFFF; // get 2 lower 8-bit byte
        String binary = String.format("%16s", Integer.toBinaryString(sum)).replace(' ', '0');
        int part1 = rotateLeft((byte) Integer.parseInt(binary.substring(0, 8), 2), 3);
        String s1 = String.format("%8s", Integer.toBinaryString(part1 & 0xFF)).replace(' ', '0');
        int part2 = rotateLeft((byte) Integer.parseInt(binary.substring(8, 16), 2), 3);
        String s2 = String.format("%8s", Integer.toBinaryString(part2 & 0xFF)).replace(' ', '0');

        checksum = Integer.parseInt((s1 + s2), 2);
        // checksum = rotateLeft((byte) checksum, 3);//  // rotate three consecutive left
        checksum = checksum ^ 38556; // xor with number 38556 as per document
        checksum = checksum & 0xFFFF; // get 2 lower 8-bit byte
        String fileDataCheckValue = Integer.toHexString(checksum);
        fileDataCheckValue = String.format("%4s", fileDataCheckValue).replace(' ', '0');// padding 0 if length less than 4
        return fileDataCheckValue.toUpperCase();
    }

    public static String AddHex(String hex1, String hex2) {
        String sumHex = "";
        try {
            int sum = Integer.parseInt(hex1, 16) + Integer.parseInt(hex2, 16);
            sumHex = Integer.toHexString(sum);
        } catch (Exception exe) {
        }
        return sumHex;
    }

    public static byte rotateRight(byte bits, int shift) {

        return (byte) (((bits & 0xff) >>> shift) | ((bits & 0xff) << (8 - shift)));
    }

    public static byte rotateLeft(byte bits, int shift) {
        return (byte) (((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 27 feb 2018
    // Purpose: calcualte file data check value
    public static String getCheckValueForFile(String data) {
        int sum = Integer.parseInt(data, 16);// calculate file check sum
        int checksum = sum & 0xFFFF; // get 2 lower 8-bit byte
        checksum = circularLeftShift( checksum, 3);//  // rotate three consecutive left
        checksum = checksum ^ 38556; // xor with number 38556 as per document
        checksum = checksum & 0xFFFF; // get 2 lower 8-bit byte
        String fileDataCheckValue = Integer.toHexString(checksum);
        fileDataCheckValue = String.format("%4s", fileDataCheckValue).replace(' ', '0');// padding 0 if length less than 4
        return fileDataCheckValue.toUpperCase();
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 27 feb 2018
    // Purpose: circular left shift on each of 8 bit bytes
    public static int circularLeftShift(int bits ,int shift)
    {
        // Convert 16 bit in to two  8 bit
        int highValue = ((bits >> 8) & 0xff);
        int lowValue  = bits & 0xff;

        // rotate each 8 bit
        int first8bitValue = rotateLeft(highValue,shift);
        int second8bitValue = rotateLeft(lowValue,shift);

        //merge two 8 bit value
        return  (((first8bitValue & 0xff) << 8) | (second8bitValue & 0xff));


    }
    // Created By: Pallavi Wattamwar
    // Created Date: 27 feb 2018
    // Purpose: For Left shift
    public static int rotateLeft(int bits, int shift) {
        return  ((((bits ) << shift))  | ( ((bits)  >>> (8 - shift))));
    }

}

