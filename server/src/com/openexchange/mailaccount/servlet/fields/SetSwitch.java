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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.servlet.fields;

import com.openexchange.mailaccount.AttributeSwitch;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link SetSwitch}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SetSwitch implements AttributeSwitch {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SetSwitch.class);

    private final MailAccountDescription desc;

    private Object value;

    public SetSwitch(final MailAccountDescription desc) {
        super();
        this.desc = desc;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public Object confirmedHam() {
        desc.setConfirmedHam((String) value);
        return null;
    }

    public Object confirmedSpam() {
        desc.setConfirmedSpam((String) value);
        return null;
    }

    public Object drafts() {
        desc.setDrafts((String) value);
        return null;
    }

    public Object id() {
        desc.setId(((Integer) value).intValue());
        return null;
    }

    public Object login() {
        desc.setLogin((String) value);
        return null;
    }

    public Object mailURL() {
        desc.parseMailServerURL((String) value);
        return null;
    }

    public Object name() {
        desc.setName((String) value);
        return null;
    }

    public Object password() {
        desc.setPassword((String) value);
        return null;
    }

    public Object primaryAddress() {
        desc.setPrimaryAddress((String) value);
        return null;
    }

    public Object sent() {
        desc.setSent((String) value);
        return null;
    }

    public Object spam() {
        desc.setSpam((String) value);
        return null;
    }

    public Object spamHandler() {
        desc.setSpamHandler((String) value);
        return null;
    }

    public Object transportURL() {
        desc.parseTransportServerURL((String) value);
        return null;
    }

    public Object trash() {
        desc.setTrash((String) value);
        return null;
    }

    public Object mailPort() {
        try {
            desc.setMailPort(Integer.parseInt(value.toString()));
        } catch (final NumberFormatException e) {
            LOG.error(
                new StringBuilder("Mail port is not a number: ").append(value).append(". Setting to fallback port 143.").toString(),
                e);
            desc.setMailPort(143);
        }
        return null;
    }

    public Object mailProtocol() {
        desc.setMailProtocol((String) value);
        return null;
    }

    public Object mailSecure() {
        desc.setMailSecure(Boolean.parseBoolean(value.toString()));
        return null;
    }

    public Object mailServer() {
        desc.setMailServer((String) value);
        return null;
    }

    public Object transportPort() {
        try {
            desc.setTransportPort(Integer.parseInt(value.toString()));
        } catch (final NumberFormatException e) {
            LOG.error(
                new StringBuilder("Transport port is not a number: ").append(value).append(". Setting to fallback port 25.").toString(),
                e);
            desc.setTransportPort(25);
        }
        return null;
    }

    public Object transportProtocol() {
        desc.setTransportProtocol((String) value);
        return null;
    }

    public Object transportSecure() {
        desc.setTransportSecure(Boolean.parseBoolean(value.toString()));
        return null;
    }

    public Object transportServer() {
        desc.setTransportServer((String) value);
        return null;
    }

    public Object transportLogin() {
        desc.setTransportLogin((String) value);
        return null;
    }

    public Object transportPassword() {
        desc.setTransportPassword((String) value);
        return null;
    }

    public Object unifiedINBOXEnabled() {
        desc.setUnifiedINBOXEnabled(((Boolean) value).booleanValue());
        return null;
    }

    public Object confirmedHamFullname() {
        desc.setConfirmedHamFullname((String) value);
        return null;
    }

    public Object confirmedSpamFullname() {
        desc.setConfirmedSpamFullname((String) value);
        return null;
    }

    public Object draftsFullname() {
        desc.setDraftsFullname((String) value);
        return null;
    }

    public Object sentFullname() {
        desc.setSentFullname((String) value);
        return null;
    }

    public Object spamFullname() {
        desc.setSpamFullname((String) value);
        return null;
    }

    public Object trashFullname() {
        desc.setTrashFullname((String) value);
        return null;
    }

    public Object pop3DeleteWriteThrough() {
        desc.addProperty("pop3.deletewt", (String) value);
        return null;
    }

    public Object pop3ExpungeOnQuit() {
        desc.addProperty("pop3.expunge", (String) value);
        return null;
    }

    public Object pop3RefreshRate() {
        desc.addProperty("pop3.refreshrate", (String) value);
        return null;
    }

    public Object pop3Path() {
        desc.addProperty("pop3.path", (String) value);
        return null;
    }

    public Object pop3Storage() {
        desc.addProperty("pop3.storage", (String) value);
        return null;
    }

}
