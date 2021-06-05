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

package com.openexchange.mail.dataobjects;

import java.io.InputStream;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.StorageUtility;

/**
 * {@link IDMailMessage} - Supports only {@link #getMailId()} and {@link #getFolder()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDMailMessage extends MailMessage {

    private static final long serialVersionUID = -8945006270321242506L;

    private long uid;
    private long originalUid;
    private String mailId;
    private int seqnum;
    private int unreadMessages;

    /**
     * Initializes a new {@link IDMailMessage}
     */
    public IDMailMessage() {
        super();
        unreadMessages = -1;
    }

    /**
     * Initializes a new {@link IDMailMessage}
     */
    public IDMailMessage(String mailId, String folder) {
        this();
        this.mailId = mailId;
        if (null == mailId) {
            uid = -1L;
        } else {
            uid = StorageUtility.parseUnsignedLong(mailId);
        }
        setFolder(folder);
        unreadMessages = -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IDMailMessage [");
        final String folder = getFolder();
        if (folder != null) {
            builder.append("folder=").append(folder).append(", ");
        }
        final String mailId = getMailId();
        if (mailId != null) {
            builder.append("mail-id=").append(mailId);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String getMailId() {
        return mailId;
    }

    @Override
    public int getUnreadMessages() {
        return unreadMessages;
    }

    @Override
    public void setMailId(String id) {
        mailId = id;
        if (null == id) {
            uid = -1L;
        } else {
            uid = StorageUtility.parseUnsignedLong(mailId);
        }
    }

    /**
     * Gets the UID
     *
     * @return The UID or <code>-1</code> if absent
     */
    public long getUid() {
        return uid;
    }

    /**
     * Sets the UID
     *
     * @param uid The UID to set or <code>-1</code> to indicate absence
     */
    public void setUid(long uid) {
        this.uid = uid;
        if (uid > 0) {
            mailId = Long.toString(uid);
        }
    }

    /**
     * Gets the original UID
     *
     * @return The original UID or <code>-1</code> if absent
     */
    public long getOriginalUid() {
        return originalUid;
    }

    /**
     * Sets the original UID
     *
     * @param originalUid The original UID to set or <code>-1</code> to indicate absence
     */
    public void setOriginalUid(long originalUid) {
        this.originalUid = originalUid;
        if (originalUid > 0) {
            setOriginalId(Long.toString(originalUid));
        }
    }

    /**
     * Gets the sequence number.
     *
     * @return The sequence number
     */
    public int getSeqnum() {
        return seqnum;
    }

    /**
     * Sets the sequence number.
     *
     * @param seqnum The sequence number to set
     */
    public void setSeqnum(int seqnum) {
        this.seqnum = seqnum;
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    @Override
    public Object getContent() throws OXException {
        throw new UnsupportedOperationException("IDMailMessage.getContent() not supported");
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        throw new UnsupportedOperationException("IDMailMessage.getDataHandler() not supported");
    }

    @Override
    public int getEnclosedCount() throws OXException {
        throw new UnsupportedOperationException("IDMailMessage.getEnclosedCount() not supported");
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        throw new UnsupportedOperationException("IDMailMessage.getEnclosedMailPart() not supported");
    }

    @Override
    public InputStream getInputStream() throws OXException {
        throw new UnsupportedOperationException("IDMailMessage.getInputStream() not supported");
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
