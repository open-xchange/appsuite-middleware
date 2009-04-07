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

package com.openexchange.mailaccount.servlet.parser;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * {@link MailAccountParser} - Parses a JSON object to a mail account.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountParser extends DataParser {

    /**
     * Default constructor.
     */
    public MailAccountParser() {
        super();
    }

    /**
     * Parses the attributes from the JSON and writes them into the account object.
     * 
     * @param account Any attributes will be stored in this account object.
     * @param json A JSON object containing a reminder.
     * @throws OXJSONException If parsing fails.
     */
    public void parse(final MailAccountDescription account, final JSONObject json) throws OXJSONException {
        try {
            parseElementAccount(account, json);
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, json.toString());
        }
    }

    protected void parseElementAccount(final MailAccountDescription account, final JSONObject json) throws JSONException, OXJSONException {
        if (json.has(MailAccountFields.ID)) {
            account.setId(parseInt(json, MailAccountFields.ID));
        }
        if (json.has(MailAccountFields.LOGIN)) {
            account.setLogin(parseString(json, MailAccountFields.LOGIN));
        }
        if (json.has(MailAccountFields.PASSWORD)) {
            account.setPassword(parseString(json, MailAccountFields.PASSWORD));
        }
        if (json.has(MailAccountFields.MAIL_URL)) {
            account.setMailServerURL(parseString(json, MailAccountFields.MAIL_URL));
        }
        if (json.has(MailAccountFields.TRANSPORT_URL)) {
            account.setTransportServerURL(parseString(json, MailAccountFields.TRANSPORT_URL));
        }
        if (json.has(MailAccountFields.NAME)) {
            account.setName(parseString(json, MailAccountFields.NAME));
        }
        if (json.has(MailAccountFields.PRIMARY_ADDRESS)) {
            account.setPrimaryAddress(parseString(json, MailAccountFields.PRIMARY_ADDRESS));
        }
        if (json.has(MailAccountFields.SPAM_HANDLER)) {
            account.setSpamHandler(parseString(json, MailAccountFields.SPAM_HANDLER));
        }
        // Folder names
        if (json.has(MailAccountFields.TRASH)) {
            account.setTrash(parseString(json, MailAccountFields.TRASH));
        }
        if (json.has(MailAccountFields.SENT)) {
            account.setSent(parseString(json, MailAccountFields.SENT));
        }
        if (json.has(MailAccountFields.DRAFTS)) {
            account.setDrafts(parseString(json, MailAccountFields.DRAFTS));
        }
        if (json.has(MailAccountFields.SPAM)) {
            account.setSpam(parseString(json, MailAccountFields.SPAM));
        }
        if (json.has(MailAccountFields.CONFIRMED_SPAM)) {
            account.setConfirmedSpam(parseString(json, MailAccountFields.CONFIRMED_SPAM));
        }
        if (json.has(MailAccountFields.CONFIRMED_HAM)) {
            account.setConfirmedHam(parseString(json, MailAccountFields.CONFIRMED_HAM));
        }
    }

}
