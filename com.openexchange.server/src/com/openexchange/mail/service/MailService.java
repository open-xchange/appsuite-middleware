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

package com.openexchange.mail.service;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link MailService} - The mail service to obtain both an appropriate instance of {@link MailAccess} for accessing mail system and an
 * appropriate instance of {@link MailTransport} for sending mails.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface MailService {

    /**
     * Gets an appropriate instance of {@link MailAccess mail access} parameterized with given session.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link MailAccess#connect()} method is supposed to be invoked.
     * On finished work the final {@link MailAccess#close(boolean)} must be called in order to release resources:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = mailService.getMailAccess(session);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param session The session
     * @param accountId The account ID
     * @return An appropriate instance of {@link MailAccess}
     * @throws OXException If an appropriate instance of {@link MailAccess mail access} cannot be initialized
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(Session session, int accountId) throws OXException;

    /**
     * Gets an appropriate instance of {@link MailAccess mail access} parameterized with given session.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link MailAccess#connect()} method is supposed to be invoked.
     * On finished work the final {@link MailAccess#close(boolean)} must be called in order to release resources:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = mailService.getMailAccess(...);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param accountId The account ID
     * @return An appropriate instance of {@link MailAccess}
     * @throws OXException If an appropriate instance of {@link MailAccess mail access} cannot be initialized
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(int userId, int contextId, int accountId) throws OXException;

    /**
     * Gets an appropriate instance of {@link MailTransport mail transport} parameterized with given session.
     * <p>
     * Note: Don't forget to call final {@link MailTransport#close()} on obtained {@link MailTransport mail transport}:
     *
     * <pre>
     * final MailTransport mailTransport = mailService.getMailTransport(session);
     * try {
     *     // Do something
     * } finally {
     *     mailTransport.close();
     * }
     * </pre>
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     * @return An appropriate instance of {@link MailTransport}
     * @throws OXException If an appropriate instance of {@link MailTransport mail transport} cannot be initialized
     */
    public MailTransport getMailTransport(Session session, int accountId) throws OXException;

    /**
     * Gets the mail configuration for session-associated user and given account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return The mail configuration
     * @throws OXException If mail configuration cannot be returned
     */
    public MailConfig getMailConfig(Session session, int accountId) throws OXException;

    /**
     * Gets the transport configuration for session-associated user and given account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return The transport configuration
     * @throws OXException If transport configuration cannot be returned
     */
    public TransportConfig getTransportConfig(Session session, int accountId) throws OXException;

    /**
     * Gets the mail login for the given account of specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @return The mail login
     * @throws OXException If mail login cannot be returned
     */
    public String getMailLoginFor(int userId, int contextId, int accountId) throws OXException;

}
