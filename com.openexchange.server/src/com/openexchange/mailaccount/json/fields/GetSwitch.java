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
        return desc.getId();
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
    public Object rootFolder() {
        return null;
    }

}
