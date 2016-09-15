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
import com.openexchange.mailaccount.TransportAuth;

/**
 * {@link SetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SetSwitch implements AttributeSwitch {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SetSwitch.class);

    private final MailAccountDescription desc;

    private Object value;

    /**
     * Initializes a new {@link SetSwitch}.
     *
     * @param desc The account description
     */
    public SetSwitch(final MailAccountDescription desc) {
        super();
        this.desc = desc;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public Object replyTo() {
        desc.setReplyTo((String) value);
        return null;
    }

    @Override
    public Object confirmedHam() {
        desc.setConfirmedHam((String) value);
        return null;
    }

    @Override
    public Object confirmedSpam() {
        desc.setConfirmedSpam((String) value);
        return null;
    }

    @Override
    public Object drafts() {
        desc.setDrafts((String) value);
        return null;
    }

    @Override
    public Object id() {
        desc.setId(((Integer) value).intValue());
        return null;
    }

    @Override
    public Object login() {
        desc.setLogin((String) value);
        return null;
    }

    @Override
    public Object mailURL() throws OXException {
        desc.parseMailServerURL((String) value);
        return null;
    }

    @Override
    public Object name() {
        desc.setName((String) value);
        return null;
    }

    @Override
    public Object password() {
        desc.setPassword((String) value);
        return null;
    }

    @Override
    public Object primaryAddress() {
        desc.setPrimaryAddress((String) value);
        return null;
    }

    @Override
    public Object personal() {
        desc.setPersonal((String) value);
        return null;
    }

    @Override
    public Object sent() {
        desc.setSent((String) value);
        return null;
    }

    @Override
    public Object spam() {
        desc.setSpam((String) value);
        return null;
    }

    @Override
    public Object spamHandler() {
        desc.setSpamHandler((String) value);
        return null;
    }

    @Override
    public Object transportURL() throws OXException {
        desc.parseTransportServerURL((String) value);
        return null;
    }

    @Override
    public Object trash() {
        desc.setTrash((String) value);
        return null;
    }

    @Override
    public Object archive() {
        desc.setArchive((String) value);
        return null;
    }

    @Override
    public Object mailPort() {
        try {
            desc.setMailPort(Integer.parseInt(value.toString()));
        } catch (final NumberFormatException e) {
            LOG.error("Mail port is not a number: {}. Setting to fallback port 143.", value,
                e);
            desc.setMailPort(143);
        }
        return null;
    }

    @Override
    public Object mailProtocol() {
        desc.setMailProtocol((String) value);
        return null;
    }

    @Override
    public Object mailSecure() {
        desc.setMailSecure(Boolean.parseBoolean(value.toString()));
        return null;
    }

    @Override
    public Object mailServer() {
        desc.setMailServer((String) value);
        return null;
    }

    @Override
    public Object transportPort() {
        try {
            desc.setTransportPort(Integer.parseInt(value.toString()));
        } catch (final NumberFormatException e) {
            LOG.debug("Transport port is not a number: {}. Setting to fallback port 25.", value,
                e);
            desc.setTransportPort(25);
        }
        return null;
    }

    @Override
    public Object transportProtocol() {
        desc.setTransportProtocol((String) value);
        return null;
    }

    @Override
    public Object transportSecure() {
        desc.setTransportSecure(Boolean.parseBoolean(value.toString()));
        return null;
    }

    @Override
    public Object transportServer() {
        desc.setTransportServer((String) value);
        return null;
    }

    @Override
    public Object transportLogin() {
        desc.setTransportLogin((String) value);
        return null;
    }

    @Override
    public Object transportPassword() {
        desc.setTransportPassword((String) value);
        return null;
    }

    @Override
    public Object unifiedINBOXEnabled() {
        desc.setUnifiedINBOXEnabled(((Boolean) value).booleanValue());
        return null;
    }

    @Override
    public Object confirmedHamFullname() {
        desc.setConfirmedHamFullname((String) value);
        return null;
    }

    @Override
    public Object confirmedSpamFullname() {
        desc.setConfirmedSpamFullname((String) value);
        return null;
    }

    @Override
    public Object draftsFullname() {
        desc.setDraftsFullname((String) value);
        return null;
    }

    @Override
    public Object sentFullname() {
        desc.setSentFullname((String) value);
        return null;
    }

    @Override
    public Object spamFullname() {
        desc.setSpamFullname((String) value);
        return null;
    }

    @Override
    public Object trashFullname() {
        desc.setTrashFullname((String) value);
        return null;
    }

    @Override
    public Object archiveFullname() {
        desc.setArchiveFullname((String) value);
        return null;
    }

    @Override
    public Object transportAuth() {
        if (value instanceof TransportAuth) {
            desc.setTransportAuth((TransportAuth) value);
        } else {
            TransportAuth tmp = null == value ? null : TransportAuth.transportAuthFor(value.toString());
            desc.setTransportAuth(tmp);
        }
        return null;
    }

    @Override
    public Object pop3DeleteWriteThrough() {
        desc.addProperty("pop3.deletewt", value.toString());
        return null;
    }

    @Override
    public Object pop3ExpungeOnQuit() {
        desc.addProperty("pop3.expunge", value.toString());
        return null;
    }

    @Override
    public Object pop3RefreshRate() {
        desc.addProperty("pop3.refreshrate", (String) value);
        return null;
    }

    @Override
    public Object pop3Path() {
        desc.addProperty("pop3.path", (String) value);
        return null;
    }

    @Override
    public Object pop3Storage() {
        desc.addProperty("pop3.storage", (String) value);
        return null;
    }

    @Override
    public Object addresses() {
        desc.addProperty("addresses", (String) value);
        return null;
    }

    @Override
    public Object mailStartTls() {
        desc.setMailStartTls(Boolean.parseBoolean(value.toString()));
        return null;
    }

    @Override
    public Object transportStartTls() {
        desc.setTransportStartTls(Boolean.parseBoolean(value.toString()));
        return null;
    }

    @Override
    public Object mailOAuth() {
        desc.setMailOAuthId(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString()));
        return null;
    }

    @Override
    public Object transportOAuth() {
        desc.setTransportOAuthId(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString()));
        return null;
    }

    @Override
    public Object rootFolder() {
        return null;
    }

}
