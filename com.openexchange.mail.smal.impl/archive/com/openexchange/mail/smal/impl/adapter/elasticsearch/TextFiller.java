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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import java.util.Map;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link TextFiller} - A test filler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextFiller {

    /**
     * Gets the filler for specified arguments.
     *
     * @param uuid The UUID
     * @param mail The mail
     * @param session The session
     * @return A new filler
     */
    public static TextFiller fillerFor(final String uuid, final MailMessage mail, final Session session) {
        return new TextFiller(uuid, mail.getMailId(), mail.getFolder(), mail.getAccountId(), session.getUserId(), session.getContextId());
    }

    /**
     * Gets the filler for specified JSON object.
     *
     * @param jsonObject The JSON object
     * @param contextId The context identifier
     * @return The text filler
     */
    public static TextFiller fillerFor(final Map<String, Object> jsonObject, final int contextId) {
        final int accountId = ((Integer) jsonObject.get(Constants.FIELD_ACCOUNT_ID)).intValue();
        final int userId = ((Integer) jsonObject.get(Constants.FIELD_USER_ID)).intValue();
        return new TextFiller((String) jsonObject.get(Constants.FIELD_UUID), (String) jsonObject.get(Constants.FIELD_ID), (String) jsonObject.get(Constants.FIELD_FULL_NAME), accountId, userId, contextId);
    }

    private final String uuid;

    private final String mailId;

    private final String fullName;

    private final int accountId;

    private final int userId;

    private final int contextId;

    /**
     * The counter reflects the number of re-enqueue operations.
     */
    public volatile int queuedCounter;

    /**
     * Initializes a new {@link TextFiller}.
     *
     * @param uuid
     * @param mailId
     * @param fullName
     * @param accountId
     * @param userId
     * @param contextId
     */
    protected TextFiller(final String uuid, final String mailId, final String fullName, final int accountId, final int userId, final int contextId) {
        super();
        queuedCounter = 0;
        this.uuid = uuid;
        this.mailId = mailId;
        this.fullName = fullName;
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Gets the UUID
     *
     * @return The UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Gets the mail identifier
     *
     * @return The mail identifier
     */
    public String getMailId() {
        return mailId;
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
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append('(');
        if (uuid != null) {
            builder.append("uuid=").append(uuid).append(", ");
        }
        if (mailId != null) {
            builder.append("mailId=").append(mailId).append(", ");
        }
        if (fullName != null) {
            builder.append("fullName=").append(fullName).append(", ");
        }
        builder.append("accountId=").append(accountId).append(", userId=").append(userId).append(", contextId=").append(contextId).append(
            ')');
        return builder.toString();
    }

}
