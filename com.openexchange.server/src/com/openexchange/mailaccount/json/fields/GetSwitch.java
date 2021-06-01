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

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.AttributeSwitch;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link GetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GetSwitch implements AttributeSwitch {

    private final MailAccountDescription desc;

    /**
     * Initializes a new {@link GetSwitch}.
     *
     * @param desc The account description
     */
    public GetSwitch(final MailAccountDescription desc) {
        super();
        this.desc = desc;
    }

    @Override
    public Object confirmedHam() {
        return desc.getConfirmedHam();
    }

    @Override
    public Object confirmedSpam() {
        return desc.getConfirmedSpam();
    }

    @Override
    public Object drafts() {
        return desc.getDrafts();
    }

    @Override
    public Object id() {
        return I(desc.getId());
    }

    @Override
    public Object login() {
        return desc.getLogin();
    }

    @Override
    public Object replyTo() {
        return desc.getReplyTo();
    }

    @Override
    public Object mailURL() throws OXException {
        return desc.generateMailServerURL();
    }

    @Override
    public Object name() {
        return desc.getName();
    }

    @Override
    public Object password() {
        return desc.getPassword();
    }

    @Override
    public Object primaryAddress() {
        return desc.getPrimaryAddress();
    }

    @Override
    public Object personal() {
        return desc.getPersonal();
    }

    @Override
    public Object sent() {
        return desc.getSent();
    }

    @Override
    public Object spam() {
        return desc.getSpam();
    }

    @Override
    public Object spamHandler() {
        return desc.getSpamHandler();
    }

    @Override
    public Object transportURL() throws OXException {
        return desc.generateTransportServerURL();
    }

    @Override
    public Object trash() {
        return desc.getTrash();
    }

    @Override
    public Object archive() {
        return desc.getArchive();
    }

    @Override
    public Object mailPort() {
        return Integer.valueOf(desc.getMailPort());
    }

    @Override
    public Object mailProtocol() {
        return desc.getMailProtocol();
    }

    @Override
    public Object mailSecure() {
        return Boolean.valueOf(desc.isMailSecure());
    }

    @Override
    public Object mailServer() {
        return desc.getMailServer();
    }

    @Override
    public Object transportPort() {
        return Integer.valueOf(desc.getTransportPort());
    }

    @Override
    public Object transportProtocol() {
        return desc.getTransportProtocol();
    }

    @Override
    public Object transportSecure() {
        return Boolean.valueOf(desc.isTransportSecure());
    }

    @Override
    public Object transportServer() {
        return desc.getTransportServer();
    }

    @Override
    public Object transportLogin() {
        return desc.getTransportLogin();
    }

    @Override
    public Object transportPassword() {
        return desc.getTransportPassword();
    }

    @Override
    public Object unifiedINBOXEnabled() {
        return Boolean.valueOf(desc.isUnifiedINBOXEnabled());
    }

    @Override
    public Object confirmedHamFullname() {
        return desc.getConfirmedHamFullname();
    }

    @Override
    public Object confirmedSpamFullname() {
        return desc.getConfirmedSpamFullname();
    }

    @Override
    public Object draftsFullname() {
        return desc.getDraftsFullname();
    }

    @Override
    public Object sentFullname() {
        return desc.getSentFullname();
    }

    @Override
    public Object spamFullname() {
        return desc.getSpamFullname();
    }

    @Override
    public Object trashFullname() {
        return desc.getTrashFullname();
    }

    @Override
    public Object archiveFullname() {
        return desc.getArchiveFullname();
    }

    @Override
    public Object transportAuth() {
        return desc.getTransportAuth();
    }

    @Override
    public Object pop3DeleteWriteThrough() {
        return Boolean.valueOf(desc.getProperties().get("pop3.deletewt"));
    }

    @Override
    public Object pop3ExpungeOnQuit() {
        return Boolean.valueOf(desc.getProperties().get("pop3.expunge"));
    }

    @Override
    public Object pop3RefreshRate() {
        return desc.getProperties().get("pop3.refreshrate");
    }

    @Override
    public Object pop3Path() {
        return desc.getProperties().get("pop3.path");
    }

    @Override
    public Object pop3Storage() {
        return desc.getProperties().get("pop3.storage");
    }

    @Override
    public Object addresses() {
        return desc.getProperties().get("addresses");
    }

    @Override
    public Object mailStartTls() {
        return Boolean.valueOf(desc.isMailStartTls());
    }

    @Override
    public Object transportStartTls() {
        return Boolean.valueOf(desc.isTransportStartTls());
    }

    @Override
    public Object mailOAuth() {
        return Integer.valueOf(desc.getMailOAuthId());
    }

    @Override
    public Object transportOAuth() {
        return Integer.valueOf(desc.getTransportOAuthId());
    }

    @Override
    public Object rootFolder() {
        return null;
    }

    @Override
    public Object mailDisabled() {
        return Boolean.valueOf(desc.isMailDisabled());
    }

    @Override
    public Object transportDisabled() {
        return Boolean.valueOf(desc.isTransportDisabled());
    }

}
