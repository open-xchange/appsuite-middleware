
package com.openexchange.admin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

public class GroupExceptionFactory extends AbstractOXExceptionFactory{
    
    public GroupExceptionFactory(Class clazz) {
        super(clazz);
    }
    
    private static final int CLASS = Classes.COM_OPENEXCHANGE_ADMIN_GROUPEXCEPTIONFACTORY;
    
    @Override
    protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
        if(component != Component.ADMIN_GROUP) {
            throw new IllegalArgumentException("This factory can only build exceptions for the group");
        }
        return new GroupException(category,number,message,cause,(Object[])msgArgs);
    }
    
    @Override
    protected int getClassId() {
        return CLASS;
    }
    
    public GroupException create(int id, Object...msgParams) {
        return (GroupException) createException(id,(Object[]) msgParams);
    }
    
    public GroupException create(int id, Throwable cause, Object...msgParams) {
        return (GroupException) createException(id,cause, (Object[]) msgParams);
    }
    
}
