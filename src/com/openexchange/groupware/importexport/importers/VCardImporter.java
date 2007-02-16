package com.openexchange.groupware.importexport.importers;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@OXExceptionSource(
		classId=ImportExportExceptionClasses.VCARDIMPORTER,
		component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
		category={
	Category.PERMISSION,
	Category.SUBSYSTEM_OR_SERVICE_DOWN,
	Category.USER_INPUT,
	Category.PROGRAMMING_ERROR},
		desc={"","","",""},
		exceptionId={0,1,2,3},
		msg={
	"Could not import into the folder %s.",
	"Subsystem down",
	"User input Error %s",
	"Programming Error"})

public class VCardImporter implements Importer {
	
	private static final Log LOG = LogFactory.getLog(VCardImporter.class);
	
	private static ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(VCardImporter.class);
	
	public boolean canImport(final SessionObject sessObj, final Format format, final Map<String, Integer> folderMappings, final Map<String, String[]> optionalParams) throws ImportExportException {
		final Iterator iterator = folderMappings.keySet().iterator();
		while (iterator.hasNext()) {
			String folder = iterator.next().toString();
			
			int folderId = new Integer(folder).intValue();
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				return false;
			}
			
			int type = folderMappings.get(folder).intValue();
			//check format of folder
			if ( type == Types.CONTACT && fo.getModule() != type ){
				return false;
			}
			//check read access to folder
			EffectivePermission perm;
			try {
				perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
			} catch (DBPoolingException e) {
				throw importExportExceptionFactory.create(1, folder);
			} catch (SQLException e) {
				throw importExportExceptionFactory.create(1, folder);
			}
			return perm.canWriteAllObjects();
		}
		
		return true;
	}
	
	public List<ImportResult> importData(final SessionObject sessObj, final Format format, final InputStream is, final Map<String, Integer> folderMappings, final Map<String, String[]> optionalParams) throws ImportExportException {
		int contactFolderId = -1;
		
		final Iterator iterator = folderMappings.keySet().iterator();
		while (iterator.hasNext()) {
			String folder = iterator.next().toString();
			
			int folderId = new Integer(folder).intValue();
			int type = folderMappings.get(folder).intValue();
			
			if (type == Types.CONTACT) {
				contactFolderId = folderId;
				break;
			}
		}
		
		final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessObj);
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
		
		List<ImportResult> list = new ArrayList<ImportResult>();
		
		try {
			final VersitDefinition def = Versit.getDefinition(format.getMimeType());
			final VersitDefinition.Reader versitReader = def.getReader(is, "UTF-8");
			final VersitObject rootVersitObject = def.parseBegin(versitReader);
			VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);
			while (versitObject != null) {
				ImportResult importResult = new ImportResult();
				try {
					final Property property = versitObject.getProperty("UID");
					
					importResult.setFolder(String.valueOf(contactFolderId));
								
					final ContactObject contactObj = oxContainerConverter.convertContact(versitObject);
					contactObj.setParentFolderID(contactFolderId);
					contactInterface.insertContactObject(contactObj);
						
					importResult.setObjectId(String.valueOf(contactObj.getObjectID()));
					importResult.setDate(contactObj.getLastModified());
				} catch (OXException exc) {
					LOG.debug("cannot import contact object", exc);
					importResult.setException(exc);
				}
				
				list.add(importResult);
				
				versitObject = def.parseChild(versitReader, rootVersitObject);
			}
		} catch (Exception exc) {
			throw new ImportExportException(exc);
		} finally {
			oxContainerConverter.close();
		}
		
		return list;
	}
}
