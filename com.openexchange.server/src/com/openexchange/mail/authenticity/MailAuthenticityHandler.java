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

package com.openexchange.mail.authenticity;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityHandler} - Checks the authenticity of a given mail message.
 * <p>
 * Results are stored to {@link MailMessage#getAuthenticityResult()}.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MailAuthenticityHandler {

    /**
     * Handles the specified mail message. Extracts the mail headers from the mail message
     * and checks if the 'Authentication-Results' header is present. If it is, then parses that header
     * and collects the results of the different {@link MailAuthenticityMechanism}s that might be present
     * in a {@link MailAuthenticityResult} object and returns that
     *
     * @param session The session providing user data
     * @param mailMessage The mail message to handle
     * @throws OXException If checking/verifying mail authenticity fails
     */
    void handle(Session session, MailMessage mailMessage) throws OXException;

    /**
     * Returns an unmodifiable {@link Collection} with all required {@link MailField}s
     *
     * @return an unmodifiable{@link Collection} with all required {@link MailField}s
     */
    Collection<MailField> getRequiredFields();

    /**
     * Returns an unmodifiable {@link Collection} with all required mail headers
     *
     * @return an unmodifiable {@link Collection} with all required mail headers
     */
    Collection<String> getRequiredHeaders();

    /**
     * Determines whether the {@link MailAuthenticityHandler} is enabled for the user
     * that is denoted by the specified {@link Session}
     *
     * @param session The groupware {@link Session}
     * @return <code>true</code> if the {@link MailAuthenticityHandler} is enabled; <code>false</code> otherwise
     */
    boolean isEnabled(Session session);

    /**
     * Returns the ranking of this {@link MailAuthenticityHandler}. A higher number in ranking
     * means a higher priority.
     *
     * @return The ranking of the {@link MailAuthenticityHandler}
     */
    int getRanking();
}
