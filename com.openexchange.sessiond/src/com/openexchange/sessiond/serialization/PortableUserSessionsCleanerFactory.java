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

package com.openexchange.sessiond.serialization;

import com.hazelcast.nio.serialization.Portable;
import com.openexchange.hazelcast.serialization.AbstractCustomPortableFactory;
import com.openexchange.hazelcast.serialization.CustomPortable;


/**
 * {@link PortableUserSessionsCleanerFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class PortableUserSessionsCleanerFactory extends AbstractCustomPortableFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_USER_SESSIONS_CLEANER_CLASS_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Portable create() {
        return new PortableUserSessionsCleaner();
    }
}
