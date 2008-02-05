/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.importexport.importers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractImporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.tools.versit.filetokenizer.VCardFileToken;
import com.openexchange.tools.versit.filetokenizer.VCardTokenizer;
import com.openexchange.tools.session.ServerSession;

@OXExceptionSource(
    classId=ImportExportExceptionClasses.VCARDIMPORTER,
    component=Component.IMPORT_EXPORT
)
@OXThrowsMultiple(
    category={
        Category.PERMISSION,
    	Category.SUBSYSTEM_OR_SERVICE_DOWN,
    	Category.USER_INPUT,
    	Category.CODE_ERROR,
    	Category.CODE_ERROR,
    	Category.USER_INPUT,
    	Category.CODE_ERROR,
    	Category.PERMISSION,
        Category.USER_INPUT
    },
	desc={"", "", "", "", "", "", "", "", ""},
	exceptionId={0, 1, 2, 3, 4, 5, 6, 7, 8},
	msg={
    	"Could not import into the folder %s.",
    	"Subsystem down",
    	"User input error %s",
    	"Programming error - folder %s",
    	"Could not load folder %s",
    	"Could not recognize format of the following data: %s",
    	"Could not use UTF-8 encoding.",
    	"Module Contacts is not enabled for this user, cannot store contacts contained in VCard.",
        "No VCard to import found."
    }
)
	/**
	 * This importer translates VCards into contacts for the OX.
	 * 
	 * @see OXContainerConverter - if you have a problem with the content of the parsed ICAL file
	 * @see ContactSQLInterface - if you cannot enter the parsed content as contact into the database
	 * 
	 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
	 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface)
	 */
	public class VCardImporter extends AbstractImporter implements Importer {
	
	private static final Log LOG = LogFactory.getLog(VCardImporter.class);
	
	private static ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(VCardImporter.class);
	
	public boolean canImport(final ServerSession sessObj, final Format format, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException {
		if (!format.equals(Format.VCARD)) {
			return false;
		}
		if(!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasContact() ){
			throw importExportExceptionFactory.create(7, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Contacts") );
		}
		final Iterator iterator = folders.iterator();
		while (iterator.hasNext()) {
			final String folder = iterator.next().toString();
			
			int folderId = 0;
			try {
				folderId = Integer.parseInt(folder);
			} catch (NumberFormatException exc) {
				throw importExportExceptionFactory.create(0, folder);
			}

			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				return false;
			}
			
			//check format of folder
			if ( fo.getModule() == FolderObject.CONTACT){
				if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasContact()) {
					return false;
				}
			} else {
				return false;
			}
			//check read access to folder
			EffectivePermission perm;
			try {
				perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage
						.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()));
			} catch (DBPoolingException e) {
				throw importExportExceptionFactory.create(1, folder);
			} catch (SQLException e) {
				throw importExportExceptionFactory.create(1, folder);
			}
			
			if (perm.canCreateObjects()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<ImportResult> importData(final ServerSession sessObj, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException {
		
		int contactFolderId = -1;
		
		final Iterator iterator = folders.iterator();
		while (iterator.hasNext()) {
			final String folder = iterator.next().toString();
			
			int folderId = Integer.parseInt(folder);
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				throw importExportExceptionFactory.create(4,folderId);
			}
			
			if (fo.getModule() == FolderObject.CONTACT) {
				contactFolderId = folderId;
				break;
			}
		}
		
		final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessObj);
		OXContainerConverter oxContainerConverter = null;
		
		List<ImportResult> list = new ArrayList<ImportResult>();
		
		try {
            oxContainerConverter = new OXContainerConverter(sessObj);
            VCardTokenizer tokenizer = new VCardTokenizer(is);
			List<VCardFileToken> chunks = tokenizer.split();
            if (0 == chunks.size()) {
                throw importExportExceptionFactory.create(8);
            }
			for(VCardFileToken chunk: chunks){
				VersitDefinition def = chunk.getVersitDefinition();
				ImportResult importResult = new ImportResult();
				
				if(def != null){
					final VersitDefinition.Reader versitReader = def.getReader(
							new ByteArrayInputStream(chunk.getContent()), "UTF-8");
					try {
						VersitObject versitObject = def.parse(versitReader);
						
						importResult.setFolder(String.valueOf(contactFolderId));
						
						final ContactObject contactObj = oxContainerConverter.convertContact(versitObject);
						contactObj.setParentFolderID(contactFolderId);
						importResult.setDate(new Date());
						try {
							contactInterface.insertContactObject(contactObj);
						} catch (OXException oxEx){
							oxEx = handleDataTruncation(oxEx);
							LOG.debug("cannot import contact object", oxEx);
							importResult.setException(oxEx);
						}
						importResult.setObjectId(String.valueOf(contactObj.getObjectID()));
						importResult.setDate(contactObj.getLastModified());
					} catch (ConverterException exc) {
						LOG.error("cannot convert contact object", exc);
						importResult.setException(new OXException("cannot parse vcard object", exc));
					} catch (VersitException exc) {
						LOG.error("cannot parse contact object", exc);
						importResult.setException(new OXException("cannot parse vcard object", exc));
					} 
				} else {
					//could not find appropriate parser for this part of the vcard file
					LOG.error("Could not recognize format of the following VCard data: " + chunk.getContent());
					importResult.setDate(new Date(System.currentTimeMillis()));
					importResult.setException(importExportExceptionFactory.create(5, chunk.getContent()));
				}
				list.add(importResult);
			}
		} catch (UnsupportedEncodingException e){
			LOG.fatal(e);
			throw importExportExceptionFactory.create(6);
		} catch (IOException e) {
			LOG.error(e);
			throw importExportExceptionFactory.create(4, contactFolderId);
		} catch (ConverterException e) {
            LOG.error(e);
			throw importExportExceptionFactory.create(1, e);
        } finally {
            if(oxContainerConverter != null)
                oxContainerConverter.close();
		}
		
		return list;
	}

	@Override
	protected String getNameForFieldInTruncationError(int id, OXException unused) {
		final ContactField field = ContactField.getByValue(id);
		if(field == null){
			return String.valueOf( id );
		}
		return field.getReadableName();
	}

}
