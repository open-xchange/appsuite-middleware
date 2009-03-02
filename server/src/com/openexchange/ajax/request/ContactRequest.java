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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.database.Database;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactServices;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.links.Links;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class ContactRequest {

    public static final String ACTION_GET_USER = "getuser";

    public static final String ACTION_LIST_USER = "listuser";

    final Session sessionObj;

    final Context ctx;

    final TimeZone timeZone;

    private Date timestamp;

    private static final Log LOG = LogFactory.getLog(ContactRequest.class);

    public Date getTimestamp() {
        return timestamp;
    }

    public ContactRequest(final Session sessionObj, final Context ctx) {
        this.sessionObj = sessionObj;
        this.ctx = ctx;

        final String sTimeZone = UserStorage.getStorageUser(sessionObj.getUserId(), ctx).getTimeZone();

        timeZone = TimeZone.getTimeZone(sTimeZone);
        if (LOG.isDebugEnabled()) {
            LOG.debug("use timezone string: " + sTimeZone);
            LOG.debug("use user timezone: " + timeZone);
        }
    }

    public Object action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException,
            JSONException, OXConcurrentModificationException, SearchIteratorException, AjaxException, OXException,
            OXJSONException {
        if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx).hasContact()) {
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
        } else if (action.equalsIgnoreCase(ACTION_LIST_USER)) {
            return actionListUser(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
            return actionAll(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            return actionGet(jsonObject);
        } else if (action.equalsIgnoreCase(ACTION_GET_USER)) {
            return actionGetUser(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
            return actionSearch(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
            return actionCopy(jsonObject);
        } else {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
    }

    public JSONObject actionNew(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException,
            AjaxException {

        final ContactObject contactObj = new ContactObject();
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final ContactParser contactparser = new ContactParser(sessionObj);
        contactparser.parse(contactObj, jData);

        if (!contactObj.containsParentFolderID()) {
            throw new OXMandatoryFieldException("missing folder");
        }

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(contactObj.getParentFolderID(),
                ctx.getContextId());
        // ContactInterface contactInterface =
        // ContactServices.getInstance().getService
        // (contactObj.getParentFolderID());
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);
        contactInterface.insertContactObject(contactObj);
        timestamp = contactObj.getLastModified();
        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, contactObj.getObjectID());

        return jsonResponseObject;
    }

    public JSONObject actionUpdate(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException,
            OXConcurrentModificationException, OXException, OXJSONException, AjaxException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);

        final ContactObject contactobject = new ContactObject();
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final ContactParser contactparser = new ContactParser(sessionObj);
        contactparser.parse(contactobject, jData);

        contactobject.setObjectID(id);

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(inFolder, ctx.getContextId());
        // ContactInterface contactInterface =
        // ContactServices.getInstance().getService(inFolder);
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);
        contactInterface.updateContactObject(contactobject, inFolder, timestamp);
        timestamp = contactobject.getLastModified();
        return new JSONObject();
    }

    public JSONArray actionUpdates(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException,
            SearchIteratorException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);

        final Date requestedTimestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        timestamp = new Date(requestedTimestamp.getTime());

        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);

        if (ignore == null) {
            ignore = "deleted";
        }

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<ContactObject> it = null;

        try {
            boolean bIgnoreDelete = false;

            if (ignore.indexOf("deleted") != -1) {
                bIgnoreDelete = true;
            }

            final int[] internalColumns = new int[columnsToLoad.length + 1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            Context ctx = null;
            try {
                ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
            } catch (final ContextException ct) {
                throw new ContactException(ct);
            }

            final ContactWriter contactWriter = new ContactWriter(timeZone);
            ContactInterface contactInterface = ContactServices.getInstance().getService(folderId, ctx.getContextId());
            // ContactInterface contactInterface =
            // ContactServices.getInstance().getService(folderId);
            if (contactInterface == null) {
                contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
            }
            contactInterface.setSession(sessionObj);

            it = contactInterface.getModifiedContactsInFolder(folderId, internalColumns, requestedTimestamp);
            while (it.hasNext()) {
                final ContactObject contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactWriter.writeArray(contactObj, columns, jsonContactArray);
                jsonResponseArray.put(jsonContactArray);

                if (timestamp.before(contactObj.getLastModified())) {
                    timestamp = contactObj.getLastModified();
                }
            }

            if (!bIgnoreDelete) {
                it = contactInterface.getDeletedContactsInFolder(folderId, internalColumns, requestedTimestamp);
                while (it.hasNext()) {
                    final ContactObject contactObj = it.next();

                    jsonResponseArray.put(contactObj.getObjectID());

                    if (timestamp.before(contactObj.getLastModified())) {
                        timestamp = contactObj.getLastModified();
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

    public JSONArray actionDelete(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException,
            OXException, OXJSONException, AjaxException {
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final int objectId = DataParser.checkInt(jData, DataFields.ID);
        final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(inFolder, ctx.getContextId());
        // ContactInterface contactInterface =
        // ContactServices.getInstance().getService(inFolder);
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);
        contactInterface.deleteContactObject(objectId, inFolder, timestamp);

        return new JSONArray();
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException,
            SearchIteratorException, OXException, OXJSONException, AjaxException {
        timestamp = new Date(0);

        Date lastModified = null;

        SearchIterator<ContactObject> it = null;

        final JSONArray jsonResponseArray = new JSONArray();

        boolean isOneFolder = true;

        try {
            final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
            final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
            final int[] columnsToLoad = removeVirtual(columns);
            final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
            int oldfolderId = 0;
            final int[][] objectIdAndFolderId = new int[jData.length()][2];
            for (int a = 0; a < objectIdAndFolderId.length; a++) {
                final JSONObject jObject = jData.getJSONObject(a);
                objectIdAndFolderId[a][0] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
                objectIdAndFolderId[a][1] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);
                if (a > 0) {
                    if (oldfolderId != objectIdAndFolderId[a][1]) {
                        isOneFolder = false;
                    }
                }
                oldfolderId = objectIdAndFolderId[0][1];
            }

            final int[] internalColumns = new int[columnsToLoad.length + 1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            Context ctx = null;
            try {
                ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
            } catch (final ContextException ct) {
                throw new ContactException(ct);
            }

            try {
                // check if the int array has everytime the same folder id
                if (isOneFolder) {
                    ContactInterface contactInterface = ContactServices.getInstance().getService(oldfolderId,
                            ctx.getContextId());
                    // ContactInterface contactInterface =
                    // ContactServices.getInstance().getService(oldfolderId);
                    if (contactInterface == null) {
                        contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
                    }
                    contactInterface.setSession(sessionObj);

                    final ContactWriter contactwriter = new ContactWriter(timeZone);

                    it = contactInterface.getObjectsById(objectIdAndFolderId, internalColumns);

                    while (it.hasNext()) {
                        final ContactObject contactObj = it.next();
                        final JSONArray jsonContactArray = new JSONArray();
                        contactwriter.writeArray(contactObj, columns, jsonContactArray);
                        jsonResponseArray.put(jsonContactArray);

                        if (timestamp.before(contactObj.getLastModified())) {
                            timestamp = contactObj.getLastModified();
                        }
                    }
                } else {
                    // costs more performance because every object in the array
                    // is checked
                    for (int a = 0; a < objectIdAndFolderId.length; a++) {
                        ContactInterface contactInterface = ContactServices.getInstance().getService(
                                objectIdAndFolderId[a][1], ctx.getContextId());
                        // ContactInterface contactInterface =
                        // ContactServices.getInstance
                        // ().getService(objectIdAndFolderId[a][1]);
                        if (contactInterface == null) {
                            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
                        }
                        contactInterface.setSession(sessionObj);
                        final ContactWriter contactwriter = new ContactWriter(timeZone);

                        final int[][] newObjectIdAndFolderId = { { objectIdAndFolderId[a][0], objectIdAndFolderId[a][1] } };
                        it = contactInterface.getObjectsById(newObjectIdAndFolderId, internalColumns);

                        while (it.hasNext()) {
                            final ContactObject contactObj = it.next();
                            final JSONArray jsonContactArray = new JSONArray();
                            contactwriter.writeArray(contactObj, columns, jsonContactArray);
                            jsonResponseArray.put(jsonContactArray);

                            lastModified = contactObj.getLastModified();
                        }

                        if ((lastModified != null) && (timestamp.getTime() < lastModified.getTime())) {
                            timestamp = lastModified;
                        }
                    }
                }
            } catch (final OXException e) {
                throw e;
            } catch (final JSONException e) {
                throw new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionListUser(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException,
            SearchIteratorException, OXException, AjaxException {
        timestamp = new Date(0);

        final SearchIterator<ContactObject> it = null;

        final JSONArray jsonResponseArray = new JSONArray();

        try {
            final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
            final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
            final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
            final int userIdArray[] = new int[jData.length()];
            for (int a = 0; a < userIdArray.length; a++) {
                userIdArray[a] = jData.getInt(a);
            }

            final int[] internalColumns = new int[columns.length + 1];
            System.arraycopy(columns, 0, internalColumns, 0, columns.length);
            internalColumns[columns.length] = DataObject.LAST_MODIFIED;

            Context ctx = null;
            try {
                ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
            } catch (final ContextException ct) {
                throw new ContactException(ct);
            }

            try {
                final ContactInterface contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
                final ContactWriter contactwriter = new ContactWriter(timeZone);

                for (int a = 0; a < userIdArray.length; a++) {
                    final ContactObject contactObj = contactInterface.getUserById(userIdArray[a]);
                    final JSONArray jsonContactArray = new JSONArray();
                    contactwriter.writeArray(contactObj, columns, jsonContactArray);
                    jsonResponseArray.put(jsonContactArray);

                    if (timestamp.before(contactObj.getLastModified())) {
                        timestamp = contactObj.getLastModified();
                    }
                }
            } catch (final JSONException e) {
                throw new OXException(new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]));
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);
        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);

        final int leftHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.LEFT_HAND_LIMIT);
        final int rightHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.RIGHT_HAND_LIMIT);

        timestamp = new Date(0);

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<ContactObject> it = null;

        try {
            final int[] internalColumns = new int[columnsToLoad.length + 1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            Context ctx = null;
            try {
                ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
            } catch (final ContextException ct) {
                throw new ContactException(ct);
            }

            ContactInterface contactInterface = ContactServices.getInstance().getService(folderId, ctx.getContextId());
            // ContactInterface contactInterface =
            // ContactServices.getInstance().getService(folderId);
            if (contactInterface == null) {
                contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
            }
            contactInterface.setSession(sessionObj);

            final ContactWriter contactwriter = new ContactWriter(timeZone);
            if (rightHandLimit == 0) {
                it = contactInterface.getContactsInFolder(folderId, leftHandLimit, 50000, orderBy, orderDir, internalColumns);
            } else {
                it = contactInterface.getContactsInFolder(folderId, leftHandLimit, rightHandLimit, orderBy, orderDir, internalColumns);
            }

            while (it.hasNext()) {
                final ContactObject contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactwriter.writeArray(contactObj, columns, jsonContactArray);
                jsonResponseArray.put(jsonContactArray);

                if (timestamp.before(contactObj.getLastModified())) {
                    timestamp = contactObj.getLastModified();
                }
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    private int[] removeVirtual(final int[] columns) {
        final List<Integer> helper = new ArrayList<Integer>(columns.length);
        for(final int col : columns) {
            if(col != ContactObject.LAST_MODIFIED_UTC) {
                helper.add(I(col));
            }
        }
        final int[] copy = new int[helper.size()];
        for(int i = 0; i < copy.length; i++) { copy[i] = helper.get(i).intValue(); }
        return copy;
    }

    public JSONObject actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException,
            OXJSONException, AjaxException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(inFolder, ctx.getContextId());
        // ContactInterface contactInterface =
        // ContactServices.getInstance().getService(inFolder);
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);

        timestamp = new Date(0);

        final ContactObject contactObj = contactInterface.getObjectById(id, inFolder);
        final ContactWriter contactwriter = new ContactWriter(timeZone);

        final JSONObject jsonResponseObject = new JSONObject();
        contactwriter.writeContact(contactObj, jsonResponseObject);

        timestamp = contactObj.getLastModified();

        return jsonResponseObject;
    }

    public JSONObject actionGetUser(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException,
            OXException, OXJSONException, AjaxException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        final ContactInterface contactInterface = new RdbContactSQLInterface(sessionObj, ctx);

        timestamp = new Date(0);

        final ContactObject contactObj = contactInterface.getUserById(id);
        final ContactWriter contactwriter = new ContactWriter(timeZone);

        final JSONObject jsonResponseObject = new JSONObject();
        contactwriter.writeContact(contactObj, jsonResponseObject);

        timestamp = contactObj.getLastModified();

        return jsonResponseObject;
    }

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);

        timestamp = new Date(0);

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
        final ContactSearchObject searchObj = new ContactSearchObject();
        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            searchObj.addFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
        }
        if (jData.has(SearchFields.PATTERN)) {
            searchObj.setPattern(DataParser.parseString(jData, SearchFields.PATTERN));
        }
        if (jData.has("startletter")) {
            searchObj.setStartLetter(DataParser.parseBoolean(jData, "startletter"));
        }
        if (jData.has("emailAutoComplete") && jData.getBoolean("emailAutoComplete")) {
            searchObj.setEmailAutoComplete(true);
        }
        if (jData.has("orSearch") && jData.getBoolean("orSearch")) {
            searchObj.setOrSearch(true);
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

        final int[] internalColumns = new int[columnsToLoad.length + 1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(searchObj.getFolder(), ctx.getContextId());
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);

        final SearchIterator<ContactObject> it =  contactInterface.getContactsByExtendedSearch(searchObj, orderBy, orderDir, internalColumns);
        final JSONArray jsonResponseArray = new JSONArray();
        try {
            final ContactWriter contactwriter = new ContactWriter(timeZone);
            while (it.hasNext()) {
                final ContactObject contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactwriter.writeArray(contactObj, columns, jsonContactArray);
                jsonResponseArray.put(jsonContactArray);

                if (timestamp.before(contactObj.getLastModified())) {
                    timestamp = contactObj.getLastModified();
                }
            }
        } finally {
            if (it != null) {
                it.close();
            }
        }
        return jsonResponseArray;
    }

    public JSONObject actionCopy(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException,
            OXException, OXJSONException, AjaxException {
        timestamp = new Date(0);

        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);

        Context ctx = null;
        try {
            ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }

        ContactInterface contactInterface = ContactServices.getInstance().getService(folderId, ctx.getContextId());
        // ContactInterface contactInterface =
        // ContactServices.getInstance().getService(folderId);
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(sessionObj, ctx);
        }
        contactInterface.setSession(sessionObj);

        final ContactObject contactObj = contactInterface.getObjectById(id, inFolder);
        final int origObjectId = contactObj.getObjectID();
        contactObj.removeObjectID();
        final int origFolderId = contactObj.getParentFolderID();
        contactObj.setParentFolderID(folderId);

        if (inFolder == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
            contactObj.removeInternalUserId();
        }

        contactObj.setNumberOfAttachments(0);

        contactInterface.insertContactObject(contactObj);

        final User user = UserStorage.getStorageUser(sessionObj.getUserId(), ctx);
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfiguration(
                sessionObj.getUserId(), ctx);
        /*
         * Check attachments
         */
        copyAttachments(folderId, ctx, contactObj, origObjectId, origFolderId, user, uc);
        /*
         * Check links
         */
        copyLinks(folderId, sessionObj, ctx, contactObj, origObjectId, origFolderId, user);

        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, contactObj.getObjectID());
        timestamp = contactObj.getLastModified();
        return jsonResponseObject;
    }

    private static void copyLinks(final int folderId, final Session session, final Context ctx,
            final ContactObject contactObj, final int origObjectId, final int origFolderId, final User user)
            throws OXException {
        /*
         * Get all
         */
        Connection readCon;
        try {
            readCon = Database.get(ctx, false);
        } catch (final DBPoolingException e) {
            throw new OXException(e);
        }
        final LinkObject[] links;
        try {
            links = Links.getAllLinksFromObject(origObjectId, Types.CONTACT, origFolderId, user.getId(), user
                    .getGroups(), session, readCon);
        } catch (final ContextException e) {
            throw new OXException(e);
        } finally {
            Database.back(ctx, false, readCon);
            readCon = null;
        }
        if (links == null || links.length == 0) {
            return;
        }
        /*
         * Copy
         */
        final Connection writeCon;
        try {
            writeCon = Database.get(ctx, true);
        } catch (final DBPoolingException e) {
            throw new OXException(e);
        }
        try {
            for (final LinkObject link : links) {
                final LinkObject copy;
                if (link.getFirstId() == origObjectId) {
                    copy = new LinkObject(contactObj.getObjectID(), Types.CONTACT, folderId, link.getSecondId(), link
                            .getSecondType(), link.getSecondFolder(), ctx.getContextId());
                } else if (link.getSecondId() == origObjectId) {
                    copy = new LinkObject(link.getFirstId(), link.getFirstType(), link.getFirstFolder(), contactObj
                            .getObjectID(), Types.CONTACT, folderId, ctx.getContextId());
                } else {
                    LOG.error("Invalid link retrieved from Links.getAllLinksFromObject()."
                            + " Neither first nor second ID matches!");
                    continue;
                }
                Links.performLinkStorage(copy, user.getId(), user.getGroups(), session, writeCon);
            }
        } catch (final ContextException e) {
            throw new OXException(e);
        } finally {
            Database.back(ctx, true, writeCon);
        }
    }

    private static void copyAttachments(final int folderId, final Context ctx, final ContactObject contactObj,
            final int origObjectId, final int origFolderId, final User user, final UserConfiguration uc)
            throws OXException {
        /*
         * Copy attachments
         */
        final AttachmentBase attachmentBase = Attachments.getInstance();
        final SearchIterator<?> iterator = attachmentBase.getAttachments(origFolderId, origObjectId, Types.CONTACT,
                ctx, user, uc).results();
        if (iterator.hasNext()) {
            try {
                attachmentBase.startTransaction();
                do {
                    final AttachmentMetadata orig = (AttachmentMetadata) iterator.next();
                    final AttachmentMetadata copy = new AttachmentImpl(orig);
                    copy.setFolderId(folderId);
                    copy.setAttachedId(contactObj.getObjectID());
                    copy.setId(AttachmentBase.NEW);
                    attachmentBase.attachToObject(copy, attachmentBase.getAttachedFile(origFolderId, origObjectId,
                            Types.CONTACT, orig.getId(), ctx, user, uc), ctx, user, uc);
                } while (iterator.hasNext());
                attachmentBase.commit();
            } catch (final SearchIteratorException e) {
                try {
                    attachmentBase.rollback();
                } catch (final TransactionException e1) {
                    LOG.error("Attachment transaction rollback failed", e);
                }
                throw new OXException(e);
            } catch (final OXException e) {
                try {
                    attachmentBase.rollback();
                } catch (final TransactionException e1) {
                    LOG.error("Attachment transaction rollback failed", e);
                }
                throw e;
            } finally {
                try {
                    iterator.close();
                } catch (final SearchIteratorException e) {
                    LOG.error("SearchIterator could not be closed", e);
                }
                try {
                    attachmentBase.finish();
                } catch (final TransactionException e) {
                    LOG.error("Attachment transaction finish failed", e);
                }
            }
        }
    }
}
