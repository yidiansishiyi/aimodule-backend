package com.yidiansishiyi.aimodule.utils;

public class DataCleaningUtils {

    public static String extractJsonPart(String data) {
        int startIndex = data.indexOf("{");
        int endIndex = data.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1) {
            return data.substring(startIndex, endIndex + 1);
        }

        return null;
    }
}

