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

package com.openexchange.share.impl.cleanup;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.user.UserService;

/**
 * {@link GuestLastModifiedMarker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestLastModifiedMarker {

    private static final Logger LOG = LoggerFactory.getLogger(GuestLastModifiedMarker.class);
    private static final String GUEST_LAST_MODIFIED_ATTRIBUTE = "com.openexchange.guestLastModified";

    /**
     * Gets the currently set value for the {@link #GUEST_LAST_MODIFIED_ATTRIBUTE} of the guest user.
     *
     * @param guestUser The guest user to get the attribute value for
     * @return The last-modified date, or <code>null</code> if currently not set
     */
    public static Date getLastModified(User guestUser) {
        String value = ShareTool.getUserAttribute(guestUser, GUEST_LAST_MODIFIED_ATTRIBUTE);
        if (null != value) {
            try {
                return new Date(Long.valueOf(value));
            } catch (NumberFormatException e) {
                LOG.warn("Ignoring invalid value in \"{}\"", GUEST_LAST_MODIFIED_ATTRIBUTE, e);
            }
        }
        return null;
    }

    /**
     * Updates or sets the {@link #GUEST_LAST_MODIFIED_ATTRIBUTE} of the guest user to the supplied date, in case it's different from
     * the current value.
     *
     * @param services A service lookup reference
     * @param context The context
     * @param guestUser The guest user to set the attribute for
     * @param lastModified The last-modified date to set, or <code>null</code> to clear a previously set value
     * @return <code>true</code> if the date was updated, <code>false</code>, otherwise
     */
    public static boolean updateLastModified(ServiceLookup services, Context context, User guestUser, Date lastModified) throws OXException {
        Date currentLastModified = getLastModified(guestUser);
        if (null == currentLastModified && null != lastModified ||
            null != currentLastModified && null == lastModified ||
            null != currentLastModified && null != lastModified && false == currentLastModified.equals(lastModified)) {
            String value = null == lastModified ? null : String.valueOf(lastModified.getTime());
            services.getService(UserService.class).setAttribute(GUEST_LAST_MODIFIED_ATTRIBUTE, value, guestUser.getId(), context);
            return true;
        }
        return false;
    }

    /**
     * Removes the {@link #GUEST_LAST_MODIFIED_ATTRIBUTE} of the guest user.
     *
     * @param services A service lookup reference
     * @param context The context
     * @param guestUser The guest user to remove the attribute for
     * @return <code>true</code> if the date was updated, <code>false</code>, otherwise
     */
    public static boolean clearLastModified(ServiceLookup services, Context context, User guestUser) throws OXException {
        return updateLastModified(services, context, guestUser, null);
    }

}

