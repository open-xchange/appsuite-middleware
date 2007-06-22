
package com.openexchange.admin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

public class UtilExceptionFactory extends AbstractOXExceptionFactory{
    
    public UtilExceptionFactory(Class clazz) {
        super(clazz);
    }
    
    private static final int CLASS = Classes.COM_OPENEXCHANGE_ADMIN_GROUPEXCEPTIONFACTORY;
    
    @Override
    protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
        if(component != Component.ADMIN_UTIL) {
            throw new IllegalArgumentException("This factory can only build exceptions for the group");
        }
        return new UtilException(category,number,message,cause,(Object[])msgArgs);
    }
    
    @Override
    protected int getClassId() {
        return CLASS;
    }
    
    public UtilException create(int id, Object...msgParams) {
        return (UtilException) createException(id,(Object[]) msgParams);
    }
    
    public UtilException create(int id, Throwable cause, Object...msgParams) {
        return (UtilException) createException(id,cause, (Object[]) msgParams);
    }
    
}
