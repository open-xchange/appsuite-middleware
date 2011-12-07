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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.smslmms.api;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.session.Session;


/**
 * {@link SMSService} - The SMS/MMS service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SMSService {

    /**
     * The identifier for a user's default SMS/MMS account.
     */
    public static final int DEFAULT_ACCOUNT_IDENTIFIER = 0;

    /**
     * The service identifier.
     */
    public static final String SERVICE_ID = "com.openexchange.messaging.sms";

    /**
     * The display name.
     */
    public static final String DISPLAY_NAME = "SMS/MMS";

    /**
     * Gets the SMS/MMS configuration for the user associated with specified session.
     * 
     * @param accountId The account identifier
     * @param session The session providing user data
     * @return The SMS/MMS configuration
     * @throws OXException If configuration cannot be returned
     */
    SMSConfiguration getSMSConfiguration(int accountId, Session session) throws OXException;

    /**
     * Gets the account manager for this SMS/MMS service.
     *
     * @return The account manager
     */
    MessagingAccountManager getAccountManager();

    /**
     * Gets the SMS/MMS access for specified account identifier.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The account access for specified account identifier
     * @throws OXException If account access cannot be returned for given account identifier
     */
    SMSAccess getSMSAccess(int accountId, Session session) throws OXException;

    /**
     * Gets the SMS/MMS transport for specified account identifier.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The account transport for specified account identifier
     * @throws OXException If account transport cannot be returned for given account identifier
     */
    SMSTransport getSMSTransport(int accountId, Session session) throws OXException;
    
}
