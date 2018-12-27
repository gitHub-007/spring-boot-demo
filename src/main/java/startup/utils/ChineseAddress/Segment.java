package startup.utils.ChineseAddress;

import java.util.regex.Pattern;

/**
 * 地址分段
 */
public class Segment {
    String value;
    Pattern pattern;

    public Segment(String value, Pattern pattern) {
        this.value = value;
        this.pattern = pattern;
    }
}
