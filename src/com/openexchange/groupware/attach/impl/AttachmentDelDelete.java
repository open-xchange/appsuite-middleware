package com.openexchange.groupware.attach.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.openexchange.groupware.delete.ContextDelete;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;

public class AttachmentDelDelete extends ContextDelete implements DeleteListener {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AttachmentDelDelete.class);

	public void deletePerformed(final DeleteEvent sqlDelEvent, final Connection readCon, final Connection writeCon)
			throws DeleteFailedException {

		if (!isContextDelete(sqlDelEvent)) {
			return;
		}
		PreparedStatement stmt = null;

		try {
			stmt = writeCon.prepareStatement("DELETE FROM del_attachment WHERE cid = ?");
			stmt.setInt(1, sqlDelEvent.getContext().getContextId());
			stmt.executeUpdate();
		} catch (final SQLException e) {
			throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getLocalizedMessage());
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}

		}

	}

}
