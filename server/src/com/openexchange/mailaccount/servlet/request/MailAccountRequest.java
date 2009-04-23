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

package com.openexchange.mailaccount.servlet.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.servlet.parser.MailAccountParser;
import com.openexchange.mailaccount.servlet.writer.MailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountRequest} - Handles request to mail account servlet.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountRequest {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountRequest.class);

    private final ServerSession session;

    private Date timestamp;

    /**
     * Gets the time stamp.
     * 
     * @return The time stamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Initializes a new {@link MailAccountRequest}.
     * 
     * @param session The session
     */
    public MailAccountRequest(final ServerSession session) {
        super();
        this.session = session;
    }

    /**
     * Handles the request dependent on specified action string.
     * 
     * @param action The action string
     * @param jsonObject The JSON object containing request's data & parameters
     * @return A JSON result object dependent on triggered action method
     * @throws OXMandatoryFieldException If a mandatory field is missing in passed JSON request object
     * @throws OXException If a server-related error occurs
     * @throws JSONException If a JSON error occurs
     * @throws SearchIteratorException If a search-iterator error occurs
     * @throws AjaxException If an AJAX error occurs
     * @throws OXJSONException If a JSON error occurs
     */
    public Object action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            return actionDelete(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
            return actionNew(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            return actionUpate(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            return actionGet(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
            return actionAll(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
            return actionList(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_VALIDATE)) {
            return actionValidate(jsonObject);
        } else {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
    }

    private JSONObject actionGet(final JSONObject jsonObject) throws JSONException, OXException, OXJSONException, AjaxException {
        final int id = DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_ID);

        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            final JSONObject jsonAccount = MailAccountWriter.write(storageService.getMailAccount(
                id,
                session.getUserId(),
                session.getContextId()));
            return jsonAccount;
        } catch (final AbstractOXException exc) {
            throw new OXException(exc);
        }
    }

    private JSONArray actionDelete(final JSONObject jsonObject) throws JSONException, OXException, OXJSONException, AjaxException {
        final int[] ids = DataParser.checkJSONIntArray(jsonObject, AJAXServlet.PARAMETER_DATA);

        final JSONArray jsonArray = new JSONArray();
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            for (int i = 0; i < ids.length; i++) {
                storageService.deleteMailAccount(ids[i], session.getUserId(), session.getContextId());

                jsonArray.put(ids[i]);
            }
        } catch (final AbstractOXException exc) {
            throw new OXException(exc);
        }
        return jsonArray;
    }

    private JSONObject actionNew(final JSONObject jsonObject) throws AjaxException, OXException, JSONException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, AJAXServlet.PARAMETER_DATA);

        try {
            final MailAccountDescription accountDescription = new MailAccountDescription();
            new MailAccountParser().parse(accountDescription, jData);

            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            final int id = storageService.insertMailAccount(
                accountDescription,
                session.getUserId(),
                session.getContext(),
                session.getPassword());

            final JSONObject jsonAccount = MailAccountWriter.write(storageService.getMailAccount(
                id,
                session.getUserId(),
                session.getContextId()));

            return jsonAccount;
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

    private Boolean actionValidate(final JSONObject jsonObject) throws AjaxException, OXException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, AJAXServlet.PARAMETER_DATA);

        try {
            final MailAccountDescription accountDescription = new MailAccountDescription();
            new MailAccountParser().parse(accountDescription, jData);
            // Validate mail server
            boolean validated = checkMailServerURL(accountDescription);
            // Failed?
            if (!validated) {
                return Boolean.FALSE;
            }
            // Now check transport server URL
            validated = checkTransportServerURL(accountDescription);
            return Boolean.valueOf(validated);
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

    private boolean checkMailServerURL(final MailAccountDescription accountDescription) throws MailException {
        final String mailServerURL = accountDescription.getMailServerURL();
        // Get the appropriate mail provider by mail server URL
        final MailProvider mailProvider = MailProviderRegistry.getMailProviderByURL(mailServerURL);
        if (null == mailProvider) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validating mail account failed. No mail provider found for URL: " + mailServerURL);
            }
            return false;
        }
        // Create a mail access instance
        final MailAccess<?, ?> mailAccess = mailProvider.createNewMailAccess(session);
        final MailConfig mailConfig = mailAccess.getMailConfig();
        // Set login and password
        mailConfig.setLogin(accountDescription.getLogin());
        mailConfig.setPassword(accountDescription.getPassword());
        // Set server and port
        final String server;
        {
            final String[] tmp = MailConfig.parseProtocol(mailServerURL);
            server = tmp == null ? mailServerURL : tmp[1];
        }
        final int pos = server.indexOf(':');
        if (pos == -1) {
            mailConfig.setPort(143);
            mailConfig.setServer(server);
        } else {
            final String sPort = server.substring(pos + 1);
            try {
                mailConfig.setPort(Integer.parseInt(sPort));
            } catch (final NumberFormatException e) {
                LOG.warn(new StringBuilder().append("Cannot parse port out of string: \"").append(sPort).append(
                    "\". Using fallback 143 instead."), e);
                mailConfig.setPort(143);
            }
            mailConfig.setServer(server.substring(0, pos));
        }
        boolean validated = true;
        // Now try to connect
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
        } catch (final AbstractOXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validating mail account failed.", e);
            }
            validated = false;
        } finally {
            if (close) {
                mailAccess.close(false);
            }
        }
        return validated;
    }

    private boolean checkTransportServerURL(final MailAccountDescription accountDescription) throws MailException {
        final String transportServerURL = accountDescription.getTransportServerURL();
        if (null == transportServerURL) {
            // Nothing to validate, treat as success
            return true;
        }
        // Get the appropriate transport provider by transport server URL
        final TransportProvider transportProvider = TransportProviderRegistry.getTransportProviderByURL(transportServerURL);
        if (null == transportProvider) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validating mail account failed. No transport provider found for URL: " + transportServerURL);
            }
            return false;
        }
        // Create a transport access instance
        final MailTransport mailTransport = transportProvider.createNewMailTransport(session);
        final TransportConfig transportConfig = mailTransport.getTransportConfig();
        // Set login and password
        transportConfig.setLogin(accountDescription.getLogin());
        transportConfig.setPassword(accountDescription.getPassword());
        // Set server and port
        final String server;
        {
            final String[] tmp = TransportConfig.parseProtocol(transportServerURL);
            server = tmp == null ? transportServerURL : tmp[1];
        }
        final int pos = server.indexOf(':');
        if (pos == -1) {
            transportConfig.setPort(25);
            transportConfig.setServer(server);
        } else {
            final String sPort = server.substring(pos + 1);
            try {
                transportConfig.setPort(Integer.parseInt(sPort));
            } catch (final NumberFormatException e) {
                LOG.warn(new StringBuilder().append("Cannot parse port out of string: \"").append(sPort).append(
                    "\". Using fallback 25 instead."), e);
                transportConfig.setPort(25);
            }
            transportConfig.setServer(server.substring(0, pos));
        }
        boolean validated = true;
        // Now try to connect
        boolean close = false;
        try {
            mailTransport.ping();
            close = true;
        } catch (final AbstractOXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validating transport account failed.", e);
            }
            validated = false;
        } finally {
            if (close) {
                mailTransport.close();
            }
        }
        return validated;
    }

    private JSONObject actionUpate(final JSONObject jsonObject) throws AjaxException, OXException, JSONException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, AJAXServlet.PARAMETER_DATA);

        try {
            final MailAccountDescription accountDescription = new MailAccountDescription();
            final Set<Attribute> fieldsToUpdate = new MailAccountParser().parse(accountDescription, jData);

            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            storageService.updateMailAccount(
                accountDescription,
                fieldsToUpdate,
                session.getUserId(),
                session.getContextId(),
                session.getPassword());

            final JSONObject jsonAccount = MailAccountWriter.write(storageService.getMailAccount(
                accountDescription.getId(),
                session.getUserId(),
                session.getContextId()));

            return jsonAccount;
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

    private JSONArray actionAll(final JSONObject request) throws JSONException, OXException {
        final String colString = request.optString(AJAXServlet.PARAMETER_COLUMNS);
        final List<Attribute> attributes = getColumns(colString);
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            final MailAccount[] userMailAccounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            return MailAccountWriter.writeArray(userMailAccounts, attributes);
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

    private List<Attribute> getColumns(final String colString) {
        List<Attribute> attributes = null;
        if (colString != null && !"".equals(colString.trim())) {
            attributes = new LinkedList<Attribute>();
            for (final String col : colString.split("\\s*,\\s*")) {
                if ("".equals(col)) {
                    continue;
                }
                attributes.add(Attribute.getById(Integer.parseInt(col)));
            }
            return attributes;
        } else {
            return Arrays.asList(Attribute.values());

        }
    }

    private JSONArray actionList(final JSONObject request) throws JSONException, OXException {
        final String colString = request.optString(AJAXServlet.PARAMETER_COLUMNS);
        final List<Attribute> attributes = getColumns(colString);
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            final JSONArray ids = request.getJSONArray(AJAXServlet.PARAMETER_DATA);
            final List<MailAccount> accounts = new ArrayList<MailAccount>();
            for (int i = 0, size = ids.length(); i < size; i++) {
                final int id = ids.getInt(i);
                final MailAccount account = storageService.getMailAccount(id, session.getUserId(), session.getContextId());
                accounts.add(account);
            }

            return MailAccountWriter.writeArray(accounts.toArray(new MailAccount[accounts.size()]), attributes);
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

}
