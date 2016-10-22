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

}
