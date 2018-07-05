package liquibase.datatype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DataTypeInfo {
    String name();
    int minParameters();
    int maxParameters();
    String[] aliases() default {};

    String description() default "##default";
    int priority();

}
