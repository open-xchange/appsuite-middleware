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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link DefaultSMSMessagingConfiguration} - The default {@link SMSMessagingConfiguration configuration} implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultSMSMessagingConfiguration implements SMSMessagingConfiguration {

    private final Map<String, Object> configuration;

    private List<String> addresses;

    private String displayString;

    private int length;

    private Boolean enabled;

    private Boolean captcha;

    private Boolean multiSMS;

    private Boolean mms;

    private Boolean folderStorage;

    private String upsellLink;

    /**
     * Initializes a new {@link DefaultSMSMessagingConfiguration}.
     */
    public DefaultSMSMessagingConfiguration(final Map<String, Object> configuration) {
        super();
        this.configuration = configuration;
        length = -1;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public List<String> getAddresses() {
        if (null != addresses) {
            return addresses;
        }
        @SuppressWarnings("unchecked") final List<String> addresses = (List<String>) configuration.get(PROP_ADDRESSES);
        return null == addresses ? Collections.<String> emptyList() : addresses;
    }

    /**
     * Sets the addresses
     * 
     * @param addresses The addresses to set
     */
    public void setAddresses(final List<String> addresses) {
        this.addresses = new ArrayList<String>(addresses);
    }

    @Override
    public String getDisplayString() {
        return displayString == null ? ((String) configuration.get(PROP_DISPLAY_STRING)) : displayString;
    }

    /**
     * Sets the display string
     * 
     * @param displayString The display string to set
     */
    public void setDisplayString(final String displayString) {
        this.displayString = displayString;
    }

    @Override
    public int getLength() {
        if (length > 0) {
            return length;
        }
        final Integer len = (Integer) configuration.get(PROP_LENGTH);
        return null == len ? -1 : len.intValue();
    }

    /**
     * Sets the length
     * 
     * @param length The length to set
     */
    public void setLength(final int length) {
        this.length = length;
    }

    @Override
    public boolean isEnabled() {
        if (null != enabled) {
            return enabled.booleanValue();
        }
        final Boolean b = (Boolean) configuration.get(PROP_ENABLED);
        return null == b ? true : b.booleanValue();
    }

    /**
     * Sets the enabled flag.
     * 
     * @param enabled The enabled flag to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = Boolean.valueOf(enabled);
    }

    @Override
    public boolean isCaptcha() {
        if (null != captcha) {
            return captcha.booleanValue();
        }
        final Boolean b = (Boolean) configuration.get(PROP_CAPTCHA);
        return null == b ? true : b.booleanValue();
    }

    /**
     * Sets the captcha flag.
     * 
     * @param captcha The captcha flag to set
     */
    public void setCaptcha(final boolean captcha) {
        this.captcha = Boolean.valueOf(captcha);
    }

    @Override
    public boolean getMultiSMS() {
        if (null != multiSMS) {
            return multiSMS.booleanValue();
        }
        final Boolean b = (Boolean) configuration.get(PROP_MULTI_SMS);
        return null == b ? true : b.booleanValue();
    }

    /**
     * Sets the multiSMS flag.
     * 
     * @param multiSMS The multiSMS flag to set
     */
    public void setMultiSMS(final boolean multiSMS) {
        this.multiSMS = Boolean.valueOf(multiSMS);
    }

    @Override
    public boolean isMMS() {
        if (null != mms) {
            return mms.booleanValue();
        }
        final Boolean b = (Boolean) configuration.get(PROP_MMS);
        return null == b ? true : b.booleanValue();
    }

    /**
     * Sets the MMS flag.
     * 
     * @param mms The MMS flag to set
     */
    public void setMms(final boolean mms) {
        this.mms = Boolean.valueOf(mms);
    }

    @Override
    public boolean supportsFolderStorage() {
        if (null != folderStorage) {
            return folderStorage.booleanValue();
        }
        final Boolean b = (Boolean) configuration.get(PROP_FOLDER_STORAGE);
        return null == b ? true : b.booleanValue();
    }

    /**
     * Sets the folder storage flag.
     * 
     * @param folderStorage The flag to set
     */
    public void setFolderStorage(final boolean folderStorage) {
        this.folderStorage = Boolean.valueOf(folderStorage);
    }

    @Override
    public String getUpsellLink() {
        return upsellLink == null ? ((String) configuration.get(PROP_UPSELL_LINK)) : upsellLink;
    }

    /**
     * Sets the upsell link
     * 
     * @param upsellLink The upsell link to set
     */
    public void setUpsellLink(final String upsellLink) {
        this.upsellLink = upsellLink;
    }

}
