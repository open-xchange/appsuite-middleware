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

import javax.mail.Message;
import com.openexchange.exception.OXException;

/**
 * {@link IMailMessageStorageMimeSupport} - Extends basic message storage by MIME support.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageMimeSupport extends IMailMessageStorage {

    /**
     * Indicates if MIME messages are supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isMimeSupported() throws OXException;

    /**
     * Appends given MIME messages to given folder.
     *
     * @param destFolder The destination folder
     * @param msgs The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If MIME messages cannot be appended.
     */
    String[] appendMimeMessages(String destFolder, Message[] msgs) throws OXException;

    /**
     * Gets the denoted MIME message.
     *
     * @param fullName The folder full name
     * @param id The message identifier
     * @param markSeen Whether to mark as seen or not
     * @return The denoted MIME message
     * @throws OXException If MIME message cannot be returned
     */
    Message getMimeMessage(String fullName, final String id, boolean markSeen) throws OXException;

}
