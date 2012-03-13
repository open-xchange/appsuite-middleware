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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.mail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.messaging.MessagingMessage;

/**
 * {@link MailMessagingMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public class MailMessagingMessage extends MailMessagingBodyPart implements MessagingMessage {

    private static final long serialVersionUID = 3329678416653813017L;

    /**
     * The underlying {@link MimeMessage} instance.
     */
    final MailMessage mailMessage;

    private String picture;

    /**
     * Initializes a new {@link MailMessagingMessage}.
     *
     * @param mailMessage The mail message
     */
    public MailMessagingMessage(final MailMessage mailMessage) {
        super(mailMessage, null);
        this.mailMessage = mailMessage;
    }

    @Override
    public int getColorLabel() throws OXException {
        return mailMessage.getColorLabel();
    }

    /**
     * Sets the color label.
     *
     * @param colorLabel The color label
     */
    public void setColorLabel(final int colorLabel) {
        mailMessage.setColorLabel(colorLabel);
    }

    @Override
    public int getFlags() throws OXException {
        return mailMessage.getFlags();
    }

    /**
     * Sets the flags.
     *
     * @param flags The flags
     */
    public void setFlags(final int flags) {
        mailMessage.setFlags(flags);
    }

    @Override
    public Collection<String> getUserFlags() throws OXException {
        final String[] userFlags = mailMessage.getUserFlags();
        return null == userFlags ? Collections.<String> emptyList() : Arrays.asList(userFlags);
    }

    /**
     * Sets specified user flags.
     *
     * @param userFlags The user flags to set
     */
    public void setUserFlags(final Collection<String> userFlags) {
        if (null == userFlags) {
            mailMessage.removeUserFlags();
            return;
        }
        mailMessage.addUserFlags(userFlags.toArray(new String[userFlags.size()]));
    }

    @Override
    public String getFolder() {
        return mailMessage.getFolder();
    }

    /**
     * Sets the folder fullname.
     *
     * @param folder The folder fullname to set
     */
    public void setFolder(final String folder) {
        mailMessage.setFolder(folder);
    }

    @Override
    public long getReceivedDate() {
        final Date receivedDate = mailMessage.getReceivedDate();
        return null == receivedDate ? -1L : receivedDate.getTime();
    }

    /**
     * Sets the received date.
     *
     * @param receivedDate The received date
     */
    public void setReceivedDate(final long receivedDate) {
        mailMessage.setReceivedDate(receivedDate >= 0 ? new Date(receivedDate) : null);
    }

    @Override
    public int getThreadLevel() {
        return mailMessage.getThreadLevel();
    }

    /**
     * Sets the thread level.
     *
     * @param threadLevel The thread level
     */
    public void setThreadLevel(final int threadLevel) {
        mailMessage.setThreadLevel(threadLevel);
    }

    @Override
    public String getId() {
        return mailMessage.getMailId();
    }

    /**
     * Sets the message identifier.
     *
     * @param id The message identifier
     */
    public void setId(final String id) {
        mailMessage.setMailId(id);
    }

    @Override
    public String getPicture() {
        return picture;
    }

    /**
     * Sets the picture URL.
     *
     * @param picture The picture URL
     */
    public void setPicture(final String picture) {
        this.picture = picture;
    }

    @Override
    public String getUrl() throws OXException {
        return null;
    }

}
