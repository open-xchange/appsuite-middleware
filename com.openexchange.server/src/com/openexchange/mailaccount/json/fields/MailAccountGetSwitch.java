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
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountGetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailAccountGetSwitch implements AttributeSwitch {

    private final MailAccount account;

    /**
     * Initializes a new {@link MailAccountGetSwitch}.
     *
     * @param account The account to read from
     * @throws IllegalArgumentException If passed account is <code>null</code>
     */
    public MailAccountGetSwitch(final MailAccount account) {
        super();
        if (null == account) {
            throw new IllegalArgumentException("account is null.");
        }
        this.account = account;
    }

    @Override
    public Object replyTo() {
        return account.getReplyTo();
    }

    @Override
    public Object confirmedHam() {
        return account.getConfirmedHam();
    }

    @Override
    public Object confirmedSpam() {
        return account.getConfirmedSpam();
    }

    @Override
    public Object drafts() {
        return account.getDrafts();
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
        return account.generateMailServerURL();
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
        return account.getSent();
    }

    @Override
    public Object spam() {
        return account.getSpam();
    }

    @Override
    public Object spamHandler() {
        return account.getSpamHandler();
    }

    @Override
    public Object transportURL() {
        return account.generateTransportServerURL();
    }

    @Override
    public Object trash() {
        return account.getTrash();
    }

    @Override
    public Object archive() {
        return account.getArchive();
    }

    @Override
    public Object mailPort() {
        return Integer.valueOf(account.getMailPort());
    }

    @Override
    public Object mailProtocol() {
        return account.getMailProtocol();
    }

    @Override
    public Object mailSecure() {
        return Boolean.valueOf(account.isMailSecure());
    }

    @Override
    public Object mailServer() {
        return account.getMailServer();
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
        return Boolean.valueOf(account.isUnifiedINBOXEnabled());
    }

    @Override
    public Object confirmedHamFullname() {
        return account.getConfirmedHamFullname();
    }

    @Override
    public Object confirmedSpamFullname() {
        return account.getConfirmedSpamFullname();
    }

    @Override
    public Object draftsFullname() {
        return account.getDraftsFullname();
    }

    @Override
    public Object sentFullname() {
        return account.getSentFullname();
    }

    @Override
    public Object spamFullname() {
        return account.getSpamFullname();
    }

    @Override
    public Object trashFullname() {
        return account.getTrashFullname();
    }

    @Override
    public Object archiveFullname() {
        return account.getArchiveFullname();
    }

    @Override
    public Object transportAuth() {
        return account.getTransportAuth();
    }

    @Override
    public Object pop3DeleteWriteThrough() {
        return account.getProperties().get("pop3.deletewt");
    }

    @Override
    public Object pop3ExpungeOnQuit() {
        return account.getProperties().get("pop3.expunge");
    }

    @Override
    public Object pop3RefreshRate() {
        return account.getProperties().get("pop3.refreshrate");
    }

    @Override
    public Object pop3Path() {
        return account.getProperties().get("pop3.path");
    }

    @Override
    public Object pop3Storage() {
        return account.getProperties().get("pop3.storage");
    }

    @Override
    public Object addresses() {
        return account.getProperties().get("addresses");
    }

    @Override
    public Object mailStartTls() {
        return Boolean.valueOf(account.isMailStartTls());
    }

    @Override
    public Object transportStartTls() {
        return Boolean.valueOf(account.isTransportStartTls());
    }

    @Override
    public Object mailOAuth() {
        return Integer.valueOf(account.getMailOAuthId());
    }

    @Override
    public Object transportOAuth() {
        return Integer.valueOf(account.getTransportOAuthId());
    }

    @Override
    public Object rootFolder() {
        return account.getRootFolder();
    }

    @Override
    public Object mailDisabled() {
        return Boolean.valueOf(account.isMailDisabled());
    }

    @Override
    public Object transportDisabled() {
        return Boolean.valueOf(account.isTransportDisabled());
    }

}
