
package com.openexchange.admin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

public class UserExceptionFactory extends AbstractOXExceptionFactory{

	public UserExceptionFactory(Class clazz) {
		super(clazz);
	}
	
	private static final int CLASS = Classes.COM_OPENEXCHANGE_ADMIN_USEREXCEPTIONFACTORY;
	
	@Override
	protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
		if(component != Component.ADMIN_USER) {
			throw new IllegalArgumentException("This factory can only build exceptions for the user");
		}
		return new UserException(category,number,message,cause,(Object[])msgArgs);
	}
	
	@Override
	protected int getClassId() {
		return CLASS;
	}
	
	public UserException create(int id, Object...msgParams) {
		return (UserException) createException(id,(Object[]) msgParams);
	}
	
	public UserException create(int id, Throwable cause, Object...msgParams) {
		return (UserException) createException(id,cause, (Object[]) msgParams);
	}
	
}
