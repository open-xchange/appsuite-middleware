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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;

/**
 * {@link IMailMessageStorageEnhancedDeletion} - Extends basic folder storage by requesting a mailbox' conversation threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageEnhancedDeletion extends IMailMessageStorage {

    /**
     * Indicates if enhanced deletion is supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isEnhancedDeletionSupported() throws OXException;

    /**
     * Deletes the messages located in given folder identified through given mail IDs.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param hardDelete <code>true</code> to hard delete the messages, meaning not to create a backup copy of each message in default trash folder; otherwise <code>false</code>
     * @return The identifiers of those mails that were put into trash (if any)
     * @throws OXException If messages cannot be deleted.
     */
    MailPath[] deleteMessagesEnhanced(String folder, String[] mailIds, boolean hardDelete) throws OXException;

}
