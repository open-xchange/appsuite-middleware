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

package com.openexchange.caching.internal.cache2jcs;

import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.jcs2cache.JCSStatElementDelegator;

/**
 * {@link Statistics2JCS} - The {@link Statistics} implementation backed by a {@link IStats} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Statistics2JCS implements Statistics {

    private static final long serialVersionUID = -9146499976597779961L;

    protected final IStats stats;

    /**
     * Initializes a new {@link Statistics2JCS}
     *
     * @param stats The {@link IStats} object to delegate to
     */
    public Statistics2JCS(final IStats stats) {
        super();
        this.stats = stats;
    }

    @Override
    public StatisticElement[] getStatElements() {
        final IStatElement[] elems = stats.getStatElements();
        if (elems == null) {
            return null;
        }
        final StatisticElement[] retval = new StatisticElement[elems.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new StatisticElement2JCS(elems[i]);
        }
        return retval;
    }

    @Override
    public String getTypeName() {
        return stats.getTypeName();
    }

    @Override
    public void setStatElements(final StatisticElement[] statisticElements) {
        if (statisticElements == null) {
            stats.setStatElements(null);
            return;
        }
        final IStatElement[] elems = new IStatElement[statisticElements.length];
        for (int i = 0; i < elems.length; i++) {
            elems[i] = new JCSStatElementDelegator(statisticElements[i]);
        }
        stats.setStatElements(elems);
    }

    @Override
    public void setTypeName(final String name) {
        stats.setTypeName(name);
    }

}
