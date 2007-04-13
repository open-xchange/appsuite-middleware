/**
 * 
 */
package com.openexchange.ajax.spellcheck;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * AJAXSpellCheckException
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class AJAXSpellCheckException extends AbstractOXException {

	private static final long serialVersionUID = -6352139947703301097L;

	public AJAXSpellCheckException(final AbstractOXException cause) {
		super(cause);
	}
	
	public AJAXSpellCheckException(final Component component, final Category category, final int number, final String message,
			final Throwable cause, final Object... msgArgs) {
		super(component, category, number, message, cause);
		setMessageArgs(msgArgs);
	}

}
