
package com.openexchange.admin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

public class ResourceExceptionFactory extends AbstractOXExceptionFactory{
    
    public ResourceExceptionFactory(Class clazz) {
        super(clazz);
    }
    
    private static final int CLASS = Classes.COM_OPENEXCHANGE_ADMIN_GROUPEXCEPTIONFACTORY;
    
    @Override
    protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
        if(component != Component.ADMIN_RESOURCE) {
            throw new IllegalArgumentException("This factory can only build exceptions for the group");
        }
        return new ResourceException(category,number,message,cause,(Object[])msgArgs);
    }
    
    @Override
    protected int getClassId() {
        return CLASS;
    }
    
    public ResourceException create(int id, Object...msgParams) {
        return (ResourceException) createException(id,(Object[]) msgParams);
    }
    
    public ResourceException create(int id, Throwable cause, Object...msgParams) {
        return (ResourceException) createException(id,cause, (Object[]) msgParams);
    }
    
}
