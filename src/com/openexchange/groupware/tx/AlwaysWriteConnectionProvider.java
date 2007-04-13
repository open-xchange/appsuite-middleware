package com.openexchange.groupware.tx;

import java.sql.Connection;

import com.openexchange.groupware.contexts.Context;

public class AlwaysWriteConnectionProvider implements DBProvider {

	private DBProvider delegate;

	public AlwaysWriteConnectionProvider(DBProvider delegate) {
		this.delegate = delegate;
	}
	
	public Connection getReadConnection(final Context ctx)
			throws TransactionException {
		return delegate.getWriteConnection(ctx);
	}

	public Connection getWriteConnection(final Context ctx)
			throws TransactionException {
		return delegate.getWriteConnection(ctx);
	}

	public void releaseReadConnection(final Context ctx, final Connection con) {
		delegate.releaseWriteConnection(ctx, con);
	}

	public void releaseWriteConnection(final Context ctx, final Connection con) {
		delegate.releaseWriteConnection(ctx, con);
	}

}
