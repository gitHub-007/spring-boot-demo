package startup.utils.ChineseAddress;

import java.util.regex.Pattern;

public class Splitter {
    Pattern pattern;
    Pattern[] patterns;
    boolean flag = true;

    public Splitter(Pattern pattern) {
        this.pattern = pattern;
    }

    public Splitter(Pattern pattern, Pattern[] patterns) {
        this.pattern = pattern;
        this.patterns = patterns;
    }

    public Splitter(Pattern pattern, Pattern[] patterns, boolean flag) {
        this.pattern = pattern;
        this.flag = flag;
        this.patterns = patterns;
    }
}
