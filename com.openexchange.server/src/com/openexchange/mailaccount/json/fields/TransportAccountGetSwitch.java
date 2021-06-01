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

package com.openexchange.mailaccount.json.fields;

import com.openexchange.mailaccount.AttributeSwitch;
import com.openexchange.mailaccount.TransportAccount;

/**
 * {@link TransportAccountGetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TransportAccountGetSwitch implements AttributeSwitch {

    private final TransportAccount account;

    /**
     * Initializes a new {@link TransportAccountGetSwitch}.
     *
     * @param account The account to read from
     * @throws IllegalArgumentException If passed account is <code>null</code>
     */
    public TransportAccountGetSwitch(final TransportAccount account) {
        super();
        if (null == account) {
            throw new IllegalArgumentException("account is null.");
        }
        this.account = account;
    }

    @Override
    public Object replyTo() {
        return null;
    }

    @Override
    public Object confirmedHam() {
        return null;
    }

    @Override
    public Object confirmedSpam() {
        return null;
    }

    @Override
    public Object drafts() {
        return null;
    }

    @Override
    public Object id() {
        return Integer.valueOf(account.getId());
    }

    @Override
    public Object login() {
        return account.getLogin();
    }

    @Override
    public Object mailURL() {
        return null;
    }

    @Override
    public Object name() {
        return account.getName();
    }

    @Override
    public Object password() {
        return account.getPassword();
    }

    @Override
    public Object primaryAddress() {
        return account.getPrimaryAddress();
    }

    @Override
    public Object personal() {
        return account.getPersonal();
    }

    @Override
    public Object sent() {
        return null;
    }

    @Override
    public Object spam() {
        return null;
    }

    @Override
    public Object spamHandler() {
        return null;
    }

    @Override
    public Object transportURL() {
        return account.generateTransportServerURL();
    }

    @Override
    public Object trash() {
        return null;
    }

    @Override
    public Object archive() {
        return null;
    }

    @Override
    public Object mailPort() {
        return null;
    }

    @Override
    public Object mailProtocol() {
        return null;
    }

    @Override
    public Object mailSecure() {
        return null;
    }

    @Override
    public Object mailServer() {
        return null;
    }

    @Override
    public Object transportPort() {
        return Integer.valueOf(account.getTransportPort());
    }

    @Override
    public Object transportProtocol() {
        return account.getTransportProtocol();
    }

    @Override
    public Object transportSecure() {
        return Boolean.valueOf(account.isTransportSecure());
    }

    @Override
    public Object transportServer() {
        return account.getTransportServer();
    }

    @Override
    public Object transportLogin() {
        return account.getTransportLogin();
    }

    @Override
    public Object transportPassword() {
        return account.getTransportPassword();
    }

    @Override
    public Object unifiedINBOXEnabled() {
        return null;
    }

    @Override
    public Object confirmedHamFullname() {
        return null;
    }

    @Override
    public Object confirmedSpamFullname() {
        return null;
    }

    @Override
    public Object draftsFullname() {
        return null;
    }

    @Override
    public Object sentFullname() {
        return null;
    }

    @Override
    public Object spamFullname() {
        return null;
    }

    @Override
    public Object trashFullname() {
        return null;
    }

    @Override
    public Object archiveFullname() {
        return null;
    }

    @Override
    public Object transportAuth() {
        return account.getTransportAuth();
    }

    @Override
    public Object pop3DeleteWriteThrough() {
        return null;
    }

    @Override
    public Object pop3ExpungeOnQuit() {
        return null;
    }

    @Override
    public Object pop3RefreshRate() {
        return null;
    }

    @Override
    public Object pop3Path() {
        return null;
    }

    @Override
    public Object pop3Storage() {
        return null;
    }

    @Override
    public Object addresses() {
        return null;
    }

    @Override
    public Object mailStartTls() {
        return null;
    }

    @Override
    public Object transportStartTls() {
        return Boolean.valueOf(account.isTransportStartTls());
    }

    @Override
    public Object mailOAuth() {
        return null;
    }

    @Override
    public Object transportOAuth() {
        return Integer.valueOf(account.getTransportOAuthId());
    }

    @Override
    public Object rootFolder() {
        return null;
    }

    @Override
    public Object mailDisabled() {
        return null;
    }

    @Override
    public Object transportDisabled() {
        return Boolean.valueOf(account.isTransportDisabled());
    }

}
