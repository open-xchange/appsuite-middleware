package com.openexchange.tools.file;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.InfostoreException;

public class QuotaFileStorageExceptionFactory extends
		AbstractOXExceptionFactory {

	public QuotaFileStorageExceptionFactory(Class clazz) {
		super(clazz);
	}
	
	private static final int CLASS = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_INFOSTOREEXCEPTIONFACTORY;
	
	@Override
	protected AbstractOXException buildException(Component component, Category category, int number, String message, Throwable cause, Object... msgArgs) {
		if(component != Component.FILESTORE) {
			throw new IllegalArgumentException("This factory can only build exceptions for the filestore");
		}
		return new InfostoreException(category,number,message,cause,(Object[])msgArgs);
	}
	
	@Override
	protected int getClassId() {
		return CLASS;
	}
	
	public InfostoreException create(int id, Object...msgParams) {
		return (InfostoreException) createException(id,(Object[]) msgParams);
	}
	
	public InfostoreException create(int id, Throwable cause, Object...msgParams) {
		return (InfostoreException) createException(id,cause, (Object[]) msgParams);
	}

}
