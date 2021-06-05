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

package com.openexchange.client.onboarding;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SimpleResultObject}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SimpleResultObject implements ResultObject {

    private final Object object;
    private final String format;
    private ArrayList<OXException> warnings;

    /**
     * Initializes a new {@link SimpleResultObject}.
     *
     * @param object The object
     * @param format The format; if <code>null</code> <code>"json"</code> is assumed
     */
    public SimpleResultObject(Object object, String format) {
        super();
        this.object = object;
        this.format = null == format ? "json" : format;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public boolean hasWarnings() {
        if (warnings == null || warnings.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public void addWarning(OXException warning) {
        if (warnings == null) {
            warnings = new ArrayList<>(1);
        }

        warnings.add(warning);
    }

}
