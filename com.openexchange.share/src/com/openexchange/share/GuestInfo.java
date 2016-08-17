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

package com.openexchange.share;

import java.util.Date;
import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.recipient.RecipientType;

/**
 * Handles the user specific parts of a share
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface GuestInfo {

    /**
     * Gets the authentication mode used for the guest user.
     *
     * @return The authentication mode
     */
    AuthenticationMode getAuthentication();

    /**
     * Gets the base token associated with the guest user.
     *
     * @return The base token
     */
    String getBaseToken();

    /**
     * Gets the e-mail address of the guest user if it denotes a named recipient.
     *
     * @return The e-mail address of the named share recipient, or <code>null</code> if the guest user is anonymous
     */
    String getEmailAddress();

    /**
     * Gets the password of the guest user in case the share recipient is anonymous and a password is required to access the share.
     *
     * @return The password of the anonymous share recipient, or <code>null</code> if no password is set or the guest user is not anonymous
     */
    String getPassword();

    /**
     * Gets the expiry date of the guest user in case the share recipient is anonymous and an expiry date is set for the share.
     *
     * @return The expiry date of the anonymous share recipient, or <code>null</code> if no expiry date is set or the guest user is not anonymous
     */
    Date getExpiryDate();

    /**
     * Gets the recipient type of the guest user.
     *
     * @return The recipient type
     */
    RecipientType getRecipientType();

    /**
     * Gets the user identifier of the guest.
     *
     * @return The guest user identifier
     */
    int getGuestID();

    /**
     * Gets the identifier of the context this guest user belongs to.
     *
     * @return The context identifier
     */
    int getContextID();

    /**
     * Gets the identifier of the user who initially created the guest user.
     *
     * @return The identifier of the user who initially created the guest
     */
    int getCreatedBy();

    /**
     * Gets the display name of the underlying guest user.
     *
     * @return The display name or <code>null</code> if the guest is anonymous or none is set.
     */
    String getDisplayName();

    /**
     * Gets the guest user's configured locale.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Returns the share target for an anonymous guest.
     *
     * @return The target or <code>null</code> if the guest is not anonymous
     */
    ShareTarget getLinkTarget();

    /**
     * Generates a share link to a specific target appropriate for the guest.
     *
     * @param hostData Host data
     * @param targetPath The share target path to create the link for, or <code>null</code> to generate a "base" link only
     * @return The share link
     */
    String generateLink(HostData hostData, ShareTargetPath targetPath);

}
