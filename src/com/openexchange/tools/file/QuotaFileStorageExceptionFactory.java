package com.openexchange.tools.file;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.InfostoreException;

public class QuotaFileStorageExceptionFactory extends
		AbstractOXExceptionFactory {

	public QuotaFileStorageExceptionFactory(final Class clazz) {
		super(clazz);
	}
	
	private static final int CLASS = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_INFOSTOREEXCEPTIONFACTORY;
	
	@Override
	protected AbstractOXException buildException(final Component component, final Category category, final int number, final String message, final Throwable cause, final Object... msgArgs) {
		if(component != Component.FILESTORE) {
			throw new IllegalArgumentException("This factory can only build exceptions for the filestore");
		}
		return new InfostoreException(category,number,message,cause,msgArgs);
	}
	
	@Override
	protected int getClassId() {
		return CLASS;
	}
	
	public InfostoreException create(final int id, final Object...msgParams) {
		return (InfostoreException) createException(id,msgParams);
	}
	
	public InfostoreException create(final int id, final Throwable cause, final Object...msgParams) {
		return (InfostoreException) createException(id,cause, msgParams);
	}

}
