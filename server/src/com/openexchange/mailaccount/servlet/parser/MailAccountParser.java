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

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mailaccount.Attribute;
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
    public Set<Attribute> parse(final MailAccountDescription account, final JSONObject json) throws OXJSONException {
        try {
            return parseElementAccount(account, json);
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, json.toString());
        }
    }

    protected Set<Attribute> parseElementAccount(final MailAccountDescription account, final JSONObject json) throws JSONException, OXJSONException {
        final Set<Attribute> attributes = new HashSet<Attribute>();
        if (json.has(MailAccountFields.ID)) {
            account.setId(parseInt(json, MailAccountFields.ID));
            attributes.add(Attribute.ID_LITERAL);
        }
        if (json.has(MailAccountFields.LOGIN)) {
            account.setLogin(parseString(json, MailAccountFields.LOGIN));
            attributes.add(Attribute.LOGIN_LITERAL);
        }
        if (json.has(MailAccountFields.PASSWORD)) {
            account.setPassword(parseString(json, MailAccountFields.PASSWORD));
            attributes.add(Attribute.PASSWORD_LITERAL);
        }
        // Expect URL or separate fields for protocol, server, port, and secure
        if (json.has(MailAccountFields.MAIL_URL)) {
            account.setMailServerURL(parseString(json, MailAccountFields.MAIL_URL));
            attributes.add(Attribute.MAIL_URL_LITERAL);
        } else {
            final String url;
            {
                if (!json.has(MailAccountFields.MAIL_SERVER)) {
                    throw new JSONException("Missing field \"" + MailAccountFields.MAIL_SERVER + "\" in JSON object.");
                }
                if (!json.has(MailAccountFields.MAIL_PORT)) {
                    throw new JSONException("Missing field \"" + MailAccountFields.MAIL_PORT + "\" in JSON object.");
                }
                final StringBuilder urlBuilder = new StringBuilder(128);
                if (json.has(MailAccountFields.MAIL_PROTOCOL)) {
                    urlBuilder.append(parseString(json, MailAccountFields.MAIL_PROTOCOL));
                } else {
                    urlBuilder.append(MailProperties.getInstance().getDefaultMailProvider());
                }
                if (parseBoolean(json, MailAccountFields.MAIL_SECURE)) {
                    urlBuilder.append('s');
                }
                urlBuilder.append("://");
                urlBuilder.append(parseString(json, MailAccountFields.MAIL_SERVER));
                urlBuilder.append(':').append(parseInt(json, MailAccountFields.MAIL_PORT));
                url = urlBuilder.toString();
            }
            account.setMailServerURL(url);
            attributes.add(Attribute.MAIL_URL_LITERAL);
        }
        if (json.has(MailAccountFields.TRANSPORT_URL)) {
            account.setTransportServerURL(parseString(json, MailAccountFields.TRANSPORT_URL));
            attributes.add(Attribute.TRANSPORT_URL_LITERAL);
        } else {
            final String url;
            {
                if (!json.has(MailAccountFields.TRANSPORT_SERVER)) {
                    throw new JSONException("Missing field \"" + MailAccountFields.TRANSPORT_SERVER + "\" in JSON object.");
                }
                if (!json.has(MailAccountFields.TRANSPORT_PORT)) {
                    throw new JSONException("Missing field \"" + MailAccountFields.TRANSPORT_PORT + "\" in JSON object.");
                }
                final StringBuilder urlBuilder = new StringBuilder(128);
                if (json.has(MailAccountFields.TRANSPORT_PROTOCOL)) {
                    urlBuilder.append(parseString(json, MailAccountFields.TRANSPORT_PROTOCOL));
                } else {
                    urlBuilder.append(TransportProperties.getInstance().getDefaultTransportProvider());
                }
                if (parseBoolean(json, MailAccountFields.TRANSPORT_SECURE)) {
                    urlBuilder.append('s');
                }
                urlBuilder.append("://");
                urlBuilder.append(parseString(json, MailAccountFields.TRANSPORT_SERVER));
                urlBuilder.append(':').append(parseInt(json, MailAccountFields.TRANSPORT_PORT));
                url = urlBuilder.toString();
            }
            account.setTransportServerURL(url);
            attributes.add(Attribute.TRANSPORT_URL_LITERAL);
        }
        if (json.has(MailAccountFields.NAME)) {
            account.setName(parseString(json, MailAccountFields.NAME));
            attributes.add(Attribute.NAME_LITERAL);
        }
        if (json.has(MailAccountFields.PRIMARY_ADDRESS)) {
            account.setPrimaryAddress(parseString(json, MailAccountFields.PRIMARY_ADDRESS));
            attributes.add(Attribute.PRIMARY_ADDRESS_LITERAL);
        }
        if (json.has(MailAccountFields.SPAM_HANDLER)) {
            account.setSpamHandler(parseString(json, MailAccountFields.SPAM_HANDLER));
            attributes.add(Attribute.SPAM_HANDLER_LITERAL);
        }
        // Folder names
        if (json.has(MailAccountFields.TRASH)) {
            account.setTrash(parseString(json, MailAccountFields.TRASH));
            attributes.add(Attribute.TRASH_LITERAL);
        }
        if (json.has(MailAccountFields.SENT)) {
            account.setSent(parseString(json, MailAccountFields.SENT));
            attributes.add(Attribute.SENT_LITERAL);
        }
        if (json.has(MailAccountFields.DRAFTS)) {
            account.setDrafts(parseString(json, MailAccountFields.DRAFTS));
            attributes.add(Attribute.DRAFTS_LITERAL);
        }
        if (json.has(MailAccountFields.SPAM)) {
            account.setSpam(parseString(json, MailAccountFields.SPAM));
            attributes.add(Attribute.SPAM_HANDLER_LITERAL);
        }
        if (json.has(MailAccountFields.CONFIRMED_SPAM)) {
            account.setConfirmedSpam(parseString(json, MailAccountFields.CONFIRMED_SPAM));
            attributes.add(Attribute.CONFIRMED_SPAM_LITERAL);
        }
        if (json.has(MailAccountFields.CONFIRMED_HAM)) {
            account.setConfirmedHam(parseString(json, MailAccountFields.CONFIRMED_HAM));
            attributes.add(Attribute.CONFIRMED_HAM_LITERAL);
        }
        return attributes;
    }

}
