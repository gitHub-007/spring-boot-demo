package startup.custom_annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description TODO
 * @Author Noah
 * @Date 2018-12-18
 * @Version V1.0
 */
@CustomizeComponent
public class ScanClass1 {
    private static final Logger logger = LoggerFactory.getLogger(ScanClass1.class);
    public void print(){
        System.out.println(this.getClass().getName());
    }
}
