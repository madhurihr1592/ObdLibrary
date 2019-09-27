package com.obdlibrary.utils;

import android.util.Log;

import java.io.*;

public class AndroidUtils {

    public static String readJsonfromSdcard(String jsonPathSdcard) {
        try {
            File jsonPath = new File(jsonPathSdcard);
            if (jsonPath.exists()) {
                FileInputStream fIn = new FileInputStream(jsonPath);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(fIn));

                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += aDataRow + "\n";

                }
                Log.d("JSONFROMSDCARD", "Buffer::" + aBuffer);

                return aBuffer.toString();

            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }
}
