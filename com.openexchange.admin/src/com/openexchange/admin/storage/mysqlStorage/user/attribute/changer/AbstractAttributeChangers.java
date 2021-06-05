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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * {@link AbstractAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractAttributeChangers implements AttributeChangers {

    protected static final Set<String> EMPTY_SET = Collections.emptySet();
    private final EnumSet<? extends Attribute> attributes;

    /**
     * Initialises a new {@link AbstractAttributeChangers}.
     */
    public AbstractAttributeChangers() {
        this(EnumSet.noneOf(EmptyAttribute.class));
    }

    /**
     * Initialises a new {@link AbstractAttributeChangers}.
     */
    public AbstractAttributeChangers(EnumSet<? extends Attribute> attributes) {
        super();
        this.attributes = attributes;
    }

    /**
     * Gets the attributes
     *
     * @return The attributes
     */
    public EnumSet<? extends Attribute> getAttributes() {
        return attributes;
    }
}
