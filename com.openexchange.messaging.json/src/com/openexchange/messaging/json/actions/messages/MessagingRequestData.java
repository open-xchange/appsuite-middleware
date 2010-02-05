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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeMessagingMessage;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Represents a request to the messaging subsystem. The class contains common parsing methods for arguments.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingRequestData {

    private AJAXRequestData request;

    private MessagingServiceRegistry registry;

    private ServerSession session;

    private MessagingMessageParser parser;

    public MessagingRequestData(AJAXRequestData request, ServerSession session, MessagingServiceRegistry registry, MessagingMessageParser parser) {
        this.request = request;
        this.registry = registry;
        this.session = session;
        this.parser = parser;
    }

    /**
     * Tries to get a message access for the messaging service and account ID as given in the request parameters
     * 
     * @throws MessagingException If parameters 'messagingService' or 'account' are missing
     */
    public MessagingMessageAccess getMessageAccess() throws MessagingException {
        return registry.getMessagingService(getMessagingServiceId()).getAccountAccess(getAccountID(), session).getMessageAccess();
    }

    String getMessagingServiceId() throws MessagingException {
        if (!isset("messagingService") && hasLongFolder()) {
            return getLongFolder().getMessagingService();
        }
        return requireParameter("messagingService");
    }

    /**
     * Tries to retrieve the value of a given parameter, failing with a MessagingException if the parameter was not sent.
     */
    public String requireParameter(String string) throws MessagingException {
        String parameter = request.getParameter(string);
        if (parameter == null) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create(string);
        }
        return parameter;
    }

    /**
     * Reads and parses the 'account' parameter.
     * 
     * @throws MessagingException - When the 'account' parameter was not set or is not a valid integer.
     */
    public int getAccountID() throws MessagingException {
        if (!isset("account") && hasLongFolder()) {
            return getLongFolder().getAccount();
        }

        String parameter = requireParameter("account");
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("account", parameter);
        }
    }

    /**
     * Reads the 'folder' parameter, failing when it is not set.
     * 
     * @throws MessagingException - When the 'folder' parameter is not set.
     */
    public String getFolderId() throws MessagingException {
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
     * @throws MessagingException - When the 'columns' parameter was not set or contains an illegal value.
     */
    public MessagingField[] getColumns() throws MessagingException {
        String parameter = requireParameter("columns");
        if (parameter == null) {
            return new MessagingField[0];
        }

        String[] columnList = parameter.split("\\s*,\\s*");
        MessagingField[] fields = MessagingField.getFields(columnList);
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
     * @throws MessagingException - When the 'sort' parameter contains an illegal value.
     */
    public MessagingField getSort() throws MessagingException {
        String parameter = request.getParameter("sort");
        if (parameter == null) {
            return null;
        }
        MessagingField field = MessagingField.getField(parameter);
        if (field == null) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("sort", parameter);
        }
        return field;
    }

    /**
     * Retrieves and parses the 'order' parameter. Returns <code>null</code> when 'order' is not set. Fails when 'order' contains neither
     * 'desc' and 'asc'. Matches case-insensitively.
     * 
     * @throws MessagingException - When 'order' contains an illegal value.
     */
    public OrderDirection getOrder() throws MessagingException {
        String parameter = request.getParameter("order");
        if (parameter == null) {
            return null;
        }
        try {
            return OrderDirection.valueOf(OrderDirection.class, parameter.toUpperCase());
        } catch (IllegalArgumentException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("order", parameter);
        }
    }

    /**
     * Retrieves the given 'id' parameter. Fails when the 'id' parameter is unset.
     * 
     * @throws MessagingException - When the 'id' parameter is unset.
     */
    public String getId() throws MessagingException {
        return requireParameter("id");
    }

    /**
     * Retrieves and parses the 'peek' parameter. Returns 'false' when 'peek' is not set. Fails when 'peek' contains neither 'true' nor
     * 'false'. Matches case insensitively.
     * 
     * @throws MessagingException - When 'peek' contains an illegal value.
     */
    public boolean getPeek() throws MessagingException {
        String parameter = request.getParameter("peek");
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
     * Retrieves a list of ids from the request body. Fails when the body does not contain a JSONArray (or the body is missing).
     * 
     * @throws JSONException - When an underlying parsing exception occurs.
     * @throws MessagingException - When The body is missing or no JSONArray
     */
    public String[] getIds() throws JSONException, MessagingException {
        Object data = request.getData();
        if (data == null) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create("body");
        }
        if (!JSONArray.class.isInstance(data)) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("body", data.toString());
        }
        JSONArray idsJSON = (JSONArray) data;
        String[] ids = new String[idsJSON.length()];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = idsJSON.getString(i);
        }

        return ids;
    }

    /**
     * Retrieves the 'messageAction' parameter. Fails when 'messageAction' was not set.
     * 
     * @return
     * @throws MessagingException - When 'messageAction' was not set.
     */
    public String getMessageAction() throws MessagingException {
        return requireParameter("messageAction");
    }

    public MessagingMessage getMessage() throws MessagingException, JSONException, IOException {
        Object data = request.getData();
        if (data == null) {
            return null;
        }
        if (!JSONObject.class.isInstance(data)) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("body", data.toString());
        }
        return parser.parse((JSONObject) data, null);
    }

    /**
     * Determines if the given parameters were set in the request.
     */
    public boolean isset(String... params) {
        for (String param : params) {
            if (null == request.getParameter(param)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the MessagingAccountTransport that matches the parameters 'messagingService' and 'account'. Fails when either of the
     * parameters has not been set.
     */
    public MessagingAccountTransport getTransport() throws MessagingException {
        return registry.getMessagingService(getMessagingServiceId()).getAccountTransport(getAccountID(), session);
    }

    /**
     * Retrieves and parses the 'recipients' parameter. May return null, if no recipients were set.
     * @throws MessagingException 
     */
    public MessagingAddressHeader getRecipients() throws MessagingException {
        String parameter = request.getParameter("recipients");
        if(parameter == null) {
            return null;
        }
        return MimeAddressMessagingHeader.valueOfRFC822("", parameter);
    }

    /**
     * Tries to either parse the folder in its long form or assemble it from the content
     * 
     * @throws MessagingException
     */
    public MessagingFolderAddress getLongFolder() throws MessagingException {
        if (hasLongFolder()) {
            return MessagingFolderAddress.parse(request.getParameter("folder"));
        } else if (isset("messagingService", "account", "folder")) {
            MessagingFolderAddress address = new MessagingFolderAddress();
            address.setMessagingService(getMessagingServiceId());
            address.setAccount(getAccountID());
            address.setFolder(getFolderId());
            return address;
        }
        return null;
    }

    private boolean hasLongFolder() throws MessagingException {
        return isset("folder") && MessagingFolderAddress.matches(request.getParameter("folder"));
    }

}
