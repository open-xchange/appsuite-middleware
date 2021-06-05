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

package com.openexchange.log.audit.slf4j;

import com.openexchange.log.audit.Attribute;
import com.openexchange.log.audit.AuditLogFilter;


/**
 * {@link NoHarmAuditLogFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class NoHarmAuditLogFilter implements AuditLogFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NoHarmAuditLogFilter.class);

    private final AuditLogFilter filter;

    /**
     * Initializes a new {@link NoHarmAuditLogFilter}.
     */
    public NoHarmAuditLogFilter(AuditLogFilter filter) {
        super();
        this.filter = filter;
    }

    @Override
    public boolean accept(String eventId, Attribute<?>[] attributes) {
        try {
            return filter.accept(eventId, attributes);
        } catch (Exception e) {
            LOGGER.error("Failed filter invocation for {}", filter.getClass().getName(), e);
            // We don't know better...
            return true;
        }
    }

}
