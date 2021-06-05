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

package com.openexchange.push.impl.balancing.registrypolicy.portable;

import com.hazelcast.nio.serialization.Portable;
import com.openexchange.hazelcast.serialization.AbstractCustomPortableFactory;


/**
 * {@link PortableOwnerFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortableOwnerFactory extends AbstractCustomPortableFactory {

    /**
     * Initializes a new {@link PortableOwnerFactory}.
     */
    public PortableOwnerFactory() {
        super();
    }

    @Override
    public Portable create() {
        return new PortableOwner();
    }

    @Override
    public int getClassId() {
        return PortableOwner.CLASS_ID;
    }

}
