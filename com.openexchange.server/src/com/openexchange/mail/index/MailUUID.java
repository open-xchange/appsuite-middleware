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

package com.openexchange.mail.index;

import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailUUID} - Represents a mail's UUID in index storage.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailUUID {

    private final String mailUUID;
    
    private final int contextId;
    
    private final int userId;
    
    private final int accountId;
    
    private final String fullName;
    
    private final String mailId;
    

    /**
     * Initializes a new {@link MailUUID}.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param fullName The folder full name
     * @param mailId The mail identifier
     */
    private MailUUID(final int contextId, final int userId, final int accountId, final String fullName, final String mailId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.accountId = accountId;
        this.fullName = fullName;
        this.mailId = mailId;
        StringBuilder tmp = new StringBuilder(64);
        tmp.append("mail/").append(contextId).append(MailPath.SEPERATOR).append(userId).append(MailPath.SEPERATOR);
        tmp.append(MailPath.getMailPath(accountId, fullName, mailId));
        mailUUID = tmp.toString();
    }
    
    public static MailUUID newUUID(int contextId, int userId, int accountId, String fullName, String mailId) {
        return new MailUUID(contextId, userId, accountId, fullName, mailId);
    }
    
    public static MailUUID newUUID(int contextId, int userId, MailMessage message) {
        return new MailUUID(contextId, userId, message.getAccountId(), message.getFolder(), message.getMailId());
    }    
    
    public final int getContextId() {
        return contextId;
    }
    
    public final int getUserId() {
        return userId;
    }
    
    public final int getAccountId() {
        return accountId;
    }
    
    public final String getFullName() {
        return fullName;
    }
    
    public final String getMailId() {
        return mailId;
    }

    @Override
    public String toString() {
        return mailUUID;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mailUUID == null) ? 0 : mailUUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MailUUID other = (MailUUID) obj;
        if (mailUUID == null) {
            if (other.mailUUID != null)
                return false;
        } else if (!mailUUID.equals(other.mailUUID))
            return false;
        return true;
    }

}
