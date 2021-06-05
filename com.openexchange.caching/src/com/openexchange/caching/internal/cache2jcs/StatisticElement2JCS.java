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

package com.openexchange.caching.internal.cache2jcs;

import org.apache.jcs.engine.stats.behavior.IStatElement;
import com.openexchange.caching.StatisticElement;

/**
 * {@link StatisticElement2JCS} - The {@link StatisticElement} implementation backed by a {@link IStatElement} object; meaning all
 * invocations are delegated to specified {@link IStatElement} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StatisticElement2JCS implements StatisticElement {

    private final IStatElement statElement;

    /**
     * Initializes a new {@link StatisticElement2JCS}
     *
     * @param statElement The {@link IStatElement} object to delegate to
     */
    public StatisticElement2JCS(final IStatElement statElement) {
        super();
        this.statElement = statElement;
    }

    @Override
    public String getData() {
        return statElement.getData();
    }

    @Override
    public String getName() {
        return statElement.getName();
    }

    @Override
    public void setData(final String data) {
        statElement.setData(data);
    }

    @Override
    public void setName(final String name) {
        statElement.setName(name);
    }

}
