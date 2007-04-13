/**
 * 
 */

package com.openexchange.ajax.spellcheck;

import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * AJAXSpellCheckExceptionFactory
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJAXSpellCheckExceptionFactory extends AbstractOXExceptionFactory<AJAXSpellCheckException> {

	public AJAXSpellCheckExceptionFactory(final Class clazz) {
		super(clazz);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.AbstractOXExceptionFactory#buildException(com.openexchange.groupware.Component,
	 *      com.openexchange.groupware.AbstractOXException.Category, int,
	 *      java.lang.String, java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	protected AJAXSpellCheckException buildException(final Component component, final Category category, final int number, final String message,
			final Throwable cause, final Object... msgArgs) {
		return new AJAXSpellCheckException(component, category, number, message, cause, msgArgs);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.AbstractOXExceptionFactory#getClassId()
	 */
	@Override
	protected int getClassId() {
		return AJAXSpellCheckExceptionClasses.AJAX_SPELL_CHECK_EXCEPTION_FACTORY;
	}

}
