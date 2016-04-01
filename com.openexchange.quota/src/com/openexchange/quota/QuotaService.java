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
import com.openexchange.osgi.annotation.SingletonService;

/**
 * Open-Xchange consists of a set of modules that serve user requests.
 * Every module that allows users to store data may provide limits for a
 * certain amount of storage and a certain number of items that it will handle
 * for each user. In other words, every module can have user-specific quotas
 * for storage size and items. Those quotas may be set by definition or
 * by configuration and can also be unlimited. The responsibility to
 * enforce quotas lies within the modules themselves, but they can announce
 * their quotas via this service. That enables a client to provide a
 * combined overview over all quotas. Each module that wants to contribute
 * to this service has to implement a {@link QuotaProvider}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
@SingletonService
public interface QuotaService {

    /**
     * Gets all currently known {@link QuotaProvider}s.
     *
     * @return A list of providers. Never <code>null</code> but possibly empty.
     */
    List<QuotaProvider> getAllProviders();

    /**
     * Gets the provider for a specific module, if available.
     *
     * @param moduleID The modules unique identifier.
     * @return The modules provider or <code>null</code>, if unknown.
     */
    QuotaProvider getProvider(String moduleID);

}
