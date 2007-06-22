
package com.openexchange.admin.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;

public class UserException extends OXException {
	/**
	 * For serialization
	 */
	private static final long serialVersionUID = 4592879884193237878L;

	public UserException(Category category, int id, String message, Throwable cause, Object...msgParams){
		super(Component.ADMIN_USER, category, id, String.format(message, (Object[]) msgParams),cause);
	}

	public UserException(Category category, String message, int id, Object...msgParams){
		this(category,id,message, null,(Object[])msgParams);
	}
}
