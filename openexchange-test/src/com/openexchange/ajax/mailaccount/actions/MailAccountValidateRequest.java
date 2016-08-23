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
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.writer.DefaultMailAccountWriter;

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

    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = DefaultMailAccountWriter.write(wrap(account));
        json.put("password", account.getPassword());
        json.put("transport_password", account.getTransportPassword());
        return json;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("action", "validate"),
            new Parameter("tree", String.valueOf(tree))
        };
    }

    @Override
    public AbstractAJAXParser<MailAccountValidateResponse> getParser() {
        return new MailAccountValidateParser(failOnError);
    }

    @Override
    public String getServletPath() {
        return "/ajax/account";
    }

    private MailAccount wrap(final MailAccountDescription acc) {
        return new MailAccount() {

            @Override
            public String getConfirmedHam() {
                return acc.getConfirmedHam();
            }

            @Override
            public String getConfirmedSpam() {
                return acc.getConfirmedSpam();
            }

            @Override
            public String getDrafts() {
                return acc.getDrafts();
            }

            @Override
            public int getId() {
                return acc.getId();
            }

            @Override
            public String getLogin() {
                return acc.getLogin();
            }

            @Override
            public String generateMailServerURL() {
                try {
                    return acc.generateMailServerURL();
                } catch (final OXException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public int getMailPort() {
                return acc.getMailPort();
            }

            @Override
            public String getMailProtocol() {
                return acc.getMailProtocol();
            }

            @Override
            public String getMailServer() {
                return acc.getMailServer();
            }

            @Override
            public boolean isMailSecure() {
                return acc.isMailSecure();
            }

            @Override
            public String getName() {
                return acc.getName();
            }

            @Override
            public String getReplyTo() {
                return acc.getReplyTo();
            }

            @Override
            public String getPassword() {
                return acc.getPassword();
            }

            @Override
            public String getPrimaryAddress() {
                return acc.getPrimaryAddress();
            }

            @Override
            public String getPersonal() {
                return acc.getPersonal();
            }

            @Override
            public String getSent() {
                return acc.getSent();
            }

            @Override
            public String getSpam() {
                return acc.getSpam();
            }

            @Override
            public String getSpamHandler() {
                return acc.getSpamHandler();
            }

            @Override
            public String generateTransportServerURL() {
                try {
                    return acc.generateTransportServerURL();
                } catch (final OXException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public TransportAuth getTransportAuth() {
                return acc.getTransportAuth();
            }

            @Override
            public int getTransportPort() {
                return acc.getTransportPort();
            }

            @Override
            public String getTransportProtocol() {
                return acc.getTransportProtocol();
            }

            @Override
            public String getTransportServer() {
                return acc.getTransportServer();
            }

            @Override
            public boolean isTransportSecure() {
                return acc.isTransportSecure();
            }

            @Override
            public String getTrash() {
                return acc.getTrash();
            }

            @Override
            public String getArchive() {
                return acc.getArchive();
            }

            @Override
            public int getUserId() {
                return -1;
            }

            @Override
            public boolean isDefaultAccount() {
                return false;
            }

            @Override
            public String getTransportLogin() {
                return acc.getTransportLogin();
            }

            @Override
            public String getTransportPassword() {
                return acc.getTransportPassword();
            }

            @Override
            public boolean isUnifiedINBOXEnabled() {
                return acc.isUnifiedINBOXEnabled();
            }

            @Override
            public String getConfirmedHamFullname() {
                return acc.getConfirmedHamFullname();
            }

            @Override
            public String getConfirmedSpamFullname() {
                return acc.getConfirmedSpamFullname();
            }

            @Override
            public String getDraftsFullname() {
                return acc.getDraftsFullname();
            }

            @Override
            public String getSentFullname() {
                return acc.getSentFullname();
            }

            @Override
            public String getSpamFullname() {
                return acc.getSpamFullname();
            }

            @Override
            public String getTrashFullname() {
                return acc.getTrashFullname();
            }

            @Override
            public String getArchiveFullname() {
                return acc.getArchiveFullname();
            }

            @Override
            public void addProperty(final String name, final String value) {
                acc.addProperty(name, value);
            }

            @Override
            public Map<String, String> getProperties() {
                return acc.getProperties();
            }

            @Override
            public Map<String, String> getTransportProperties() {
                return acc.getTransportProperties();
            }

            @Override
            public void addTransportProperty(final String name, final String value) {
                acc.addTransportProperty(name, value);
            }

            @Override
            public boolean isMailStartTls() {
                return acc.isMailStartTls();
            }

            @Override
            public boolean isTransportStartTls() {
                return acc.isTransportStartTls();
            }

            @Override
            public int getMailOAuthId() {
                return acc.getMailOAuthId();
            }

            @Override
            public int getTransportOAuthId() {
                return acc.getTransportOAuthId();
            }

            @Override
            public boolean isMailOAuthAble() {
                return acc.isMailOAuthAble();
            }

            @Override
            public boolean isTransportOAuthAble() {
                return acc.isTransportOAuthAble();
            }

            @Override
            public String getRootFolder() {
                return null;
            }
        };
    }
}
