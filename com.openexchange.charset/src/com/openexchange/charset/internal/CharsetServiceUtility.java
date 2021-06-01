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

package com.openexchange.charset.internal;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link CharsetServiceUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CharsetServiceUtility {

    /**
     * Initializes a new {@link CharsetServiceUtility}.
     */
    private CharsetServiceUtility() {
        super();
    }

    private static final AtomicReference<Charset> ISO_8859_1_REF = new AtomicReference<Charset>(null);

    static void setIso8859CS(Charset iso8859) {
        ISO_8859_1_REF.set(iso8859);
    }

    public static Charset getIso8859CS() {
        return ISO_8859_1_REF.get();
    }

}
