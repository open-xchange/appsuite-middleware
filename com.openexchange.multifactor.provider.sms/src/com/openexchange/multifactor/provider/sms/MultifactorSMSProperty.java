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

package com.openexchange.multifactor.provider.sms;

import com.openexchange.config.lean.Property;
import com.openexchange.multifactor.MultifactorProperties;

/**
 * {@link MultifactorSMSProperty}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorSMSProperty implements Property {

    /**
     * Defines if the SMS provider is enabled.
     *
     * Only providers which are "enabled" can be used by a user.
     */
    enabled(false),

    /**
     * Defines if the SMS provider can also be used as "backup provider".
     * I.E. This defines if it's possible for a user to register SMS based devices as backup device.
     */
    backup(true),

    /**
     * Defines the length of the SMS token
     */
    tokenLength(8),

    /**
     * Defines the lifetime (in seconds) of the SMS token in minutes before it expires and cannot be used for authentication anymore.
     *
     */
    tokenLifetime(120),

    /**
     * The maximum amount of active tokens a user is allowed to own at a point in time
     */
    maxTokenAmount(5);

    private static final String PREFIX = MultifactorProperties.PREFIX + "sms.";
    private Object defaultValue;

    MultifactorSMSProperty(Object defaultValue){
        this.defaultValue = defaultValue;
    }

    /* (non-Javadoc)
     * @see com.openexchange.config.lean.Property#getFQPropertyName()
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    /* (non-Javadoc)
     * @see com.openexchange.config.lean.Property#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
