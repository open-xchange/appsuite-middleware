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

package com.openexchange.messaging.generic.internet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.ParameterizedMessagingMessage;
import com.openexchange.messaging.generic.internal.InternalUtility;
import com.openexchange.messaging.generic.internal.InternalUtility.ParsedFlags;

/**
 * {@link MimeMessagingMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeMessagingMessage extends MimeMessagingBodyPart implements ParameterizedMessagingMessage {

    private static final long serialVersionUID = -1862413106904761044L;

    private static final Flags ALL_COLOR_LABELS;

    private static final Flags ALL_SYSTEM_FLAGS;

    static {
        final StringBuilder sb = new StringBuilder(6);
        Flags flags = new Flags();
        for (int i = 0; i <= 10; i++) {
            sb.setLength(0);
            flags.add(sb.append(InternalUtility.COLOR_LABEL_PREFIX).append(i).toString());
            sb.setLength(0);
            flags.add(sb.append(InternalUtility.COLOR_LABEL_PREFIX_OLD).append(i).toString());
        }
        ALL_COLOR_LABELS = flags;

        flags = new Flags();
        flags.add(Flags.Flag.ANSWERED);
        flags.add(Flags.Flag.DELETED);
        flags.add(Flags.Flag.DRAFT);
        flags.add(Flags.Flag.FLAGGED);
        flags.add(Flags.Flag.SEEN);
        flags.add(Flags.Flag.USER);
        flags.add(MessagingMessage.USER_FORWARDED);
        flags.add(MessagingMessage.USER_READ_ACK);
        ALL_SYSTEM_FLAGS = flags;
    }

    /**
     * The underlying {@link MimeMessage} instance.
     */
    final MimeMessage mimeMessage;

    private String folder;

    private ParsedFlags cachedParsedFlags;

    private long receivedDate;

    private int threadLevel;

    private String id;

    private String picture;

    private final Map<String, Object> parameters;

    /**
     * Initializes a new {@link MimeMessagingMessage}.
     */
    public MimeMessagingMessage() {
        super(new MimeMessage(MimeDefaultSession.getDefaultSession()), null);
        mimeMessage = (MimeMessage) part;
        parameters = new HashMap<String, Object>(4);
    }

    /**
     * Initializes a new {@link MimeMessagingMessage}.
     *
     * @param mimeMessage The MIME message
     */
    protected MimeMessagingMessage(final MimeMessage mimeMessage) {
        super(mimeMessage, null);
        this.mimeMessage = mimeMessage;
        parameters = new HashMap<String, Object>(4);
    }

    @Override
    public int getColorLabel() throws OXException {
        if (null == cachedParsedFlags) {
            try {
                cachedParsedFlags = InternalUtility.parseFlags(mimeMessage.getFlags());
            } catch (javax.mail.MessagingException e) {
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
        return cachedParsedFlags.getColorLabel();
    }

    /**
     * Sets the color label.
     *
     * @param colorLabel The color label
     * @throws OXException If setting color label fails
     */
    public void setColorLabel(final int colorLabel) throws OXException {
        try {
            /*
             * Disable all existing color label flags
             */
            mimeMessage.setFlags(ALL_COLOR_LABELS, false);
            /*
             * Apply new one
             */
            final Flags newFlags = new Flags();
            newFlags.add(InternalUtility.getColorLabelStringValue(colorLabel));
            mimeMessage.setFlags(newFlags, true);
            cachedParsedFlags = null;
        } catch (javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    @Override
    public int getFlags() throws OXException {
        if (null == cachedParsedFlags) {
            try {
                cachedParsedFlags = InternalUtility.parseFlags(mimeMessage.getFlags());
            } catch (javax.mail.MessagingException e) {
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
        return cachedParsedFlags.getFlags();
    }

    /**
     * Sets the flags.
     *
     * @param flags The flags
     * @throws OXException If given flags cannot be set
     */
    public void setFlags(final int flags) throws OXException {
        try {
            /*
             * Disable all existing system flags
             */
            mimeMessage.setFlags(ALL_SYSTEM_FLAGS, false);
            /*
             * Apply new one
             */
            final Flags newFlags = InternalUtility.convertMessagingFlags(flags);
            mimeMessage.setFlags(newFlags, true);
            cachedParsedFlags = null;
        } catch (javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    @Override
    public Collection<String> getUserFlags() throws OXException {
        if (null == cachedParsedFlags) {
            try {
                cachedParsedFlags = InternalUtility.parseFlags(mimeMessage.getFlags());
            } catch (javax.mail.MessagingException e) {
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
        return cachedParsedFlags.getUserFlags();
    }

    /**
     * Sets specified user flags.
     *
     * @param userFlags The user flags to set
     * @throws OXException If setting user flags fails
     */
    public void setUserFlags(final Collection<String> userFlags) throws OXException {
        try {
            final String[] strings = mimeMessage.getFlags().getUserFlags();
            Flags newFlags = new Flags();
            for (final String string : strings) {
                final String uf = string;
                if (!InternalUtility.isColorLabel(uf) && !MessagingMessage.USER_FORWARDED.equalsIgnoreCase(uf) && !MessagingMessage.USER_READ_ACK.equalsIgnoreCase(uf)) {
                    newFlags.add(uf);
                }
            }
            /*
             * Disable all existing system flags
             */
            mimeMessage.setFlags(newFlags, false);
            /*
             * Apply new ones
             */
            newFlags = new Flags();
            for (final String userFlag : userFlags) {
                newFlags.add(userFlag);
            }
            mimeMessage.setFlags(newFlags, true);
            cachedParsedFlags = null;
        } catch (javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    @Override
    public String getFolder() {
        if (null == folder) {
            /*
             * Determine MIME message's folder.
             */
            final Folder folder = mimeMessage.getFolder();
            return null == folder ? null : folder.getFullName();
        }
        return folder;
    }

    /**
     * Sets the folder fullname.
     *
     * @param folder The folder fullname to set
     */
    public void setFolder(final String folder) {
        this.folder = folder;
    }

    @Override
    public long getReceivedDate() {
        return receivedDate;
    }

    /**
     * Sets the received date.
     *
     * @param receivedDate The received date
     */
    public void setReceivedDate(final long receivedDate) {
        this.receivedDate = receivedDate;
    }

    @Override
    public int getThreadLevel() {
        return threadLevel;
    }

    /**
     * Sets the thread level.
     *
     * @param threadLevel The thread level
     */
    public void setThreadLevel(final int threadLevel) {
        this.threadLevel = threadLevel;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the message identifier.
     *
     * @param id The message identifier
     */
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getPicture() {
        return picture;
    }

    /**
     * Sets the picture
     *
     * @param picture The picture url
     */
    public void setPicture(final String picture) {
        this.picture = picture;
    }

    @Override
    public String getUrl() throws OXException {
        return null;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public void putParameter(final String name, final Object value) {
        parameters.put(name, value);
    }

    @Override
    public boolean putParameterIfAbsent(final String name, final Object value) {
        if (parameters.containsKey(name)) {
            return false;
        }
        parameters.put(name, value);
        return true;
    }

    @Override
    public void clearParameters() {
        parameters.clear();
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

}
