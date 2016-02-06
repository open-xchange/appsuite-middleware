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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.sms;

import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link SMSService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public interface SMSService {
    
    /**
     * Send a SMS message to a recipient
     *
     * @param recipient Phone number to send the message
     * @param message The message
     * @throws OXException
     */
    public void sendMessage(String recipient, String message) throws OXException;

    /**
     * Send a SMS message to a recipient
     *
     * @param recipient Phone number to send the message
     * @param message The message
     * @param locale Locale of recipient's phone number to parse into correct format
     * @throws OXException
     */
    public void sendMessage(String recipient, String message, Locale locale) throws OXException;

    /**
     * Send a SMS message to a recipient
     *
     * @param recipient Phone number to send the message
     * @param message The message
     * @param languageTag Language tag of recipient's phone number to parse into correct format
     * @throws OXException
     */
    public void sendMessage(String recipient, String message, String languageTag) throws OXException;

    /**
     * Send a SMS message to recipients
     *
     * @param recipients Phone numbers to send the message
     * @param message The message
     * @throws OXException
     */
    public void sendMessage(String[] recipients, String message) throws OXException;
    
    /**
     * Send a SMS message to recipients
     *
     * @param recipients Phone numbers to send the message
     * @param message The message
     * @param locale Locales of recipients' phone numbers to parse into correct format
     * @throws OXException
     */
    public void sendMessage(String[] recipients, String message, Locale[] locale) throws OXException;

    /**
     * Send a SMS message to recipients
     *
     * @param recipients Phone numbers to send the message
     * @param message The message
     * @param languageTags Language tags of recipients' phone numbers to parse into correct format
     * @throws OXException
     */
    public void sendMessage(String[] recipients, String message, String[] languageTags) throws OXException;

}
