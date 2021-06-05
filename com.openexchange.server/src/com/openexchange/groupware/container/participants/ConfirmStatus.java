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

package com.openexchange.groupware.container.participants;

import static com.openexchange.java.Autoboxing.I;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ConfirmStatus}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ConfirmStatus {

    NONE(0),
    ACCEPT(1),
    DECLINE(2),
    TENTATIVE(3);

    private int id;

    private ConfirmStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ConfirmStatus byId(int id) {
        return ID_MAP.get(I(id));
    }

    private static Map<Integer, ConfirmStatus> ID_MAP;

    static {
        ImmutableMap.Builder<Integer, ConfirmStatus> m = ImmutableMap.builder();
        for (ConfirmStatus status : values()) {
            m.put(I(status.getId()), status);
        }
        ID_MAP = m.build();
    }
}
