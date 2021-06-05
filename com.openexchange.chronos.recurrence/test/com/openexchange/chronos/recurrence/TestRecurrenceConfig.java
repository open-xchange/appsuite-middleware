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

package com.openexchange.chronos.recurrence;

import com.openexchange.chronos.recurrence.service.RecurrenceConfig;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.config.lean.SimLeanConfigurationService;

/**
 * {@link TestRecurrenceConfig}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class TestRecurrenceConfig extends RecurrenceConfig {

    private final int calculationLimit;

    /**
     * Initializes a new {@link TestRecurrenceConfig}.
     */
    public TestRecurrenceConfig() {
        this(1001);
    }

    /**
     * Initializes a new {@link TestRecurrenceConfig}.
     *
     * @param calculationLimit The calculation limit to use
     */
    public TestRecurrenceConfig(int calculationLimit) {
        super(new SimLeanConfigurationService(new SimConfigurationService()));
        this.calculationLimit = calculationLimit;
    }

    @Override
    public int getCalculationLimit() {
        return calculationLimit;
    }

}
