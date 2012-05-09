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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.FinalContactConstants;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api2.FinalContactInterface;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactSearchMultiplexer;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.links.Links;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactRequest} - Handles AJAX requests for contact module.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactRequest {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactRequest.class));

    public static final String ACTION_GET_USER = "getuser";

    public static final String ACTION_LIST_USER = "listuser";

    /*-
     * ++++++++++++++++++++++++++++++ Member section ++++++++++++++++++++++++++++++
     */

    private final ServerSession session;

    private final TimeZone timeZone;

    private Date timestamp;

    /**
     * Gets the time stamp.
     *
     * @return The time stamp
     */
    public Date getTimestamp() {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    /**
     * Initializes a new {@link ContactRequest}.
     *
     * @param session The session
     */
    public ContactRequest(final ServerSession session) {
        this.session = session;

        final String sTimeZone = session.getUser().getTimeZone();

        timeZone = TimeZoneUtils.getTimeZone(sTimeZone);
        if (LOG.isDebugEnabled()) {
            LOG.debug("use timezone string: " + sTimeZone);
            LOG.debug("use user timezone: " + timeZone);
        }
    }

    public JSONValue action(final String action, final JSONObject jsonObject) throws JSONException, OXException {
        if (!session.getUserConfiguration().hasContact()) {
            throw OXException.noPermissionForModule("contact");
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
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_TERMSEARCH)) {
            return actionTermSearch(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
            return actionCopy(jsonObject);
        } else if (action.equalsIgnoreCase(FinalContactConstants.ACTION_ASSOCIATE.getName())) {
            return actionAssociate(jsonObject);
        } else if (action.equalsIgnoreCase(FinalContactConstants.ACTION_DISSOCIATE.getName())) {
            return actionDissociate(jsonObject);
        } else if (action.equalsIgnoreCase(FinalContactConstants.ACTION_GET_ASSOCIATED.getName())) {
            return actionGetAssociated(jsonObject);
        } else if (action.equalsIgnoreCase(FinalContactConstants.ACTION_GET_ASSOCIATION.getName())) {
            return actionGetAssociation(jsonObject);
        } else if (action.equalsIgnoreCase(FinalContactConstants.ACTION_GET_BY_UUID.getName())) {
            return actionGetByUuid(jsonObject);
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
    }



	public JSONValue actionGetByUuid(final JSONObject jsonObj) throws JSONException, OXException {
        final FinalContactInterface contactInterface = getFinalContactInterface();

        final Contact contactObj = getFinalContact(contactInterface, jsonObj);

        final TimeZone tz;
        final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
        tz = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);

        final ContactWriter contactwriter = new ContactWriter(tz);

        final JSONObject jsonResponseObject = new JSONObject();
        contactwriter.writeContact(contactObj, jsonResponseObject, session);

        timestamp = contactObj.getLastModified();

        return jsonResponseObject;
    }

    public JSONValue actionGetAssociation(final JSONObject jsonObject) throws JSONException, OXException {
        final FinalContactInterface contactInterface = getFinalContactInterface();
        final Contact[] twoContacts = getTwoFinalContacts(contactInterface, jsonObject);

        final ContactUnificationState association = contactInterface.getAssociationBetween(twoContacts[0], twoContacts[1]);
        final JSONObject ret = new JSONObject().put("state", association.getNumber());
        return ret;
    }

    private JSONValue actionGetAssociated(final JSONObject jsonObject) throws JSONException, OXException {
        final FinalContactInterface contactInterface = getFinalContactInterface();
        final Contact contact  = getFinalContact(contactInterface, jsonObject);

        final TimeZone tz;
        final String timeZoneId = DataParser.parseString(jsonObject, AJAXServlet.PARAMETER_TIMEZONE);
        tz = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);

        final List<UUID> associatedContacts = contactInterface.getAssociatedContacts(contact);

        final JSONArray ret = new JSONArray();

        for(final UUID associate: associatedContacts){
            ret.put(associate.toString());
        }

        return ret;
    }

    private JSONValue actionDissociate(final JSONObject jsonObject) throws OXException {
        final FinalContactInterface contactInterface = getFinalContactInterface();
        final Contact[] twoContacts = getTwoFinalContacts(contactInterface, jsonObject);

        contactInterface.separateTwoContacts(twoContacts[0], twoContacts[1]);

        return null;
    }

    private JSONValue actionAssociate(final JSONObject jsonObject) throws OXException {
        final FinalContactInterface contactInterface = getFinalContactInterface();
        final Contact[] twoContacts = getTwoFinalContacts(contactInterface, jsonObject);

        contactInterface.associateTwoContacts(twoContacts[0], twoContacts[1]);

        return null;
    }

    public JSONObject actionNew(final JSONObject jsonObj) throws JSONException, OXException {

        final Contact contactObj = new Contact();
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final ContactParser contactparser = new ContactParser();
        contactparser.parse(contactObj, jData);

        if (!contactObj.containsParentFolderID()) {
            throw OXException.mandatoryField("missing folder");
        }

        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
            contactObj.getParentFolderID(),
            session);

        contactInterface.insertContactObject(contactObj);
        timestamp = contactObj.getLastModified();
        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, contactObj.getObjectID());

        return jsonResponseObject;
    }

    public JSONObject actionUpdate(final JSONObject jsonObj) throws JSONException, OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);

        final Contact contactobject = new Contact();
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final ContactParser contactparser = new ContactParser();
        contactparser.parse(contactobject, jData);

        contactobject.setObjectID(id);

        final ContactInterfaceDiscoveryService discoveryService = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class);
        final ContactInterface contactIface = discoveryService.newContactInterface(
            inFolder,
            session);

        if (contactobject.containsParentFolderID() && contactobject.getParentFolderID() > 0) {
            /*
             * A move to another folder
             */
            final int folderId = contactobject.getParentFolderID();
            final ContactInterface targetContactIface = discoveryService.newContactInterface(folderId, session);
            if (!contactIface.getClass().equals(targetContactIface.getClass())) {
                final Contact toMove = move2AnotherProvider(id, inFolder, contactobject, contactIface, folderId, targetContactIface, session, timestamp);
                timestamp = toMove.getLastModified();
                return new JSONObject();
            }
        }

        contactIface.updateContactObject(contactobject, inFolder, timestamp);
        timestamp = contactobject.getLastModified();
        return new JSONObject();
    }

    public static Contact move2AnotherProvider(final int id, final int inFolder, final Contact contact, final ContactInterface contactIface, final int newFolderId, final ContactInterface targetContactIface, final ServerSession session, final Date timestamp) throws OXException {
        /*
         * A move to another contact service
         */
        final Contact toMove = contactIface.getObjectById(id, inFolder);
        for (int i = 1; i < Contacts.mapping.length; i++) {
            if (null != Contacts.mapping[i]) {
                if (contact.contains(i)) {
                    toMove.set(i, contact.get(i));
                }
            }
        }
        if (inFolder == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
            toMove.removeInternalUserId();
        }
        toMove.setNumberOfAttachments(0);
        targetContactIface.insertContactObject(toMove);

        final User user = session.getUser();
        final UserConfiguration uc = session.getUserConfiguration();
        /*
         * Check attachments
         */
        copyAttachments(newFolderId, session, session.getContext(), toMove, id, inFolder, user, uc);
        /*
         * Check links
         */
        copyLinks(newFolderId, session, session.getContext(), toMove, id, inFolder, user);
        /*
         * Delete original
         */
        contactIface.deleteContactObject(id, inFolder, timestamp);
        return toMove;
    }

    public JSONArray actionUpdates(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final Date requestedTimestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        timestamp = new Date(requestedTimestamp.getTime());

        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);

        if (ignore == null) {
            ignore = "deleted";
        }

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<Contact> it = null;

        try {
            boolean bIgnoreDelete = false;

            if (ignore.indexOf("deleted") != -1) {
                bIgnoreDelete = true;
            }

            final int[] internalColumns = checkLastModified(columnsToLoad);

            final ContactWriter contactWriter = new ContactWriter(timeZone);

            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
                folderId,
                session);

            it = contactInterface.getModifiedContactsInFolder(folderId, internalColumns, requestedTimestamp);
            while (it.hasNext()) {
                final Contact contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactWriter.writeArray(contactObj, columns, jsonContactArray, session);
                jsonResponseArray.put(jsonContactArray);

                final Date clm = contactObj.getLastModified();
                if (null != clm && timestamp.before(clm)) {
                    timestamp = clm;
                }
            }

            if (!bIgnoreDelete) {
                it = contactInterface.getDeletedContactsInFolder(folderId, internalColumns, requestedTimestamp);
                while (it.hasNext()) {
                    final Contact contactObj = it.next();

                    jsonResponseArray.put(contactObj.getObjectID());

                    final Date clm = contactObj.getLastModified();
                    if (null != clm && timestamp.before(clm)) {
                        timestamp = clm;
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

    public JSONArray actionDelete(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final int objectId = DataParser.checkInt(jData, DataFields.ID);
        final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);

        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
            inFolder,
            session);

        contactInterface.deleteContactObject(objectId, inFolder, timestamp);

        return new JSONArray();
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        Date lastModified = null;

        SearchIterator<Contact> it = null;

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
            final TimeZone timeZone;
            {
                final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
                timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
            }

            final int[] internalColumns = checkLastModified(columnsToLoad);

            try {
                // check if the int array has always the same folder id
                if (isOneFolder) {

                    final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                        ContactInterfaceDiscoveryService.class).newContactInterface(oldfolderId, session);

                    final ContactWriter contactwriter = new ContactWriter(timeZone);

                    it = contactInterface.getObjectsById(objectIdAndFolderId, internalColumns);

                    while (it.hasNext()) {
                        final Contact contactObj = it.next();
                        final JSONArray jsonContactArray = new JSONArray();
                        contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
                        jsonResponseArray.put(jsonContactArray);

                        final Date clm = contactObj.getLastModified();
                        if (clm != null && timestamp.before(clm)) {
                            timestamp = clm;
                        }
                    }
                } else {
                    // costs more performance because every object in the array
                    // is checked
                    for (int a = 0; a < objectIdAndFolderId.length; a++) {
                        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                            ContactInterfaceDiscoveryService.class).newContactInterface(objectIdAndFolderId[a][1], session);

                        final ContactWriter contactwriter = new ContactWriter(timeZone);

                        final int[][] newObjectIdAndFolderId = { { objectIdAndFolderId[a][0], objectIdAndFolderId[a][1] } };
                        it = contactInterface.getObjectsById(newObjectIdAndFolderId, internalColumns);

                        while (it.hasNext()) {
                            final Contact contactObj = it.next();
                            final JSONArray jsonContactArray = new JSONArray();
                            contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
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
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionListUser(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        final JSONArray jsonResponseArray = new JSONArray();

        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int userIdArray[] = new int[jData.length()];
        for (int a = 0; a < userIdArray.length; a++) {
            userIdArray[a] = jData.getInt(a);
        }
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final Context ctx = session.getContext();

        try {
            // TODO: Use discovery service?
            final ContactInterface contactInterface = new RdbContactSQLImpl(session, ctx);
            final ContactWriter contactwriter = new ContactWriter(timeZone);

            for (int a = 0; a < userIdArray.length; a++) {
                final Contact contactObj = contactInterface.getUserById(userIdArray[a]);
                final JSONArray jsonContactArray = new JSONArray();
                contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
                jsonResponseArray.put(jsonContactArray);

                final Date clm = contactObj.getLastModified();
                if (null != clm && timestamp.before(clm)) {
                    timestamp = clm;
                }
            }
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
        }

        return jsonResponseArray;
    }

    public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);
        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final Order order = OrderFields.parse(DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER));
        final String collation = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_COLLATION);

        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final int leftHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.LEFT_HAND_LIMIT);
        final int rightHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.RIGHT_HAND_LIMIT);

        timestamp = new Date(0);

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<Contact> it = null;

        try {
            final int[] internalColumns = checkLastModified(columnsToLoad);

            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
                folderId,
                session);

            final ContactWriter contactwriter = new ContactWriter(timeZone);
            it = contactInterface.getContactsInFolder(folderId, leftHandLimit, rightHandLimit, orderBy, order, collation, internalColumns);

            while (it.hasNext()) {
                final Contact contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
                jsonResponseArray.put(jsonContactArray);

                final Date clm = contactObj.getLastModified();
                if (null != clm && timestamp.before(clm)) {
                    timestamp = clm;
                }
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    // Removes virtual columns or exchanges them agains real columns
    private int[] removeVirtual(final int[] columns) {
        final TIntList helper = new TIntArrayList(columns.length);
        for (final int col : columns) {
            if (col == Contact.LAST_MODIFIED_UTC) {
                // SKIP
            } else if (col == Contact.IMAGE1_URL) {
                helper.add(Contact.IMAGE1);
            } else {
                helper.add(col);
            }

        }
        return helper.toArray();
    }

    public JSONObject actionGet(final JSONObject jsonObj) throws JSONException, OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
            inFolder,
            session);

        timestamp = new Date(0);

        final Contact contactObj = contactInterface.getObjectById(id, inFolder);
        final ContactWriter contactwriter = new ContactWriter(timeZone);

        final JSONObject jsonResponseObject = new JSONObject();
        contactwriter.writeContact(contactObj, jsonResponseObject, session);

        timestamp = contactObj.getLastModified();

        return jsonResponseObject;
    }

    public JSONObject actionGetUser(final JSONObject jsonObj) throws JSONException, OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final Context ctx = session.getContext();

        // TODO: Use discovery service?
        final ContactInterface contactInterface = new RdbContactSQLImpl(session, ctx);

        timestamp = new Date(0);

        final Contact contactObj = contactInterface.getUserById(id);
        final ContactWriter contactwriter = new ContactWriter(timeZone);

        final JSONObject jsonResponseObject = new JSONObject();
        contactwriter.writeContact(contactObj, jsonResponseObject, session);

        timestamp = contactObj.getLastModified();

        return jsonResponseObject;
    }

    public JSONArray actionTermSearch(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

    	final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
    	final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
    	final int[] columnsToLoad = removeVirtual(columns);
        final int[] internalColumns = checkLastModified(columnsToLoad);

        final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
        final TimeZone timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final Order order = OrderFields.parse(DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER));
        final String collation = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_COLLATION);

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
        final JSONArray filterContent = jData.getJSONArray("filter");
        final SearchTerm<?> searchTerm = SearchTermParser.parse(filterContent);

        final ContactSearchMultiplexer multiplexer = new ContactSearchMultiplexer(ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class));
        final SearchIterator<Contact> it = multiplexer.extendedSearch(session, searchTerm, orderBy, order, collation, internalColumns);

        final JSONArray jsonResponseArray = new JSONArray();
        try {
            final ContactWriter contactwriter = new ContactWriter(timeZone);
            while (it.hasNext()) {
                final Contact contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();

                contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
                jsonResponseArray.put(jsonContactArray);

                final Date clm = contactObj.getLastModified();
                if (null != clm && timestamp.before(clm)) {
                    timestamp = clm;
                }
            }
        } finally {
            if (it != null) {
                it.close();
            }
        }
        return jsonResponseArray;
    }

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(" *, *");
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtual(columns);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        timestamp = new Date(0);

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
        final ContactSearchObject searchObj = new ContactSearchObject();
        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            if (jData.get(AJAXServlet.PARAMETER_INFOLDER).getClass().equals(JSONArray.class)) {
                for (final int folder : DataParser.parseJSONIntArray(jData, AJAXServlet.PARAMETER_INFOLDER)) {
                    searchObj.addFolder(folder);
                }
            } else {
                searchObj.addFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
            }
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
        final Order order = OrderFields.parse(DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER));
        final String collation = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_COLLATION);

        searchObj.setSurname(DataParser.parseString(jData, ContactFields.LAST_NAME));
        searchObj.setDisplayName(DataParser.parseString(jData, ContactFields.DISPLAY_NAME));
        searchObj.setGivenName(DataParser.parseString(jData, ContactFields.FIRST_NAME));
        searchObj.setCompany(DataParser.parseString(jData, ContactFields.COMPANY));
        searchObj.setEmail1(DataParser.parseString(jData, ContactFields.EMAIL1));
        searchObj.setEmail2(DataParser.parseString(jData, ContactFields.EMAIL2));
        searchObj.setEmail3(DataParser.parseString(jData, ContactFields.EMAIL3));
        searchObj.setDepartment(DataParser.parseString(jData, ContactFields.DEPARTMENT));
        searchObj.setStreetBusiness(DataParser.parseString(jData, ContactFields.STREET_BUSINESS));
        searchObj.setCityBusiness(DataParser.parseString(jData, ContactFields.CITY_BUSINESS));
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
        searchObj.setYomiCompany(DataParser.parseString(jData, ContactFields.YOMI_COMPANY));
        searchObj.setYomiFirstname(DataParser.parseString(jData, ContactFields.YOMI_FIRST_NAME));
        searchObj.setYomiLastName(DataParser.parseString(jData, ContactFields.YOMI_LAST_NAME));

        final int[] internalColumns = checkLastModified(columnsToLoad);

        final ContactSearchMultiplexer multiplexer = new ContactSearchMultiplexer(ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class));
        final SearchIterator<Contact> it = multiplexer.extendedSearch(session, searchObj, orderBy, order, collation, internalColumns);

        final JSONArray jsonResponseArray = new JSONArray();
        try {
            final ContactWriter contactwriter = new ContactWriter(timeZone);
            while (it.hasNext()) {
                final Contact contactObj = it.next();
                final JSONArray jsonContactArray = new JSONArray();
                contactwriter.writeArray(contactObj, columns, jsonContactArray, session);
                jsonResponseArray.put(jsonContactArray);

                final Date clm = contactObj.getLastModified();
                if (null != clm && timestamp.before(clm)) {
                    timestamp = clm;
                }
            }
        } finally {
            if (it != null) {
                it.close();
            }
        }
        return jsonResponseArray;
    }

    public JSONObject actionCopy(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);

        final Context ctx = session.getContext();

        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
            folderId,
            session);

        final Contact contactObj = contactInterface.getObjectById(id, inFolder);
        final int origObjectId = contactObj.getObjectID();
        contactObj.removeObjectID();
        final int origFolderId = contactObj.getParentFolderID();
        contactObj.setParentFolderID(folderId);

        if (inFolder == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
            contactObj.removeInternalUserId();
        }

        contactObj.setNumberOfAttachments(0);

        contactInterface.insertContactObject(contactObj);

        final User user = session.getUser();
        final UserConfiguration uc = session.getUserConfiguration();
        /*
         * Check attachments
         */
        copyAttachments(folderId, session, ctx, contactObj, origObjectId, origFolderId, user, uc);
        /*
         * Check links
         */
        copyLinks(folderId, session, ctx, contactObj, origObjectId, origFolderId, user);

        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, contactObj.getObjectID());
        timestamp = contactObj.getLastModified();
        return jsonResponseObject;
    }



    private static void copyLinks(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user) throws OXException {
        /*
         * Get all
         */
        Connection readCon = Database.get(ctx, false);
        final LinkObject[] links;
        try {
            links = Links.getAllLinksFromObject(origObjectId, Types.CONTACT, origFolderId, user.getId(), user.getGroups(), session, readCon);
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
        final Connection writeCon = Database.get(ctx, true);
        try {
            for (final LinkObject link : links) {
                final LinkObject copy;
                if (link.getFirstId() == origObjectId) {
                    copy = new LinkObject(
                        contactObj.getObjectID(),
                        Types.CONTACT,
                        folderId,
                        link.getSecondId(),
                        link.getSecondType(),
                        link.getSecondFolder(),
                        ctx.getContextId());
                } else if (link.getSecondId() == origObjectId) {
                    copy = new LinkObject(
                        link.getFirstId(),
                        link.getFirstType(),
                        link.getFirstFolder(),
                        contactObj.getObjectID(),
                        Types.CONTACT,
                        folderId,
                        ctx.getContextId());
                } else {
                    LOG.error("Invalid link retrieved from Links.getAllLinksFromObject()." + " Neither first nor second ID matches!");
                    continue;
                }
                Links.performLinkStorage(copy, user.getId(), user.getGroups(), session, writeCon);
            }
        } finally {
            Database.back(ctx, true, writeCon);
        }
    }

    private static void copyAttachments(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user, final UserConfiguration uc) throws OXException {
        /*
         * Copy attachments
         */
        final AttachmentBase attachmentBase = Attachments.getInstance();
        final SearchIterator<?> iterator = attachmentBase.getAttachments(origFolderId, origObjectId, Types.CONTACT, ctx, user, uc).results();
        if (iterator.hasNext()) {
            try {
                attachmentBase.startTransaction();
                do {
                    final AttachmentMetadata orig = (AttachmentMetadata) iterator.next();
                    final AttachmentMetadata copy = new AttachmentImpl(orig);
                    copy.setFolderId(folderId);
                    copy.setAttachedId(contactObj.getObjectID());
                    copy.setId(AttachmentBase.NEW);
                    attachmentBase.attachToObject(copy, attachmentBase.getAttachedFile(
                        origFolderId,
                        origObjectId,
                        Types.CONTACT,
                        orig.getId(),
                        ctx,
                        user,
                        uc), session, ctx, user, uc);
                } while (iterator.hasNext());
                attachmentBase.commit();
            } catch (final SearchIteratorException e) {
                try {
                    attachmentBase.rollback();
                } catch (final OXException e1) {
                    LOG.error("Attachment transaction rollback failed", e1);
                }
                throw new OXException(e);
            } catch (final OXException e) {
                try {
                    attachmentBase.rollback();
                } catch (final OXException e1) {
                    LOG.error("Attachment transaction rollback failed", e1);
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
                } catch (final OXException e) {
                    LOG.error("Attachment transaction finish failed", e);
                }
            }
        }
    }

    /**
     * Ensure last-modified field {@link DataObject#LAST_MODIFIED} is contained in specified columns.
     *
     * @param columns The columns to check
     * @return Either specified columns if last-modified field is already contained or specified columns extended by last-modified field
     */
    private static int[] checkLastModified(final int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == DataObject.LAST_MODIFIED) {
                // Last-Modified is already contained in requested columns
                return columns;
            }
        }
        // Add last-modified to requested columns
        final int[] internalColumns = new int[columns.length + 1];
        System.arraycopy(columns, 0, internalColumns, 0, columns.length);
        internalColumns[columns.length] = DataObject.LAST_MODIFIED;
        return internalColumns;
    }


    protected FinalContactInterface getFinalContactInterface() throws OXException, OXException {
        final ContactInterface contactInterfaceTemp = ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).newContactInterface(
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            session);
        if(! (contactInterfaceTemp instanceof FinalContactInterface)) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( FinalContactConstants.ACTION_GET_BY_UUID.getName());
        }
        return (FinalContactInterface) contactInterfaceTemp;
    }

    protected Contact[] getTwoFinalContacts(final FinalContactInterface contactInterface, final JSONObject jsonObject) throws OXException, OXException, OXException{
        final UUID uuid1 = DataParser.parseUUID(jsonObject, FinalContactConstants.PARAMETER_UUID1.getName());
        final UUID uuid2 = DataParser.parseUUID(jsonObject, FinalContactConstants.PARAMETER_UUID2.getName());
        Contact c1, c2;
        if( uuid1 == null){
            final int fid1 = DataParser.checkInt(jsonObject, FinalContactConstants.PARAMETER_FOLDER_ID1.getName());
            final int id1 =  DataParser.checkInt(jsonObject, FinalContactConstants.PARAMETER_CONTACT_ID1.getName());
            c1 = contactInterface.getObjectById(id1, fid1);

        } else {
            c1 = contactInterface.getContactByUUID(uuid1);
        }
        if( uuid2 == null){
            final int fid2 = DataParser.checkInt(jsonObject, FinalContactConstants.PARAMETER_FOLDER_ID2.getName());
            final int id2 =  DataParser.checkInt(jsonObject, FinalContactConstants.PARAMETER_CONTACT_ID2.getName());
            c2 = contactInterface.getObjectById(id2, fid2);
        } else {
            c2 = contactInterface.getContactByUUID(uuid2);
        }
        return new Contact[]{c1,c2};
    }

    protected Contact getFinalContact(final FinalContactInterface contactInterface, final JSONObject jsonObject) throws OXException, OXException, OXException{
        final UUID uuid = DataParser.parseUUID(jsonObject, FinalContactConstants.PARAMETER_UUID.getName());
        Contact c;
        if( uuid == null){
            final int fid = DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_FOLDERID);
            final int id =  DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_ID);
            c = contactInterface.getObjectById(id, fid);

        } else {
            c = contactInterface.getContactByUUID(uuid);
        }
        return c;
    }
}
