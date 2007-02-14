package com.openexchange.groupware.importexport.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * A factory producing exceptions related to the import or export of OX data
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ImportExportExceptionFactory extends AbstractOXExceptionFactory {

	public ImportExportExceptionFactory(Class clazz) {
		super(clazz);
	}
		
	private static final int CLASS = ImportExportExceptionClasses.IMPORTEXPORTEXCEPTIONFACTORY;
		
	@Override
	protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
		if(component != Component.INFOSTORE) {
			throw new IllegalArgumentException("This factory can only build exceptions for the infostore");
		}
		return new ImportExportException(category,number,message,cause,(Object[])msgArgs);
	}

	@Override
	protected int getClassId() {
		return CLASS;
	}

	public ImportExportException create(int id, Object...msgParams) {
		return (ImportExportException) createException(id,(Object[]) msgParams);
	}

	public ImportExportException create(int id, Throwable cause, Object...msgParams) {
		return (ImportExportException) createException(id,cause, (Object[]) msgParams);
	}

}

