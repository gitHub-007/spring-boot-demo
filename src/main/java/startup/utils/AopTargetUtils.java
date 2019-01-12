package startup.utils;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * Aop工具类
 *
 * @Author Noah
 * @Date 2018-11-9
 * @Version V1.0
 */
public class AopTargetUtils {

    private AopTargetUtils() {
    }

    /**
     * 获取代理类的实际对象
     *
     * @param proxy       代理类
     * @param isInterface 是否获取接口（默认获取实现类）
     * @return
     * @throws Exception
     */
    public static Object getTarget(Object proxy, boolean isInterface) throws Exception {

        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;//不是代理对象  
        }
        if (!isInterface) {
            if (AopUtils.isJdkDynamicProxy(proxy)) {
                return getJdkDynamicProxyTargetObject(proxy);
            } else { //cglib
                return getCglibProxyTargetObject(proxy);
            }
        } else {
            if (AopUtils.isJdkDynamicProxy(proxy)) {
                return getJdkDynamicProxyTargetService(proxy);
            } else { //cglib
                return null;
            }
        }
    }

    private static AdvisedSupport getJdkProxyAdvisedSupport(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        return (AdvisedSupport) advised.get(aopProxy);
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);

        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();

        return target;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        return getJdkProxyAdvisedSupport(proxy).getTargetSource().getTarget();
    }

    private static Class<?> getJdkDynamicProxyTargetService(Object proxy) throws Exception {
        Class<?>[] interfaces = getJdkProxyAdvisedSupport(proxy).getProxiedInterfaces();
        return interfaces[0];
    }

}  