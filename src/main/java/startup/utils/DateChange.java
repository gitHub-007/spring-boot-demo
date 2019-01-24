package startup.utils;

public class DateChange {

    private static final String[] NUMBERS1 = {"〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] NUMBERS2 = {"十", "百", "千", "万"};

    /**
     * 将数字日期转化成中文日期
     * 
     * 2018年07月17日 -----> 二〇一八年七月十七日
     */
    public static String getChineseDate(String date) {
        String[] str = date.split("年|月|日");
        String year = str[0];
        String month = str[1];
        String day = str[2];
        for (int i = 0; i < 10; i++) {
            year = year.replaceAll(String.valueOf(i), NUMBERS1[i]);
        }
        month = toChinese(month);
        day = toChinese(day);
        String dateChinese = year + "年" + month + "月" + day + "日";
        return dateChinese;

    }

    /**
     * 将数字转为中文格式 12 --> 十二
     * 
     * @param number
     * @return
     */
    public static String toChinese(String number) {
        String result = "";
        int n = number.length();
        for (int i = 0; i < n; i++) {
            int num = number.charAt(i) - '0';
            if (i == 0 && num == 0) {
                result += "";
            } else if (n == 2 && i == 0 && num == 1) {
                result += NUMBERS2[0];
            } else if ((i != n - 1) && (num != 0)) {
                result += NUMBERS1[num] + NUMBERS2[n - 2 - i];
            } else {
                result += NUMBERS1[num];
            }
        }
        return result;
    }

    /**
     * 将中文日期格式转化为数字日期格式 二〇一七年十二月十一日 ----> 2017年12月11日
     */
    public static String getNumberDate(String date) {
        String[] str = date.split("年|月|日");
        String year = str[0];
        String month = str[1];
        String day = str[2];
        for (int i = 0; i < 10; i++) {
            year = year.replaceAll(NUMBERS1[i], String.valueOf(i));
        }
        month = toNumber(month);
        day = toNumber(day);
        String dateNumber = year + "年" + month + "月" + day + "日";
        return dateNumber;
    }

    /**
     * 将中文转化数字格式 十二 ---> 12
     * 
     * @param args
     */
    public static String toNumber(String chinese) {
        String result = "";
        int n = chinese.length();
        for (int i = 0; i < n; i++) {
            if (n % 2 == 0 && n != 2) {
                continue;
            }
            if (n == 2 && i == 0) {
                result += "1";
                continue;
            }
            if (n == 1) {
                result += "0";
            }
            String num = chinese.substring(i, i + 1);
            for (int a = 0; a < NUMBERS1.length; a++) {
                if (NUMBERS1[a].equals(num)) {
                    result += a;
                }
            }
        }
        return result;
    }
}
