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

package com.openexchange.caching.internal.jcs2cache;

import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.cache2jcs.StatisticElement2JCS;

/**
 * {@link JCSStatsDelegator} - The {@link Stats} subclass backed by a {@link Statistics} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSStatsDelegator extends Stats {

    private static final long serialVersionUID = -1350875341961863476L;

    private final Statistics statistics;

    /**
     * Initializes a new {@link JCSStatsDelegator}
     *
     * @param statistics The {@link Statistics} object to delegate to
     */
    public JCSStatsDelegator(final Statistics statistics) {
        super();
        this.statistics = statistics;
    }

    @Override
    public IStatElement[] getStatElements() {
        final StatisticElement[] statisticElements = statistics.getStatElements();
        if (statisticElements == null) {
            return null;
        }
        final StatElement[] retval = new StatElement[statisticElements.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new JCSStatElementDelegator(statisticElements[i]);
        }
        return retval;
    }

    @Override
    public void setStatElements(final IStatElement[] stats) {
        if (stats == null) {
            statistics.setStatElements(null);
            return;
        }
        final StatisticElement[] statisticElements = new StatisticElement[stats.length];
        for (int i = 0; i < statisticElements.length; i++) {
            statisticElements[i] = new StatisticElement2JCS(stats[i]);
        }
        statistics.setStatElements(statisticElements);
    }

    @Override
    public String getTypeName() {
        return statistics.getTypeName();
    }

    @Override
    public void setTypeName(final String name) {
        statistics.setTypeName(name);
    }

}
