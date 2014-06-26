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

package com.openexchange.mail.smal.impl.adapter.solrj.contentgrab;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.impl.SmalExceptionCodes;
import com.openexchange.mail.smal.impl.adapter.solrj.SolrConstants;
import com.openexchange.session.Session;

/**
 * {@link TextFiller} - A test filler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextFiller implements SolrConstants {

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
     * Gets the filler for specified document.
     *
     * @param document The document
     * @return The filler
     * @throws OXException If creating filler fails
     */
    public static TextFiller fillerFor(final SolrDocument document) throws OXException {
        return new TextFiller(
            document.getFieldValue(FIELD_UUID).toString(),
            document.getFieldValue(FIELD_ID).toString(),
            document.getFieldValue(FIELD_FULL_NAME).toString(),
            TextFiller.<Integer> get(FIELD_ACCOUNT, document).intValue(),
            TextFiller.<Long> get(FIELD_USER, document).intValue(),
            TextFiller.<Long> get(FIELD_CONTEXT, document).intValue());
    }

    @SuppressWarnings("unchecked")
    private static <V> V get(final String name, final SolrDocument document) throws OXException {
        final Object object = document.getFieldValue(name);
        try {
            return (V) object;
        } catch (final ClassCastException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
        }
    }

    /**
     * Gets the filler for specified input document.
     *
     * @param inputDocument The input document
     * @return The filler
     * @throws OXException If creating filler fails
     */
    public static TextFiller fillerFor(final SolrInputDocument inputDocument) throws OXException {
        return new TextFiller(
            inputDocument.getFieldValue("uuid").toString(),
            inputDocument.getFieldValue("id").toString(),
            inputDocument.getFieldValue("full_name").toString(),
            TextFiller.<Integer> get("account", inputDocument).intValue(),
            TextFiller.<Long> get("user", inputDocument).intValue(),
            TextFiller.<Long> get("context", inputDocument).intValue());
    }

    @SuppressWarnings("unchecked")
    private static <V> V get(final String name, final SolrInputDocument inputDocument) throws OXException {
        final Object object = inputDocument.getFieldValue(name);
        try {
            return (V) object;
        } catch (final ClassCastException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
        }
    }

    private final String uuid;

    private final String mailId;

    private final String fullName;

    private final int accountId;

    private final int userId;

    private final int contextId;

    private final int hash;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + contextId;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((mailId == null) ? 0 : mailId.hashCode());
        result = prime * result + userId;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        hash = result;
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

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TextFiller)) {
            return false;
        }
        final TextFiller other = (TextFiller) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (contextId != other.contextId) {
            return false;
        }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return false;
            }
        } else if (!mailId.equals(other.mailId)) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
