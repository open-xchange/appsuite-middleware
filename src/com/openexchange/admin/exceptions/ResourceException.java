
package com.openexchange.admin.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;

public class ResourceException extends OXException {
	/**
	 * For serialization
	 */
	private static final long serialVersionUID = -6605835970184821696L;

	public ResourceException(Category category, int id, String message, Throwable cause, Object...msgParams){
		super(Component.ADMIN_RESOURCE, category, id, String.format(message, (Object[]) msgParams),cause);
	}

	public ResourceException(Category category, String message, int id, Object...msgParams){
		this(category,id,message, null,(Object[])msgParams);
	}
}
