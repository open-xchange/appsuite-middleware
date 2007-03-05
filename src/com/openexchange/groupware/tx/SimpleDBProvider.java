package com.openexchange.groupware.tx;

import java.sql.Connection;

import com.openexchange.groupware.contexts.Context;

public class SimpleDBProvider implements DBProvider {

	private Connection writeCon;
	private Connection readCon;

	public SimpleDBProvider(Connection readCon, Connection writeCon) {
		this.readCon = readCon;
		this.writeCon = writeCon;
	}

	public Connection getReadConnection(Context ctx)
			throws TransactionException {
		return readCon;
	}

	public Connection getWriteConnection(Context ctx)
			throws TransactionException {
		return writeCon;
	}

	public void releaseReadConnection(Context ctx, Connection con) {

	}

	public void releaseWriteConnection(Context ctx, Connection con) {

	}

}
