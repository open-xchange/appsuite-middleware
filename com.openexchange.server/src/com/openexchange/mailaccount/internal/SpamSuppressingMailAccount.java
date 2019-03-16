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
