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

package com.openexchange.mailaccount.servlet.writer;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.utils.ProviderUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields;
import com.openexchange.mailaccount.servlet.fields.MailAccountGetSwitch;

/**
 * {@link MailAccountWriter} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountWriter {

    private MailAccountWriter() {
        super();
    }

    /**
     * Writes specified mail account to a JSON object.
     * 
     * @param account The mail account to written from mail account's data
     * @return A JSON object filled with
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject write(final MailAccount account) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(MailAccountFields.ID, account.getId());
        json.put(MailAccountFields.LOGIN, account.getLogin());
        // json.put(MailAccountFields.PASSWORD, account.getLogin());
        {
            final String mailURL = account.getMailServerURL();
            json.put(MailAccountFields.MAIL_URL, mailURL);
            String protocol = ProviderUtility.extractProtocol(mailURL, MailProperties.getInstance().getDefaultMailProvider());
            final boolean secure = protocol.endsWith("s");
            if (secure) {
                protocol = protocol.substring(0, protocol.length() - 1);
            }
            String hostname;
            {
                final String[] parsed = MailConfig.parseProtocol(mailURL);
                if (parsed == null) {
                    hostname = mailURL;
                } else {
                    hostname = parsed[1];
                }
            }
            final int port;
            {
                final int pos = hostname.indexOf(':');
                if (pos > -1) {
                    port = Integer.parseInt(hostname.substring(pos + 1));
                    hostname = hostname.substring(0, pos);
                } else {
                    port = 143;
                }
            }
            json.put(MailAccountFields.MAIL_PORT, port);
            json.put(MailAccountFields.MAIL_PROTOCOL, protocol);
            json.put(MailAccountFields.MAIL_SECURE, secure);
            json.put(MailAccountFields.MAIL_SERVER, hostname);
        }
        {
            final String transportURL = account.getTransportServerURL();
            json.put(MailAccountFields.TRANSPORT_URL, account.getTransportServerURL());
            String protocol = ProviderUtility.extractProtocol(transportURL, TransportProperties.getInstance().getDefaultTransportProvider());
            final boolean secure = null != protocol && protocol.endsWith("s");
            if (secure) {
                protocol = protocol.substring(0, protocol.length() - 1);
            }
            String hostname;
            {
                final String[] parsed = MailConfig.parseProtocol(transportURL);
                if (parsed == null) {
                    hostname = transportURL;
                } else {
                    hostname = parsed[1];
                }
            }
            final int port;
            {
                final int pos = hostname.indexOf(':');
                if (pos > -1) {
                    port = Integer.parseInt(hostname.substring(pos + 1));
                    hostname = hostname.substring(0, pos);
                } else {
                    port = 25;
                }
            }
            json.put(MailAccountFields.TRANSPORT_PORT, port);
            json.put(MailAccountFields.TRANSPORT_PROTOCOL, protocol);
            json.put(MailAccountFields.TRANSPORT_SECURE, secure);
            json.put(MailAccountFields.TRANSPORT_SERVER, hostname);
        }
        json.put(MailAccountFields.NAME, account.getName());
        json.put(MailAccountFields.PRIMARY_ADDRESS, account.getPrimaryAddress());
        json.put(MailAccountFields.SPAM_HANDLER, account.getSpamHandler());
        // Folder
        json.put(MailAccountFields.TRASH, account.getTrash());
        json.put(MailAccountFields.SENT, account.getSent());
        json.put(MailAccountFields.DRAFTS, account.getDrafts());
        json.put(MailAccountFields.SPAM, account.getSpam());
        json.put(MailAccountFields.CONFIRMED_SPAM, account.getConfirmedSpam());
        json.put(MailAccountFields.CONFIRMED_HAM, account.getConfirmedHam());

        return json;
    }

    public static JSONArray writeArray(final MailAccount[] userMailAccounts, final List<Attribute> attributes) throws JSONException {
        final JSONArray rows = new JSONArray();
        for (final MailAccount account : userMailAccounts) {
            final MailAccountGetSwitch getter = new MailAccountGetSwitch(account);
            final JSONArray row = new JSONArray();
            for (final Attribute attribute : attributes) {
                if (Attribute.PASSWORD_LITERAL == attribute) {
                    row.put(JSONObject.NULL);
                } else {
                    row.put(attribute.doSwitch(getter));
                }
            }
            rows.put(row);
        }
        return rows;
    }
}
