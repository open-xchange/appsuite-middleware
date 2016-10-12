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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount;

import java.util.Locale;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link KnownStatus} - An enumeration for known statuses for a mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum KnownStatus implements Status {

    /**
     * The "OK" status. All fine.
     */
    OK("ok", KnownStatusMessage.MESSAGE_OK),
    /**
     * Referenced account currently carries invalid credentials and is therefore unable to connect. Credentials are supposed to be corrected by user.
     */
    INVALID_CREDENTIALS("invalid_credentials", KnownStatusMessage.MESSAGE_INVALID_CREDENTIALS),

    ;

    private final String id;
    private final String message;

    private KnownStatus(String id, String message) {
        this.id = id;
        this.message = message;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMessage(Locale locale) {
        return StringHelper.valueOf(null == locale ? Locale.US : locale).getString(message);
    }

    /**
     * Gets the status for given identifier
     *
     * @param identifier The status' identifier
     * @return The status or <code>null</code>
     */
    public static Status statusFor(String identifier) {
        if (null == identifier) {
            return null;
        }
        for (KnownStatus s : KnownStatus.values()) {
            if (identifier.equalsIgnoreCase(s.id)) {
                return s;
            }
        }
        return null;
    }
}
