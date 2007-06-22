
package com.openexchange.admin.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;

/**
 * 
 * @author cutmasta
 * $Version$
 */
public class GroupException extends OXException {
	/**
	 * For serialization
	 */
	private static final long serialVersionUID = -5145370116653227016L;

	public GroupException(Category category, int id, String message, Throwable cause, Object...msgParams){
		super(Component.ADMIN_GROUP, category, id, String.format(message, (Object[]) msgParams),cause);
	}

	public GroupException(Category category, String message, int id, Object...msgParams){
		this(category,id,message, null,(Object[])msgParams);
	}
}
