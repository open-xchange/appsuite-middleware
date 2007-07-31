package com.openexchange.tools.exceptions;

import org.apache.commons.logging.Log;

import com.openexchange.groupware.AbstractOXException;

public class DefaultLoggingLogic extends LoggingLogic {

	public DefaultLoggingLogic(Log log) {
		super(log);
	}

	@Override
	public void codeError(AbstractOXException aox) {
		LOG.error("Coding Error: "+aox.toString(), aox);
	}

	@Override
	public void concurrentModification(AbstractOXException aox) {
		LOG.debug("Concurrent Modification: "+aox.toString(), aox);
	}

	@Override
	public void externalResourceFull(AbstractOXException aox) {
		LOG.fatal("External Resource is full: "+aox.toString(), aox);
	}

	@Override
	public void internalError(AbstractOXException aox) {
		LOG.error("An internal error occurred: "+aox.toString(), aox);
	}

	@Override
	public void permission(AbstractOXException aox) {
		LOG.debug("Permission Exception: "+aox.toString(), aox);
	}

	@Override
	public void setupError(AbstractOXException aox) {
		LOG.fatal("Setup Error: "+aox.toString(), aox);
	}

	@Override
	public void socketConnection(AbstractOXException aox) {
		LOG.fatal("Socket Connection Excpetion: "+aox.toString(), aox);
	}

	@Override
	public void subsystemOrServiceDown(AbstractOXException aox) {
		LOG.fatal("Subsystem or service down: "+aox.toString(), aox);
	}

	@Override
	public void truncated(AbstractOXException aox) {
		LOG.debug("Database truncated fields: "+aox.toString(), aox);
	}

	@Override
	public void tryAgain(AbstractOXException aox) {
		LOG.error("Temporarily Disabled? "+aox.toString(), aox);
	}

	@Override
	public void unknownCategory(AbstractOXException aox) {
		LOG.error("Unkown Category: "+aox.toString(), aox);
	}

	@Override
	public void userConfiguration(AbstractOXException aox) {
		LOG.error("User Configuration Error: "+aox.toString(), aox);
	}

	@Override
	public void userInput(AbstractOXException aox) {
		LOG.debug("User Input: "+aox.toString(), aox);
	}

	@Override
	public void warning(AbstractOXException aox) {
		LOG.warn("Warning: "+aox.toString(), aox);
	}

}
