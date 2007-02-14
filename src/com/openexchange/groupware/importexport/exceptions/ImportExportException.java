package com.openexchange.groupware.importexport.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * An exception thrown by classes associated with the import or export of
 * OX data.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ImportExportException extends OXException {

	private static final long serialVersionUID = 8368543799201210727L;

	public ImportExportException(Category category, int id, String message, Throwable cause, Object...msgParams){
		super(Component.INFOSTORE, category, id, String.format(message, (Object[]) msgParams),cause);
	}

	public ImportExportException(Category category, String message, int id, Object...msgParams){
		this(category,id,message, null,(Object[])msgParams);
	}

	public ImportExportException(AbstractOXException e1) {
		super(e1);
	}
}
