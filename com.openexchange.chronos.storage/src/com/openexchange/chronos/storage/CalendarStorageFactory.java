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

package com.openexchange.chronos.storage;

import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarStorageFactory {

    /**
     * Initializes a new {@link CalendarStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     */
    CalendarStorage create(Context context, int accountId, EntityResolver entityResolver) throws OXException;

    /**
     * Initializes a new {@link CalendarStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    CalendarStorage create(Context context, int accountId, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException;

    /**
     * Wraps a calendar storage into a special <i>resilient</i> calendar storage that tries to automatically handle SQL <i>truncation</i>
     * and <i>incorrect string</i> warnings by adjusting the affected strings, and retrying the operation. Additionally, no exceptions are
     * raised when trying to store properties or property values that are not supported by the storage.
     *
     * @param storage The calendar storage to wrap
     * @return The wrapped calendar storage
     * @see CalendarParameters#PARAMETER_IGNORE_STORAGE_WARNINGS
     */
    CalendarStorage makeResilient(CalendarStorage storage);

}
