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

package com.openexchange.caching.internal.jcs2cache;

import org.apache.jcs.engine.stats.StatElement;
import com.openexchange.caching.StatisticElement;

/**
 * {@link JCSStatElementDelegator} - A {@link StatElement} backed by a {@link StatisticElement} object to which all invocations are
 * delegated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSStatElementDelegator extends StatElement {

    private final StatisticElement statisticElement;

    /**
     * Initializes a new {@link JCSStatElementDelegator}
     */
    public JCSStatElementDelegator(final StatisticElement statisticElement) {
        super();
        this.statisticElement = statisticElement;
    }

    @Override
    public String getName() {
        return statisticElement.getName();
    }

    @Override
    public void setName(final String name) {
        statisticElement.setName(name);
    }

    @Override
    public String getData() {
        return statisticElement.getData();
    }

    @Override
    public void setData(final String data) {
        statisticElement.setData(data);
    }

}
