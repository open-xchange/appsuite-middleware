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

import org.json.JSONObject;
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
        this.value = value == JSONObject.NULL ? null : value;
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
        } catch (NumberFormatException e) {
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
        } catch (NumberFormatException e) {
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

    @Override
    public Object mailDisabled() {
        desc.setMailDisabled(Boolean.parseBoolean(value.toString()));
        return null;
    }

    @Override
    public Object transportDisabled() {
        desc.setTransportDisabled(Boolean.parseBoolean(value.toString()));
        return null;
    }

}
