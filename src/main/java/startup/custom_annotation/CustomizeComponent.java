package startup.custom_annotation;

import java.lang.annotation.*;

/**
 * @Description TODO
 * @Author Noah
 * @Date 2018-12-18
 * @Version V1.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomizeComponent {
}
