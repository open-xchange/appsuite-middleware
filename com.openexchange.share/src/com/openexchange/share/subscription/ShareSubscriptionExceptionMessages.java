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

package com.openexchange.share.subscription;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareSubscriptionExceptionMessages}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class ShareSubscriptionExceptionMessages implements LocalizableStrings {

    /** Unable to find a subscription for \"%1$s\" */
    public static final String MISSING_SUBSCRIPTION_MSG = "Unable to find a subscription for \"%1$s\"";

    /** The given link \"%1$s\" can't be resolved to a share. */
    public static final String NOT_USABLE_MSG = "The given link \"%1$s\" can't be resolved to a share.";

    /** You don't have enough permissions to perform the operation. */
    public static final String MISSING_PERMISSIONS_MSG = "You don't have enough permissions to perform the operation.";

    /** The folder %1$s belongs to a folder tree that is unsubscribed. */
    public static final String UNSUBSCRIEBED_FOLDER_MSG = "The folder %1$s belongs to a folder tree that is unsubscribed.";

    /** After unsubscribing from \"%1$s\", all folders from the account \"%2$s\" will be removed. */
    public static final String ACCOUNT_WILL_BE_REMOVED_MSG = "After unsubscribing from \"%1$s\", all folders from the account \"%2$s\" will be removed.";

    /**
     * Initializes a new {@link OXExceptionMessages}.
     */
    private ShareSubscriptionExceptionMessages() {
        super();
    }

}
