package com.openexchange.solr.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.solr.internal.Services;
import com.openexchange.tools.sql.DBUtils;

public class SolrCoresCreateTableTask extends UpdateTaskAdapter {
	
	private final SolrCoresCreateTableService createTableService;
	

	public SolrCoresCreateTableTask(final SolrCoresCreateTableService createTableService) {
		super();
		this.createTableService = createTableService;
	}

	@Override
	public void perform(final PerformParameters params) throws OXException {
		final DatabaseService dbService = Services.getService(DatabaseService.class);
		final int contextId = params.getContextId();
		final Connection con = dbService.getForUpdateTask(contextId);
		try {
			final String[] tables = createTableService.tablesToCreate();
			final String[] statements = createTableService.getCreateStatements();
			DBUtils.startTransaction(con);
			for (int i = 0; i < createTableService.tablesToCreate().length; i++) {
				final String table = tables[i];
				if (!DBUtils.tableExists(con, table)) {
				    PreparedStatement stmt = null;
					try {
                        final String statement = statements[i];
                        stmt = con.prepareStatement(statement);
                        stmt.executeUpdate();
                    } finally {
                        DBUtils.closeSQLStuff(stmt);
                    }
				}				
			}
			con.commit();
		} catch (SQLException e) {
			DBUtils.rollback(con);
			throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
		} finally {
			DBUtils.autocommit(con);
			dbService.backForUpdateTask(contextId, con);
		}
	}

	@Override
	public String[] getDependencies() {
		return new String[] {  };
	}

}
