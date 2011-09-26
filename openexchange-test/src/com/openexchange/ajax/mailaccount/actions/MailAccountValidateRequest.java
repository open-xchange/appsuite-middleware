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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.mailaccount.actions;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.json.writer.MailAccountWriter;

/**
 * {@link MailAccountValidateRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountValidateRequest implements AJAXRequest<MailAccountValidateResponse> {

    private final MailAccountDescription account;

    private final boolean failOnError;

    private final boolean tree;

    public MailAccountValidateRequest(final MailAccountDescription account) {
        this(account, true);
    }

    public MailAccountValidateRequest(final MailAccountDescription account, final boolean failOnError) {
        this(account, false, failOnError);
    }

    public MailAccountValidateRequest(final MailAccountDescription account, final boolean tree, final boolean failOnError) {
        this.account = account;
        this.failOnError = failOnError;
        this.tree = tree;
    }

    public Object getBody() throws JSONException {
        final JSONObject json = MailAccountWriter.write(wrap(account));
        json.put("password", account.getPassword());
        return json;
    }

    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.PUT;
    }

    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("action", "validate"),
            new Parameter("tree", String.valueOf(tree))
        };
    }

    public AbstractAJAXParser<MailAccountValidateResponse> getParser() {
        return new MailAccountValidateParser(failOnError);
    }

    public String getServletPath() {
        return "/ajax/account";
    }

    private MailAccount wrap(final MailAccountDescription acc) {
        return new MailAccount() {

            public String getConfirmedHam() {
                return acc.getConfirmedHam();
            }

            public String getConfirmedSpam() {
                return acc.getConfirmedSpam();
            }

            public String getDrafts() {
                return acc.getDrafts();
            }

            public int getId() {
                return acc.getId();
            }

            public String getLogin() {
                return acc.getLogin();
            }

            public String generateMailServerURL() {
                try {
                    return acc.generateMailServerURL();
                } catch (final OXException e) {
                    throw new IllegalStateException(e);
                }
            }

            public int getMailPort() {
                return acc.getMailPort();
            }

            public String getMailProtocol() {
                return acc.getMailProtocol();
            }

            public String getMailServer() {
                return acc.getMailServer();
            }

            public boolean isMailSecure() {
                return acc.isMailSecure();
            }

            public String getName() {
                return acc.getName();
            }

            public String getReplyTo() {
                return acc.getReplyTo();
            }

            public String getPassword() {
                return acc.getPassword();
            }

            public String getPrimaryAddress() {
                return acc.getPrimaryAddress();
            }

            public String getPersonal() {
                return acc.getPersonal();
            }

            public String getSent() {
                return acc.getSent();
            }

            public String getSpam() {
                return acc.getSpam();
            }

            public String getSpamHandler() {
                return acc.getSpamHandler();
            }

            public String generateTransportServerURL() {
                try {
                    return acc.generateTransportServerURL();
                } catch (final OXException e) {
                    throw new IllegalStateException(e);
                }
            }

            public int getTransportPort() {
                return acc.getTransportPort();
            }

            public String getTransportProtocol() {
                return acc.getTransportProtocol();
            }

            public String getTransportServer() {
                return acc.getTransportServer();
            }

            public boolean isTransportSecure() {
                return acc.isTransportSecure();
            }

            public String getTrash() {
                return acc.getTrash();
            }

            public int getUserId() {
                return -1;
            }

            public boolean isDefaultAccount() {
                return false;
            }

            public String getTransportLogin() {
                return acc.getTransportLogin();
            }

            public String getTransportPassword() {
                return acc.getTransportPassword();
            }

            public boolean isUnifiedINBOXEnabled() {
                return acc.isUnifiedINBOXEnabled();
            }

            public String getConfirmedHamFullname() {
                return acc.getConfirmedHamFullname();
            }

            public String getConfirmedSpamFullname() {
                return acc.getConfirmedSpamFullname();
            }

            public String getDraftsFullname() {
                return acc.getDraftsFullname();
            }

            public String getSentFullname() {
                return acc.getSentFullname();
            }

            public String getSpamFullname() {
                return acc.getSpamFullname();
            }

            public String getTrashFullname() {
                return acc.getTrashFullname();
            }

            public void addProperty(final String name, final String value) {
                acc.addProperty(name, value);
            }

            public Map<String, String> getProperties() {
                return acc.getProperties();
            }
        };
    }
}
