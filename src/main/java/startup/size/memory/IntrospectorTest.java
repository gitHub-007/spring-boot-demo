package startup.size.memory;

import startup.utils.Solution;

public class IntrospectorTest {
    private static class ObjectC {  
        ObjectD[] array = new ObjectD[2];  
    }  

    private static class ObjectD {  
        int value;  
    }  
    
    public static void main(String[] args) throws IllegalAccessException {
        final ClassIntrospector ci = new ClassIntrospector();  
        ObjectInfo res = ci.introspect(new ObjectC());  
        System.out.println(String.format("%d(byte)", res.getDeepSize()) );
         res = ci.introspect(new ClassIntrospector());
        System.out.println(String.format("%d(byte)", res.getDeepSize()) );
    }
}