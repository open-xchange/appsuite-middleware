
package com.openexchange.admin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

public class ContextExceptionFactory extends AbstractOXExceptionFactory{
    
    public ContextExceptionFactory(Class clazz) {
        super(clazz);
    }
    
    private static final int CLASS = Classes.COM_OPENEXCHANGE_ADMIN_CONTEXTEXCEPTIONFACTORY;
    
    @Override
    protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
        if(component != Component.ADMIN_CONTEXT) {
            throw new IllegalArgumentException("This factory can only build exceptions for context");
        }
        return new ContextException(category,number,message,cause,(Object[])msgArgs);
    }
    
    @Override
    protected int getClassId() {
        return CLASS;
    }
    
    public ContextException create(int id, Object...msgParams) {
        return (ContextException) createException(id,(Object[]) msgParams);
    }
    
    public ContextException create(int id, Throwable cause, Object...msgParams) {
        return (ContextException) createException(id,cause, (Object[]) msgParams);
    }
    
}
