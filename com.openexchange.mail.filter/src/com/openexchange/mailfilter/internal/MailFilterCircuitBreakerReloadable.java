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

package com.openexchange.mailfilter.internal;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;

/**
 * {@link MailFilterCircuitBreakerReloadable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MailFilterCircuitBreakerReloadable implements Reloadable {

    private final MailFilterServiceImpl mailFilterService;

    /**
     * Initializes a new {@link MailFilterCircuitBreakerReloadable}.
     *
     * @param mailFilterService The mail filter service
     */
    public MailFilterCircuitBreakerReloadable(MailFilterServiceImpl mailFilterService) {
        super();
        this.mailFilterService = mailFilterService;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.mail.filter.breaker.*");
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        mailFilterService.reinitBreaker(configService);
    }

}
