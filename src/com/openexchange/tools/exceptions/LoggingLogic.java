package com.openexchange.tools.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;

public abstract class LoggingLogic {

	public static LoggingLogic getLoggingLogic(Class klass) {
		// We could add hooks for custom logging logic for certain classes here, if needed. 
		// For now everyone uses the default logic.
		
		return new DefaultLoggingLogic(LogFactory.getLog(klass));
	}
	
	public static LoggingLogic getLoggingLogic(Class klass, Log log) {
		// We could add hooks for custom logging logic for certain classes here, if needed. 
		// For now everyone uses the default logic.
		
		return new DefaultLoggingLogic(log);
	}
	
	protected Log LOG;
	
	public LoggingLogic(Log log) {
		this.LOG = log;
	}
	
	public void log(AbstractOXException aox) {
		Category cat =aox.getCategory();
		if(Category.CODE_ERROR.equals(cat)) {
			this.codeError(aox);
		} else if (Category.CONCURRENT_MODIFICATION.equals(cat)) {
			this.concurrentModification(aox);
		} else if (Category.EXTERNAL_RESOURCE_FULL.equals(cat)){
			this.externalResourceFull(aox);
		} else if (Category.INTERNAL_ERROR.equals(cat)) {
			this.internalError(aox);
		} else if (Category.PERMISSION.equals(cat)) {
			this.permission(aox);
		} else if (Category.SETUP_ERROR.equals(cat)) {
			this.setupError(aox);
		} else if (Category.SOCKET_CONNECTION.equals(cat)) {
			this.socketConnection(aox);
		} else if (Category.SUBSYSTEM_OR_SERVICE_DOWN.equals(cat)) {
			this.subsystemOrServiceDown(aox);
		} else if (Category.TRUNCATED.equals(cat)) {
			this.truncated(aox);
		} else if (Category.TRY_AGAIN.equals(cat)) {
			this.tryAgain(aox);
		} else if (Category.USER_CONFIGURATION.equals(cat)) {
			this.userConfiguration(aox);
		} else if (Category.USER_INPUT.equals(cat)) {
			this.userInput(aox);
		} else if (Category.WARNING.equals(cat)) {
			this.warning(aox);
		} else {
			this.unknownCategory(aox);
		}
	}

	public abstract void unknownCategory(AbstractOXException aox);
	public abstract void warning(AbstractOXException aox);
	public abstract void userInput(AbstractOXException aox);
	public abstract void userConfiguration(AbstractOXException aox);
	public abstract void tryAgain(AbstractOXException aox);
	public abstract void truncated(AbstractOXException aox);
	public abstract void subsystemOrServiceDown(AbstractOXException aox);
	public abstract void socketConnection(AbstractOXException aox);
	public abstract void setupError(AbstractOXException aox);
	public abstract void permission(AbstractOXException aox);
	public abstract void internalError(AbstractOXException aox);
	public abstract void externalResourceFull(AbstractOXException aox);
	public abstract void concurrentModification(AbstractOXException aox);
	public abstract void codeError(AbstractOXException aox);
	
}
