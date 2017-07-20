package com.lancw.util;

/**
 * File:FormatUtil.java
 * Date:2016-4-13 11:46:20
 * Encoding:UTF-8
 * Author:lancw
 * Description:
 */
public class FormatUtil {

    /**
     * 格式化json显示输出
     *
     * @param jsonStr
     * @return
     */
    public static String formatJsonOutput(String jsonStr) {
        int level = 0;
        StringBuilder jsonForMatStr = new StringBuilder();
        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c).append("\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c).append("\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level) {
        StringBuilder levelStr = new StringBuilder();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("    ");
        }
        return levelStr.toString();
    }
}
