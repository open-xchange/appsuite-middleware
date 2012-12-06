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

package com.openexchange.mail.smal.impl.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link IndexDocumentHelper} - Helper to get <code>IndexDocument</code>s from <code>MailMessage</code>s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexDocumentHelper {

    /**
     * Initializes a new {@link IndexDocumentHelper}.
     */
    private IndexDocumentHelper() {
        super();
    }

    /**
     * Gets the message provided by specified document
     * 
     * @param documents The document
     * @return The message or <code>null</code>
     */
    public static MailMessage messageFrom(final IndexDocument<MailMessage> document) {
        if (null == document) {
            return null;
        }
        return document.getObject();
    }

    /**
     * Gets the messages provided by specified documents
     * 
     * @param documents The documents
     * @return The messages
     */
    public static List<MailMessage> messagesFrom(final Collection<IndexDocument<MailMessage>> documents) {
        if (null == documents || documents.isEmpty()) {
            return Collections.emptyList();
        }
        final List<MailMessage> messages = new ArrayList<MailMessage>(documents.size());
        for (final IndexDocument<MailMessage> document : documents) {
            messages.add(document.getObject());
        }
        return messages;
    }

    /**
     * Gets the index documents for given mails.
     * 
     * @param mails The mails
     * @param accountId The account identifier
     * @return The index documents
     */
    public static List<IndexDocument<MailMessage>> documentsFor(final Collection<MailMessage> mails, final int accountId) {
        if (null == mails || mails.isEmpty()) {
            return Collections.<IndexDocument<MailMessage>> emptyList();
        }
        final List<IndexDocument<MailMessage>> list = new ArrayList<IndexDocument<MailMessage>>(mails.size());
        for (final MailMessage mail : mails) {
            mail.setAccountId(accountId);
            list.add(new StandardIndexDocument<MailMessage>(mail));
        }
        return list;
    }

    /**
     * Gets the index document for given mail.
     * 
     * @param mail The mail
     * @param accountId The account identifier
     * @return The index document
     */
    public static IndexDocument<MailMessage> documentFor(final MailMessage mail, final int accountId) {
        if (null == mail) {
            return null;
        }
        mail.setAccountId(accountId);
        return new StandardIndexDocument<MailMessage>(mail);
    }

}
