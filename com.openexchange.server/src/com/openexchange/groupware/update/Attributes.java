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

package com.openexchange.groupware.update;

import static com.openexchange.groupware.update.UpdateConcurrency.BLOCKING;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;

/**
 * Default database update task attributes. This represents how old database update tasks worked.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Attributes implements TaskAttributes {

    private final UpdateConcurrency concurrency;

    private final WorkingLevel level;

    /**
     * Default attributes for an update tasks are a blocking update task working on schema level.
     */
    public Attributes() {
        this(BLOCKING, SCHEMA);
    }

    /**
     * Initializes a new attributes with given concurrency behavior and working level.
     *
     * @param concurrency The concurrency behavior
     * @param level The working level
     */
    public Attributes(UpdateConcurrency concurrency, WorkingLevel level) {
        super();
        this.concurrency = concurrency;
        this.level = level;
    }

    /**
     * Initializes a new attributes with given concurrency behavior on schema level.
     *
     * @param concurrency The concurrency behavior
     */
    public Attributes(UpdateConcurrency concurrency) {
        this(concurrency, SCHEMA);
    }

    @Override
    public UpdateConcurrency getConcurrency() {
        return concurrency;
    }

    @Override
    public WorkingLevel getLevel() {
        return level;
    }
}
