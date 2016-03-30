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


package com.openexchange.messaging.sms.service;

import java.util.List;

/**
 * An interface describing what information a user configuration has to provide
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface MessagingUserConfigurationInterface {

    /**
     * Get the list of allowed sender addresses
     * @return
     */
    public List<String> getAddresses();

    /**
     * TODO
     * @return
     */
    public String getDisplayString();

    /**
     * Get the maximum allowed length of the message
     * @return
     */
    public int getLength();

    /**
     * If the messaging service is enabled for the user or not
     *
     * @return
     */
    public boolean isEnabled();

    /**
     * If the message service uses captchas
     * @return
     */
    public boolean isCaptcha();

    /**
     * If the backend is allowed to send multiple SMS, if yes, the GUI shows a counter for the number of SMS messages to be sent
     * @return
     */
    public boolean getMultiSMS();

    /**
     * If the backend is allowed to send MMS messages, if yes, the GUI allows to upload images
     * @return
     */
    public boolean isMMS();

    /**
     * Returns an optional Upsell link, if the user has no SMS enabled.
     * @return
     */
    public String getUpsellLink();

    /**
     * If the user should have the option to append a signature to the outgoing SMS
     * @return
     */
    public boolean isSignatureOption();

    /**
     * Returns the max. number of of recipients, use 0 for unlimited
     * @return
     */
    public int getRecipientLimit();

    /**
     * Returns the max. number of of sms, use 0 for unlimited
     * @return
     */
    public int getSmsLimit();

    /**
     * Return the RegEx which will be used to clean numbers in the GUI. Can be null if the default should be used
     * @return
     */
    public String getNumCleanRegEx();
}
