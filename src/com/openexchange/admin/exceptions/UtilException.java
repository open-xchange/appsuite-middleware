
package com.openexchange.admin.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;

public class UtilException extends OXException {
	/**
	 * For serialization
	 */
	private static final long serialVersionUID = -3913563452534032927L;

	public UtilException(Category category, int id, String message, Throwable cause, Object...msgParams){
		super(Component.ADMIN_UTIL, category, id, String.format(message, (Object[]) msgParams),cause);
	}

	public UtilException(Category category, String message, int id, Object...msgParams){
		this(category,id,message, null,(Object[])msgParams);
	}
}
