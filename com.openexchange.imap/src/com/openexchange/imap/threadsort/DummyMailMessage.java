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

package com.openexchange.imap.threadsort;

import java.io.InputStream;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;

/**
 * {@link DummyMailMessage} - A dummy mail message for thread-sort purpose.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class DummyMailMessage extends MailMessage {

    /**
     *
     */
    private static final long serialVersionUID = -541392246833470725L;

    private String mailId;

    /**
     * Initializes a new {@link DummyMailMessage}.
     */
    DummyMailMessage() {
        super();
    }

    @Override
    public String getMailId() {
        return mailId;
    }

    @Override
    public int getUnreadMessages() {
        return -1;
    }

    @Override
    public void setMailId(String id) {
        mailId = id;
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
        // Nothing to do
    }

    @Override
    public Object getContent() throws OXException {
        throw new UnsupportedOperationException("DummyMailMessage.getContent()");
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        throw new UnsupportedOperationException("DummyMailMessage.getDataHandler()");
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        throw new UnsupportedOperationException("DummyMailMessage.getEnclosedMailPart()");
    }

    @Override
    public InputStream getInputStream() throws OXException {
        throw new UnsupportedOperationException("DummyMailMessage.getInputStream()");
    }

    @Override
    public void loadContent() throws OXException {
        // Nothing to do
    }

    @Override
    public void prepareForCaching() {
        // Nothing to do
    }

}
