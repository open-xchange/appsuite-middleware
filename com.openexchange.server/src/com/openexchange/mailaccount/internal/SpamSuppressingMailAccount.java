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

package com.openexchange.mailaccount.internal;

import java.util.Map;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.TransportAuth;

/**
 * {@link SpamSuppressingMailAccount} - A view on a mail account with spam/ham information suppressed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SpamSuppressingMailAccount implements MailAccount {

    private static final long serialVersionUID = 3164431489000757656L;

    private final MailAccount mailAccount;
    private final boolean suppressSpam;
    private final boolean suppresHam;

    /**
     * Initializes a new {@link SpamSuppressingMailAccount}.
     */
    public SpamSuppressingMailAccount(MailAccount mailAccount, boolean suppressSpam, boolean suppresHam) {
        super();
        this.mailAccount = mailAccount;
        this.suppressSpam = suppressSpam;
        this.suppresHam = suppresHam;
    }

    @Override
    public int getUserId() {
        return mailAccount.getUserId();
    }

    @Override
    public String generateMailServerURL() {
        return mailAccount.generateMailServerURL();
    }

    @Override
    public String generateTransportServerURL() {
        return mailAccount.generateTransportServerURL();
    }

    @Override
    public String getMailServer() {
        return mailAccount.getMailServer();
    }

    @Override
    public int getId() {
        return mailAccount.getId();
    }

    @Override
    public String getLogin() {
        return mailAccount.getLogin();
    }

    @Override
    public int getMailPort() {
        return mailAccount.getMailPort();
    }

    @Override
    public String getName() {
        return mailAccount.getName();
    }

    @Override
    public String getMailProtocol() {
        return mailAccount.getMailProtocol();
    }

    @Override
    public String getPassword() {
        return mailAccount.getPassword();
    }

    @Override
    public boolean isMailSecure() {
        return mailAccount.isMailSecure();
    }

    @Override
    public boolean isMailOAuthAble() {
        return mailAccount.isMailOAuthAble();
    }

    @Override
    public String getPrimaryAddress() {
        return mailAccount.getPrimaryAddress();
    }

    @Override
    public int getMailOAuthId() {
        return mailAccount.getMailOAuthId();
    }

    @Override
    public String getPersonal() {
        return mailAccount.getPersonal();
    }

    @Override
    public String getReplyTo() {
        return mailAccount.getReplyTo();
    }

    @Override
    public boolean isMailDisabled() {
        return mailAccount.isMailDisabled();
    }

    @Override
    public TransportAuth getTransportAuth() {
        return mailAccount.getTransportAuth();
    }

    @Override
    public String getTransportLogin() {
        return mailAccount.getTransportLogin();
    }

    @Override
    public int getTransportPort() {
        return mailAccount.getTransportPort();
    }

    @Override
    public String getTransportProtocol() {
        return mailAccount.getTransportProtocol();
    }

    @Override
    public String getTransportPassword() {
        return mailAccount.getTransportPassword();
    }

    @Override
    public String getTransportServer() {
        return mailAccount.getTransportServer();
    }

    @Override
    public boolean isTransportOAuthAble() {
        return mailAccount.isTransportOAuthAble();
    }

    @Override
    public boolean isTransportSecure() {
        return mailAccount.isTransportSecure();
    }

    @Override
    public int getTransportOAuthId() {
        return mailAccount.getTransportOAuthId();
    }

    @Override
    public boolean isDefaultAccount() {
        return mailAccount.isDefaultAccount();
    }

    @Override
    public String getSpamHandler() {
        return mailAccount.getSpamHandler();
    }

    @Override
    public boolean isTransportStartTls() {
        return mailAccount.isTransportStartTls();
    }

    @Override
    public String getDrafts() {
        return mailAccount.getDrafts();
    }

    @Override
    public String getSent() {
        return mailAccount.getSent();
    }

    @Override
    public String getSpam() {
        return mailAccount.getSpam();
    }

    @Override
    public String getTrash() {
        return mailAccount.getTrash();
    }

    @Override
    public boolean isTransportDisabled() {
        return mailAccount.isTransportDisabled();
    }

    @Override
    public String getArchive() {
        return mailAccount.getArchive();
    }

    @Override
    public String getConfirmedHam() {
        return suppresHam ? null : mailAccount.getConfirmedHam();
    }

    @Override
    public String getConfirmedSpam() {
        return suppressSpam ? null : mailAccount.getConfirmedSpam();
    }

    @Override
    public boolean isUnifiedINBOXEnabled() {
        return mailAccount.isUnifiedINBOXEnabled();
    }

    @Override
    public String getTrashFullname() {
        return mailAccount.getTrashFullname();
    }

    @Override
    public String getArchiveFullname() {
        return mailAccount.getArchiveFullname();
    }

    @Override
    public String getSentFullname() {
        return mailAccount.getSentFullname();
    }

    @Override
    public String getDraftsFullname() {
        return mailAccount.getDraftsFullname();
    }

    @Override
    public String getSpamFullname() {
        return mailAccount.getSpamFullname();
    }

    @Override
    public String getConfirmedSpamFullname() {
        return suppressSpam ? null : mailAccount.getConfirmedSpamFullname();
    }

    @Override
    public String getConfirmedHamFullname() {
        return suppresHam ? null : mailAccount.getConfirmedHamFullname();
    }

    @Override
    public Map<String, String> getProperties() {
        return mailAccount.getProperties();
    }

    @Override
    public void addProperty(String name, String value) {
        mailAccount.addProperty(name, value);
    }

    @Override
    public Map<String, String> getTransportProperties() {
        return mailAccount.getTransportProperties();
    }

    @Override
    public void addTransportProperty(String name, String value) {
        mailAccount.addTransportProperty(name, value);
    }

    @Override
    public boolean isMailStartTls() {
        return mailAccount.isMailStartTls();
    }

    @Override
    public String getRootFolder() {
        return mailAccount.getRootFolder();
    }


}
