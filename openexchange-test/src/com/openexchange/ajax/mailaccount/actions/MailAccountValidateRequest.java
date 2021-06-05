/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        return new Parameter[] { new Parameter("action", "validate"), new Parameter("tree", String.valueOf(tree))
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

    @SuppressWarnings("serial")
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
                } catch (OXException e) {
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
                } catch (OXException e) {
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

            @Override
            public boolean isMailDisabled() {
                return acc.isMailDisabled();
            }

            @Override
            public boolean isTransportDisabled() {
                return acc.isTransportDisabled();
            }
        };
    }
}
