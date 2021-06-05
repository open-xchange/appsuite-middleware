/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.impl.cleanup;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.user.User;
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
                return new Date(Long.parseLong(value));
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

