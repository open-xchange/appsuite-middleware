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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.processor;

import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link MailFolderInfo} - Provides needed information about the mail folder to process
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderInfo {

    private final String fullName;

    private final int messageCount;

    /**
     * Initializes a new {@link MailFolderInfo}.
     * 
     * @param fullName The full name
     * @param messageCount The message count or <code>-1</code> if denoted folder does not hold messages
     */
    public MailFolderInfo(final String fullName, final int messageCount) {
        super();
        this.fullName = fullName;
        this.messageCount = messageCount;
    }

    /**
     * Initializes a new {@link MailFolderInfo}.
     * 
     * @param mailFolder The mail folder
     */
    public MailFolderInfo(final MailFolder mailFolder) {
        super();
        fullName = mailFolder.getFullname();
        messageCount = mailFolder.isHoldsMessages() ? mailFolder.getMessageCount() : -1;
    }

    /**
     * Gets the full name
     * 
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the message count
     * 
     * @return The message count
     */
    public int getMessageCount() {
        return messageCount;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append('(');
        if (fullName != null) {
            builder.append("fullName=").append(fullName).append(", ");
        }
        builder.append("messageCount=").append(messageCount).append(')');
        return builder.toString();
    }

}
