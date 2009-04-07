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

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
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
    public JSONValue action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            return actionDelete(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
            return actionNew(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            return actionUpate(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            return actionGet(jsonObject);
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

    private JSONObject actionUpate(final JSONObject jsonObject) throws AjaxException, OXException, JSONException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, AJAXServlet.PARAMETER_DATA);

        try {
            final MailAccountDescription accountDescription = new MailAccountDescription();
            new MailAccountParser().parse(accountDescription, jData);

            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);

            storageService.updateMailAccount(accountDescription, session.getUserId(), session.getContextId(), session.getPassword());

            final JSONObject jsonAccount = MailAccountWriter.write(storageService.getMailAccount(
                accountDescription.getId(),
                session.getUserId(),
                session.getContextId()));

            return jsonAccount;
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

}
