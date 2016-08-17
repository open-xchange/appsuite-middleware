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

package com.openexchange.realtime.cleanup;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentHashSet;

/**
 * {@link AbstractJanitors} - Collection of RealtimeJanitors on bundle level.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class AbstractJanitors {

    private final Set<RealtimeJanitor> janitors;

    /**
     * Initializes a new {@link AbstractJanitor}.
     *
     * @param janitors
     */
    protected AbstractJanitors() {
        super();
        janitors = new ConcurrentHashSet<RealtimeJanitor>();
    }

    /**
     * Reset all states to uninitialized.
     */
    public void cleanup() throws OXException {
        janitors.clear();
    }

    /**
     * Add a new RealtimeJanitor
     *
     * @param janitor The {@link RealimteJanitor} to add
     * @return true if the object was successfully added, false if an Object with the same name already exists.
     */
    public boolean addJanitor(RealtimeJanitor janitor) {
        return janitors.add(janitor);
    }

    /**
     * Remove a RealtimeJanitor
     *
     * @param janitor The {@link RealimteJanitor} to remove
     * @return true if the object was successfully removed
     */
    public boolean removeJanitor(RealtimeJanitor janitor) {
        return janitors.remove(janitor);
    }
    
    /**
     * Get an unmodifiable view of the currently known {@link RealtimeJanitor}s.
     * @return an unmodifiable {@link Collection} containing the currently known {@link RealtimeJanitor}s.
     */
    public Collection<RealtimeJanitor> getJanitors() {
        return Collections.unmodifiableCollection(janitors);
    }

}
