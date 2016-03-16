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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.messaging.json.actions.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.json.ManagedFileInputStreamRegistry;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.cacheing.CacheingMessageAccess;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Represents a request to the messaging subsystem. The class contains common parsing methods for arguments.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingRequestData {

    private final AJAXRequestData request;

    private final MessagingServiceRegistry registry;

    private final ServerSession session;

    private final MessagingMessageParser parser;

    private MessagingAccountAccess accountAccess;

    private final Cache cache;

    private MessagingMessageAccess messageAccess;

    private final Collection<MessagingAccountAccess> closeables = new LinkedList<MessagingAccountAccess>();

    public MessagingRequestData(final AJAXRequestData request, final ServerSession session, final MessagingServiceRegistry registry, final MessagingMessageParser parser, final Cache cache) {
        super();
        this.request = request;
        this.registry = registry;
        this.session = session;
        this.parser = parser;
        this.cache = cache;
    }

    public MessagingRequestData(final AJAXRequestData request, final ServerSession session, final MessagingServiceRegistry registry, final MessagingMessageParser parser) {
        this(request, session, registry, parser, null);
    }

    public MessagingMessageAccess getMessageAccess(final String messagingService, final int account, final int user, final int cid) throws OXException {
        final MessagingAccountAccess access = registry.getMessagingService(messagingService, user, cid).getAccountAccess(account, session);
        if (!access.isConnected()) {
            access.connect();
            mustClose(access);
        }
        return wrap(access.getMessageAccess(), messagingService, account);
    }

    private void mustClose(final MessagingAccountAccess access) {
        closeables.add(access);
    }

    /**
     * Tries to get a message access for the messaging service and account ID as given in the request parameters
     * @param user
     * @param cid
     *
     * @throws OXException If parameters 'messagingService' or 'account' are missing
     */
    public MessagingMessageAccess getMessageAccess(final int user, final int cid) throws OXException {
        if (messageAccess != null) {
            return messageAccess;
        }
        final MessagingMessageAccess access = getAccountAccess(user, cid).getMessageAccess();
        return messageAccess = wrap(access, getMessagingServiceId(), getAccountID());
    }

    public MessagingAccountAccess getAccountAccess(final int user, final int cid) throws OXException {
        if (accountAccess != null) {
            return accountAccess;
        }
        accountAccess = registry.getMessagingService(getMessagingServiceId(), user, cid).getAccountAccess(getAccountID(), session);

        if (!accountAccess.isConnected()) {
            accountAccess.connect();
            mustClose(accountAccess);
        }

        return accountAccess;
    }

    public String getMessagingServiceId() throws OXException {
        if (hasLongFolder()) {
            return getLongFolder().getMessagingService();
        } else {
            if (isset("messagingService")) {
                return request.getParameter("messagingService");
            }
            missingParameter("folder");
            return null;
        }
    }

    private void missingParameter(final String string) throws OXException {
        throw MessagingExceptionCodes.MISSING_PARAMETER.create(string);
    }

    /**
     * Tries to retrieve the value of a given parameter, failing with a OXException if the parameter was not sent.
     */
    public String requireParameter(final String string) throws OXException {
        final String parameter = request.getParameter(string);
        if (parameter == null) {
            missingParameter(string);
        }
        return parameter;
    }

    /**
     * Reads and parses the 'account' parameter.
     *
     * @throws OXException - When the 'account' parameter was not set or is not a valid integer.
     */
    public int getAccountID() throws OXException {
        if (!isset("account") && hasLongFolder()) {
            return getLongFolder().getAccount();
        }

        final String parameter = requireParameter("account");
        try {
            return Integer.parseInt(parameter);
        } catch (final NumberFormatException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("account", parameter);
        }
    }

    /**
     * Reads the 'folder' parameter, failing when it is not set.
     *
     * @throws OXException - When the 'folder' parameter is not set.
     */
    public String getFolderId() throws OXException {
        if (hasLongFolder()) {
            return getLongFolder().getFolder();
        }
        return requireParameter("folder");
    }

    /**
     * Reads and parses the 'columns' parameter. Fails when 'columns' is not set or if it contains an unknown value. Columns are a string
     * separated list of MessagingField names.
     *
     * @return An array of MessagingFields corresponding to the comma-separated list given in the 'columns' parameter.
     * @throws OXException - When the 'columns' parameter was not set or contains an illegal value.
     */
    public MessagingField[] getColumns() throws OXException {
        final String parameter = requireParameter("columns");
        if (parameter == null) {
            return new MessagingField[0];
        }

        final String[] columnList = parameter.split("\\s*,\\s*");
        final MessagingField[] fields = MessagingField.getFields(columnList);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null) {
                throw MessagingExceptionCodes.INVALID_PARAMETER.create("columns", columnList[i]);
            }
        }
        return fields;
    }

    /**
     * Retrieves and parses the 'sort' parameter, turning it into a MessagingField. Returns <code>null</code> when 'sort' is unset. Fails
     * when 'sort' contains an unknown MessagingField.
     *
     * @throws OXException - When the 'sort' parameter contains an illegal value.
     */
    public MessagingField getSort() throws OXException {
        final String parameter = request.getParameter("sort");
        if (parameter == null) {
            return null;
        }
        final MessagingField field = MessagingField.getField(parameter);
        if (field == null) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("sort", parameter);
        }
        return field;
    }

    /**
     * Retrieves and parses the 'order' parameter. Returns <code>null</code> when 'order' is not set. Fails when 'order' contains neither
     * 'desc' and 'asc'. Matches case-insensitively.
     *
     * @throws OXException - When 'order' contains an illegal value.
     */
    public OrderDirection getOrder() throws OXException {
        final String parameter = request.getParameter("order");
        if (parameter == null) {
            return null;
        }
        try {
            return Enum.valueOf(OrderDirection.class, parameter.toUpperCase());
        } catch (final IllegalArgumentException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("order", parameter);
        }
    }

    /**
     * Retrieves the given 'id' parameter. Fails when the 'id' parameter is unset.
     *
     * @throws OXException - When the 'id' parameter is unset.
     */
    public String getId() throws OXException {
        return requireParameter("id");
    }

    /**
     * Retrieves and parses the 'peek' parameter. Returns 'false' when 'peek' is not set. Fails when 'peek' contains neither 'true' nor
     * 'false'. Matches case insensitively.
     *
     * @throws OXException - When 'peek' contains an illegal value.
     */
    public boolean getPeek() throws OXException {
        final String parameter = request.getParameter("peek");
        if (parameter == null) {
            return false;
        }
        if (parameter.equalsIgnoreCase("true")) {
            return true;
        } else if (parameter.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("peek", parameter);
        }
    }

    /**
     * Retrieves the 'messageAction' parameter. Fails when 'messageAction' was not set.
     *
     * @return
     * @throws OXException - When 'messageAction' was not set.
     */
    public String getMessageAction() throws OXException {
        return requireParameter("messageAction");
    }

    public MessagingMessage getMessage() throws OXException, JSONException, IOException {
        final Object data = request.getData();
        if (data == null) {
            return null;
        }
        if (!JSONObject.class.isInstance(data)) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("body", data.toString());
        }
        return parser.parse((JSONObject) data, ManagedFileInputStreamRegistry.getInstance(), request.getRemoteAddress());
    }

    /**
     * Determines if the given parameters were set in the request.
     */
    public boolean isset(final String... params) {
        for (final String param : params) {
            if (null == request.getParameter(param)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the MessagingAccountTransport that matches the parameters 'messagingService' and 'account'. Fails when either of the
     * parameters has not been set.
     * @param user
     * @param cid
     */
    public MessagingAccountTransport getTransport(final int user, final int cid) throws OXException {
        return registry.getMessagingService(getMessagingServiceId(), user, cid).getAccountTransport(getAccountID(), session);
    }

    /**
     * Retrieves and parses the 'recipients' parameter. May return null, if no recipients were set.
     *
     * @throws OXException
     */
    public Collection<MessagingAddressHeader> getRecipients() throws OXException {
        final String parameter = request.getParameter("recipients");
        if (parameter == null) {
            return null;
        }
        return new ArrayList<MessagingAddressHeader>(MimeAddressMessagingHeader.parseRFC822("", parameter));
    }

    /**
     * Tries to either parse the folder in its long form or assemble it from the content
     *
     * @throws OXException
     */
    public MessagingFolderAddress getLongFolder() throws OXException {
        if (hasLongFolder()) {
            return MessagingFolderAddress.parse(request.getParameter("folder"));
        } else if (isset("messagingService", "account", "folder")) {
            final MessagingFolderAddress address = new MessagingFolderAddress();
            address.setMessagingService(getMessagingServiceId());
            address.setAccount(getAccountID());
            address.setFolder(getFolderId());
            return address;
        }
        return null;
    }

    private boolean hasLongFolder() throws OXException {
        return isset("folder") && MessagingFolderAddress.matches(request.getParameter("folder"));
    }

    public List<MessageAddress> getMessageAddresses() throws JSONException, OXException {
        final Object data = request.getData();
        if (data == null) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create("body");
        }
        if (!JSONArray.class.isInstance(data)) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("body", data.toString());
        }
        final JSONArray idsJSON = (JSONArray) data;

        final List<MessageAddress> addresses = new ArrayList<MessageAddress>(idsJSON.length());

        for (int i = 0, size = idsJSON.length(); i < size; i++) {
            final JSONObject pair = idsJSON.getJSONObject(i);
            addresses.add(new MessageAddress(pair.getString("folder"), pair.getString("id")));
        }

        return addresses;
    }

    public String getAccountAddress() throws OXException {
        return getMessagingServiceId() + "://" + getAccountID();
    }

    public MessagingMessageAccess wrap(final MessagingMessageAccess messageAccess2, final String service, final int account) {
        if (cache == null) {
            return messageAccess2;
        }
        return new CacheingMessageAccess(messageAccess2, cache, new StringBuilder(service).append("://").append(account).toString(), session);
    }

    public void cleanUp() {
        for (final MessagingAccountAccess closeable : closeables) {
            closeable.close();
        }
    }

    public String getReferenceId() throws OXException {
        return requireParameter("referenceId");
    }

}
