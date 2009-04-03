package com.openexchange.fitnesse.documentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import java.util.Collections;

/**
 * 
 * {@link FieldFindingReflector}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class FieldFindingReflector {
    

    private String classname;
    private List<String> methods;
    private List<String> fields;
    private String prefix;

    public FieldFindingReflector(Class reflectedClass) {
        super();
        this.classname = reflectedClass.getCanonicalName();
        Method[] methods = reflectedClass.getMethods();
        Field[] fields = reflectedClass.getFields();

        this.methods = new LinkedList<String>();
        for(Method method : methods){
            this.methods.add( method.getName() );
        }
        
        this.fields = new LinkedList<String>();
        for(Field field: fields){
            this.fields.add( field.getName() );
        }
    }
    
    public String stringifyAccessorMethods(){
        Set<String> accessors = new HashSet<String>();
        for(String method: methods){
            if(method.startsWith("set") 
            || method.startsWith("add")
            || method.startsWith("get"))
                accessors.add(method.substring(3));
        }
        return join(accessors, "\n");
    }
    
    public String stringifyAccessorMethodsUnderscoreStyle(){
        Set<String> accessors = new HashSet<String>();
        for(String method: methods){
            if(method.startsWith("set") 
            || method.startsWith("add")
            || method.startsWith("get"))
                if(method.length() > 3)
                    accessors.add(transform_to_underscore_style( method.substring(3) ) );
        }
        List<String> accessors2 = new LinkedList<String>(accessors);
        Collections.sort(accessors2);
        if(prefix == null)
            return join(accessors2, "\n");
        return join(accessors2, prefix, "\n");
    }
    
    public String transform_to_underscore_style(String camelCaseString){
            return camelCaseString.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase();
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }
    
    public String toString(){
        return classname + ":\n\n" + stringifyAccessorMethodsUnderscoreStyle();
    }



    public static void main(String[] args) {
        for(Class clazz: new Class[]{CalendarDataObject.class, ContactObject.class, FolderObject.class, Task.class}){
            FieldFindingReflector myClass = new FieldFindingReflector(clazz);
            myClass.setPrefix(" * ");
            System.out.println(myClass);
        }
    }

    public static String join(Collection collection, String connector){
        StringBuilder stringBuilder = new StringBuilder();
        for(Object obj: collection){
            stringBuilder.append(obj.toString());
            stringBuilder.append(connector);
        }
        return stringBuilder.subSequence(0, stringBuilder.length() - connector.length()).toString();
    }
    
    public static String join(Collection collection, String prefix, String postfix){
        StringBuilder stringBuilder = new StringBuilder();
        for(Object obj: collection){
            stringBuilder.append(prefix);
            stringBuilder.append(obj.toString());
            stringBuilder.append(postfix);
        }
        return stringBuilder.toString();
    }

}
