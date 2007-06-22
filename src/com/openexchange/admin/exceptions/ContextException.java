
package com.openexchange.admin.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;

public class ContextException extends OXException {
    /**
	 * For serialization.
	 */
	private static final long serialVersionUID = -7885788736269214361L;

	public ContextException(Category category, int id, String message, Throwable cause, Object...msgParams){
        super(Component.ADMIN_CONTEXT, category, id, String.format(message, (Object[]) msgParams),cause);
    }
    
    public ContextException(Category category, String message, int id, Object...msgParams){
        this(category,id,message, null,(Object[])msgParams);
    }
}
