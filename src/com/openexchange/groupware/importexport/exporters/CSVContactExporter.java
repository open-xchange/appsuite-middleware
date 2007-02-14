package com.openexchange.groupware.importexport.exporters;

import java.io.InputStream;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Exporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;

public class CSVContactExporter implements Exporter {

	public boolean canExport(SessionObject sobj, Format format, String folder) {
		int folderId = new Integer(folder).intValue();
		FolderObject fo;
		try {
			fo = FolderObject.loadFolderObjectFromDB(folderId, sobj.getContext());
		} catch (OXException e) {
			return false;
		}
		//check format of folder
		if ( fo.getModule() != Types.CONTACT){
			return false;
		}
		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sobj.getUserObject().getId(), sobj.getUserConfiguration());
		} catch (DBPoolingException e) {
			return false;
		} catch (SQLException e) {
			return false;
		}
		return perm.canReadAllObjects();
	}

	public InputStream exportData(SessionObject sessObj, Format format, String folder,
			String... fieldsToBeExported) throws ImportExportException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream exportData(SessionObject sessObj, Format format, String folder, int objectId,
			String... fieldsToBeExported) throws ImportExportException {
		// TODO Auto-generated method stub
		return null;
	}

}
