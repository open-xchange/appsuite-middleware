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

package com.openexchange.sms.tools;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SMSBucketService} provides a user based token-bucket for sms tokens
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface SMSBucketService {

    /**
     * Retrieves the number of available sms tokens for the given user and reduce the amount by one.
     * 
     * @param session The user session
     * @return The previous amount of sms tokens
     * @throws OXException if it was unable to retrieve the sms token or if the sms limit is reached
     */
    public int getSMSToken(Session session) throws OXException;

    /**
     * Checks if the user sms limit is enabled for the given user
     * @param session The user session
     * @return true if SMSUserLimit is enabled, false otherwise
     * @throws OXException
     */
    public boolean isEnabled(Session session) throws OXException;

    /**
     * Retrieves the refresh interval in hours rounded up
     * 
     * @param session The user session
     * @return The time in hours rounded up
     * @throws OXException if it was unable to retrieve the interval
     */
    public int getRefreshInterval(Session session) throws OXException;
}
