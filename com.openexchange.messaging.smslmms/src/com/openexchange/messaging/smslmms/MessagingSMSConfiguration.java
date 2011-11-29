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

package com.openexchange.messaging.smslmms;

import java.util.List;
import java.util.Map;

/**
 * {@link MessagingSMSConfiguration} - The user configuration for a SMS/MMS account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingSMSConfiguration {

    /**
     * The property name for addresses.
     */
    public static final String PROP_ADDRESSES = "addresses";

    /**
     * The property name for display string.
     */
    public static final String PROP_DISPLAY_STRING = "displayString";

    /**
     * The property name for length.
     */
    public static final String PROP_LENGTH = "length";

    /**
     * The property name for enabled flag.
     */
    public static final String PROP_ENABLED = "enabled";

    /**
     * The property name for captcha flag.
     */
    public static final String PROP_CAPTCHA = "captcha";

    /**
     * The property name for mulit-SMS flag.
     */
    public static final String PROP_MULTI_SMS = "multiSMS";

    /**
     * The property name for MMS flag.
     */
    public static final String PROP_MMS = "mms";

    /**
     * The property name for upsell link.
     */
    public static final String PROP_UPSELL_LINK = "upsellLink";

    /**
     * Gets user's account configuration as a map.
     *
     * @return The configuration as a {@link Map}
     */
    public Map<String, Object> getConfiguration();

    /**
     * Gets the list of allowed sender addresses.
     * 
     * @return The list of allowed sender addresses
     */
    public List<String> getAddresses();

    /**
     * Gets the display string.
     * 
     * @return The display string
     */
    public String getDisplayString();

    /**
     * Gets the maximum allowed length of the message.
     * 
     * @return The maximum allowed length of the message
     */
    public int getLength();
    
    /**
     * Indicates if the messaging service is enabled for the user or not.
     * 
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean isEnabled();
    
    /**
     * Indicates if the message service uses captchas
     * 
     * @return <code>true</code> if using captchas; otherwsie <code>false</code>
     */
    public boolean isCaptcha();

    /**
     * Indicates if the backend is allowed to send multiple SMS, if yes, the GUI shows a counter for the number of SMS messages to be sent
     * 
     * @return <code>true</code> if multiple SMS are allowed; otherwise <code>false</code>
     */
    public boolean getMultiSMS();

    /**
     * Indicates if the backend is allowed to send MMS messages, if yes, the GUI allows to upload images.
     * 
     * @return <code>true</code> if MMS is allowed; otherwise <code>false</code>
     */
    public boolean isMMS();
    
    /**
     * Gets an optional upsell link, if the user has no SMS enabled.
     * 
     * @return The upsell link or <code>null</code>
     */
    public String getUpsellLink();
}
