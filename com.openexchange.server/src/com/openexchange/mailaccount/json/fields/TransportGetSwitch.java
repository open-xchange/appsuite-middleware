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
import com.openexchange.mailaccount.TransportAccountDescription;

/**
 * {@link TransportGetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TransportGetSwitch implements AttributeSwitch {

    private final TransportAccountDescription desc;

    /**
     * Initializes a new {@link TransportGetSwitch}.
     *
     * @param desc The account description
     */
    public TransportGetSwitch(final TransportAccountDescription desc) {
        super();
        this.desc = desc;
    }

    @Override
    public Object confirmedHam() {
        return null;
    }

    @Override
    public Object confirmedSpam() {return null;
    }

    @Override
    public Object drafts() {return null;
    }

    @Override
    public Object id() {
        return desc.getId();
    }

    @Override
    public Object login() {return null;
    }

    @Override
    public Object replyTo() {
        return desc.getReplyTo();
    }

    @Override
    public Object mailURL() throws OXException {return null;
    }

    @Override
    public Object name() {
        return desc.getName();
    }

    @Override
    public Object password() {return null;
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
    public Object sent() {return null;
    }

    @Override
    public Object spam() {return null;
    }

    @Override
    public Object spamHandler() {return null;
    }

    @Override
    public Object transportURL() throws OXException {
        return desc.generateTransportServerURL();
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
        return Integer.valueOf(desc.getTransportPort());
    }

    @Override
    public Object transportProtocol() {
        return desc.getTransportProtocol();
    }

    @Override
    public Object transportSecure() {
        return null;
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
        return desc.getTransportAuth();
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
        return Boolean.valueOf(desc.isTransportStartTls());
    }

    @Override
    public Object mailOAuth() {
        return null;
    }

    @Override
    public Object transportOAuth() {
        return Integer.valueOf(desc.getTransportOAuthId());
    }

    @Override
    public Object rootFolder() {
        return null;
    }

}
