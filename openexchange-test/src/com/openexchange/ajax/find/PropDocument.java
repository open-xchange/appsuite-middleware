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

package com.openexchange.ajax.find;

import java.util.Map;
import com.openexchange.find.Document;
import com.openexchange.find.DocumentVisitor;

/**
 * {@link PropDocument}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PropDocument implements Document {

    private static final long serialVersionUID = 2506394204168147507L;

    private final Map<String, Object> props;

    /**
     * Initializes a new {@link PropDocument}.
     */
    public PropDocument(final Map<String, Object> props) {
        super();
        this.props = props;
    }

    @Override
    public void accept(final DocumentVisitor visitor) {
        // Ignore
    }

    /**
     * Gets the props
     *
     * @return The props
     */
    public Map<String, Object> getProps() {
        return props;
    }

}
