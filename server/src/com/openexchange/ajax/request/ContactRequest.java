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

package com.openexchange.ajax.request;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class ContactRequest {
	
	final SessionObject sessionObj;
	
	final TimeZone timeZone;
	
	private Date timestamp;
	
	private static final Log LOG = LogFactory.getLog(ContactRequest.class);
	
	public Date getTimestamp() {
		return timestamp;
	}

	public ContactRequest(SessionObject sessionObj) {
		this.sessionObj = sessionObj;
		
		final String sTimeZone = UserStorage.getUser(sessionObj.getUserId(), sessionObj.getContext()).getTimeZone();
		
		timeZone = TimeZone.getTimeZone(sTimeZone);
		if (LOG.isDebugEnabled()) {
			LOG.debug("use timezone string: " + sTimeZone);
			LOG.debug("use user timezone: " + timeZone);
		}
	}
	
	public Object action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXConcurrentModificationException, SearchIteratorException, AjaxException, OXException, OXJSONException {
		if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()).hasContact()) {
			throw new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "contact");
		}
		
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			return actionNew(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			return actionDelete(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			return actionUpdate(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			return actionUpdates(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			return actionList(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			return actionAll(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			return actionGet(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			return actionSearch(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
			return actionCopy(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public JSONObject actionNew(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, AjaxException {
		
		final ContactObject contactObj = new ContactObject();
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final ContactParser contactparser = new ContactParser(sessionObj);
		contactparser.parse(contactObj, jData);
		
		if (!contactObj.containsParentFolderID()) {
			throw new OXMandatoryFieldException("missing folder");
		}
		
		final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		contactsql.insertContactObject(contactObj);
		
		final JSONObject jsonResponseObject = new JSONObject();
		jsonResponseObject.put(ContactFields.ID, contactObj.getObjectID());
		
		return jsonResponseObject;
	}
	
	public JSONObject actionUpdate(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConcurrentModificationException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		
		final ContactObject contactobject = new ContactObject();
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final ContactParser contactparser = new ContactParser(sessionObj);
		contactparser.parse(contactobject, jData);
		
		contactobject.setObjectID(id);
		
		final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		contactsql.updateContactObject(contactobject, inFolder, timestamp);
		
		return new JSONObject();
	}
	
	public JSONArray actionUpdates(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		SearchIterator it = null;
		
		try {
			timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
			final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
			final String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);
			
			boolean bIgnoreDelete = false;
			
			if (ignore != null && ignore.indexOf("deleted") != -1) {
				bIgnoreDelete = true;
			}
			
			int[] internalColumns = new int[columns.length+1];
			System.arraycopy(columns, 0, internalColumns, 0, columns.length);
			internalColumns[columns.length] = DataObject.LAST_MODIFIED;
			
			final ContactWriter contactWriter = new ContactWriter(timeZone);
			final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
			it = contactsql.getModifiedContactsInFolder(folderId, internalColumns, timestamp);
			while (it.hasNext()) {
				final ContactObject contactObj = (ContactObject)it.next();
				final JSONArray jsonContactArray = new JSONArray();
				contactWriter.writeArray(contactObj, columns, jsonContactArray);
				jsonResponseArray.put(jsonContactArray);

				lastModified = contactObj.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			if (!bIgnoreDelete) {
				it = contactsql.getDeletedContactsInFolder(folderId, internalColumns, timestamp);
				while (it.hasNext()) {
					final ContactObject contactObj = (ContactObject)it.next();
					
					jsonResponseArray.put(contactObj.getObjectID());
					
					lastModified = contactObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			}
			
			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public JSONArray actionDelete(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final int objectId = DataParser.checkInt(jData, DataFields.ID);
		final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);
		
		final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		try {
			contactsql.deleteContactObject(objectId, inFolder, timestamp);
		} catch (Exception e) {
			throw new OXException(e);
		}
		
		return new JSONArray();
	}
	
	public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		SearchIterator it = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		try {
			final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
			final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
			final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
			int[][] objectIdAndFolderId = new int[jData.length()][2];
			for (int a = 0; a < objectIdAndFolderId.length; a++) {
				final JSONObject jObject = jData.getJSONObject(a);
				objectIdAndFolderId[a][0] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
				objectIdAndFolderId[a][1] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);
			}
			
			int[] internalColumns = new int[columns.length+1];
			System.arraycopy(columns, 0, internalColumns, 0, columns.length);
			internalColumns[columns.length] = DataObject.LAST_MODIFIED;
			
			final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
			final ContactWriter contactwriter = new ContactWriter(timeZone);
			
			try {
				it = contactsql.getObjectsById(objectIdAndFolderId, internalColumns);
				
				while (it.hasNext()) {
					final ContactObject contactObj = (ContactObject)it.next();
					final JSONArray jsonContactArray = new JSONArray();
					contactwriter.writeArray(contactObj, columns, jsonContactArray);
					jsonResponseArray.put(jsonContactArray);
					
					lastModified = contactObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			} catch (Exception e) {
				throw new OXException(e);
			}
			
			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public JSONArray actionAll(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
		final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
		
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		SearchIterator it = null;
		
		try {
			int[] internalColumns = new int[columns.length+1];
			System.arraycopy(columns, 0, internalColumns, 0, columns.length);
			internalColumns[columns.length] = DataObject.LAST_MODIFIED;
			
			final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
			final ContactWriter contactwriter = new ContactWriter(timeZone);
			it = contactsql.getContactsInFolder(folderId, 0, 50000, orderBy, orderDir, internalColumns);
			
			while (it.hasNext()) {
				final ContactObject contactObj = (ContactObject)it.next();
				final JSONArray jsonContactArray = new JSONArray();
				contactwriter.writeArray(contactObj, columns, jsonContactArray);
				jsonResponseArray.put(jsonContactArray);
				
				lastModified = contactObj.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public JSONObject actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		
		final ContactSQLInterface sqlinterface = new RdbContactSQLInterface(sessionObj);
		
		timestamp = new Date(0);
		
		final ContactObject contactObj = sqlinterface.getObjectById(id, inFolder);
		final ContactWriter contactwriter = new ContactWriter(timeZone);
		
		final JSONObject jsonResponseObject = new JSONObject();
		contactwriter.writeContact(contactObj, jsonResponseObject);
		
		timestamp = contactObj.getLastModified();
		
		return jsonResponseObject;
	}
	
	public JSONArray actionSearch(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		
		boolean startletter = false;
		
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		SearchIterator it = null;
		
		try {
			final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
			final ContactSearchObject searchObj = new ContactSearchObject();
			if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
				searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
			} else {
				searchObj.setAllFolders(true);
			}
			
			if (jData.has("pattern")) {
				searchObj.setPattern(DataParser.parseString(jData, "pattern"));
			}
			if (jData.has("startletter")){
				startletter = DataParser.parseBoolean(jData, "startletter");
			}
			if (jData.has("emailAutoComplete")){
				searchObj.setEmailAutoComplete(true);
				searchObj.setAllFolders(false);
			}
			
			final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
			final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
			
			searchObj.setSurname(DataParser.parseString(jData, ContactFields.LAST_NAME));
			searchObj.setDisplayName(DataParser.parseString(jData, ContactFields.DISPLAY_NAME));
			searchObj.setGivenName(DataParser.parseString(jData, ContactFields.FIRST_NAME));
			searchObj.setCompany(DataParser.parseString(jData, ContactFields.COMPANY));
			searchObj.setEmail1(DataParser.parseString(jData, ContactFields.EMAIL1));
			searchObj.setEmail2(DataParser.parseString(jData, ContactFields.EMAIL2));
			searchObj.setEmail3(DataParser.parseString(jData, ContactFields.EMAIL3));
			searchObj.setDynamicSearchField(DataParser.parseJSONIntArray(jData, "dynamicsearchfield"));
			searchObj.setDynamicSearchFieldValue(DataParser.parseJSONStringArray(jData, "dynamicsearchfieldvalue"));
			searchObj.setPrivatePostalCodeRange(DataParser.parseJSONStringArray(jData, "privatepostalcoderange"));
			searchObj.setBusinessPostalCodeRange(DataParser.parseJSONStringArray(jData, "businesspostalcoderange"));
			searchObj.setPrivatePostalCodeRange(DataParser.parseJSONStringArray(jData, "privatepostalcoderange"));
			searchObj.setOtherPostalCodeRange(DataParser.parseJSONStringArray(jData, "otherpostalcoderange"));
			searchObj.setBirthdayRange(DataParser.parseJSONDateArray(jData, "birthdayrange"));
			searchObj.setAnniversaryRange(DataParser.parseJSONDateArray(jData, "anniversaryrange"));
			searchObj.setNumberOfEmployeesRange(DataParser.parseJSONStringArray(jData, "numberofemployee"));
			searchObj.setSalesVolumeRange(DataParser.parseJSONStringArray(jData, "salesvolumerange"));
			searchObj.setCreationDateRange(DataParser.parseJSONDateArray(jData, "creationdaterange"));
			searchObj.setLastModifiedRange(DataParser.parseJSONDateArray(jData, "lastmodifiedrange"));
			searchObj.setCatgories(DataParser.parseString(jData, "categories"));
			searchObj.setSubfolderSearch(DataParser.parseBoolean(jData, "subfoldersearch"));
			
			int[] internalColumns = new int[columns.length+1];
			System.arraycopy(columns, 0, internalColumns, 0, columns.length);
			internalColumns[columns.length] = DataObject.LAST_MODIFIED;
			
			final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
			final ContactWriter contactwriter = new ContactWriter(timeZone);
			
			if (searchObj.getFolder() > 0 && (searchObj.getPattern() != null || startletter)) {
				it = contactsql.searchContacts(searchObj.getPattern(), startletter, searchObj.getFolder(), orderBy, orderDir, internalColumns);
				
				while (it.hasNext()) {
					final ContactObject contactObj = (ContactObject)it.next();
					final JSONArray jsonContactArray = new JSONArray();
					contactwriter.writeArray(contactObj, columns, jsonContactArray);
					jsonResponseArray.put(jsonContactArray);
					
					lastModified = contactObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			} else {
				it = contactsql.getContactsByExtendedSearch(searchObj, orderBy, orderDir, internalColumns);
				
				while (it.hasNext()) {
					final ContactObject contactObj = (ContactObject)it.next();
					final JSONArray jsonContactArray = new JSONArray();
					contactwriter.writeArray(contactObj, columns, jsonContactArray);
					jsonResponseArray.put(jsonContactArray);
					
					lastModified = contactObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			}
			
			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public JSONObject actionCopy(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		timestamp = new Date(0);
		
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);
		
		final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessionObj);
		final  ContactObject contactObj = contactInterface.getObjectById(id, inFolder);
		contactObj.removeObjectID();
		contactObj.setParentFolderID(folderId);
		
		if (inFolder == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
			contactObj.removeInternalUserId();
		}
		
		contactInterface.insertContactObject(contactObj);
		
		final JSONObject jsonResponseObject = new JSONObject();
		jsonResponseObject.put(ContactFields.ID, contactObj.getObjectID());
		
		return jsonResponseObject;
	}
}
