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

package com.openexchange.sessiond.impl.container;

import com.openexchange.sessiond.impl.SessionImpl;

/**
 * {@link LongTermSessionControl} - The instance managed in long-term container.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class LongTermSessionControl extends AbstractSessionControl {

    /**
     * Initializes a new {@link LongTermSessionControl}.
     *
     * @param sessionControl The session control
     */
    public LongTermSessionControl(SessionControl sessionControl) {
        super(sessionControl.getSession(), sessionControl.getCreationTime());
    }

    /**
     * Initializes a new {@link LongTermSessionControl}.
     *
     * @param session The session
     * @param creationTime The creation time to apply
     */
    public LongTermSessionControl(SessionImpl session, long creationTime) {
        super(session, creationTime);
    }

    @Override
    public ContainerType geContainerType() {
        return ContainerType.LONG_TERM;
    }

}
