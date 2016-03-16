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

package com.openexchange.quota;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * A {@link QuotaProvider} must be implemented by every module that wants
 * to contribute to the general {@link QuotaService}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface QuotaProvider {

    /**
     * Gets the id of the corresponding module. A modules id must always be
     * unique.
     *
     * @return The modules id; never <code>null</code>.
     */
    String getModuleID();

    /**
     * Gets the modules name that may be displayed within a client application.
     * The name should be localizable, that means it should be a valid key for
     * the I18nService.
     *
     * @return The name, never <code>null</code>.
     */
    String getDisplayName();

    /**
     * Gets the quota and usage for a session-specific user and a given account.
     *
     * @param session The session, never <code>null</code>.
     * @param account The id of a possible account for the user within this module,
     *  never <code>null</code>.
     * @return The quota and usage, never <code>null</code>.
     * @throws OXException If no account was found for the given id, {@link QuotaExceptionCodes.UNKNOWN_ACCOUNT}
     * is thrown. Other exception codes denote ocurred errors while calculating quota and usage.
     */
    AccountQuota getFor(Session session, String accountID) throws OXException;

    /**
     * Gets the quota and usage for all accounts for the session-specific
     * user within this module.
     *
     * @param session The session, never <code>null</code>.
     * @return A list of quotas and usages. Never <code>null</code> but possibly
     *  empty, if no account exists.
     * @throws OXException If an error occurs while calculating quota and usage.
     */
    List<AccountQuota> getFor(Session session) throws OXException;

}
