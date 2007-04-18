package com.openexchange.tools.file;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.openexchange.groupware.delete.ContextDelete;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;

public class QuotaUsageDelete extends ContextDelete {

	public void deletePerformed(DeleteEvent sqlDelEvent, Connection readCon,
			Connection writeCon) throws DeleteFailedException, LdapException,
			SQLException, DBPoolingException {
		
		if(!isContextDelete(sqlDelEvent))
			return;
		
		PreparedStatement stmt = null;
		try{
			stmt = writeCon.prepareStatement("DELETE FROM filestore_usage WHERE cid = ?");
			stmt.setInt(1,sqlDelEvent.getContext().getContextId());
			stmt.executeUpdate();
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}

}
