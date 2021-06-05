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

package com.openexchange.drive.impl.internal;

import java.text.Normalizer;
import java.text.Normalizer.Form;


/**
 * {@link PathNormalizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PathNormalizer {

    /**
     * Normalizes a file- or directory path. The supplied string will be normalized according to the {@link Form#NFC} normalization from,
     * i.e. canonical decomposition, followed by canonical composition.
     *
     * @param path The file- or directory path to normalize
     * @return The normalized form
     */
    public static String normalize(String path) {
        return null != path ? Normalizer.normalize(path, Form.NFC) : null;
    }

    /**
     * Gets a value indicating whether the supplied file- or directory path is normalized according to the {@link Form#NFC} normalization
     * from, i.e. canonical decomposition, followed by canonical composition.
     *
     * @param path The file- or directory path to check
     * @return <code>true</code> if the path is normalized, <code>false</code>, otherwise
     */
    public static boolean isNormalized(String path) {
        return null != path ? Normalizer.isNormalized(path, Form.NFC) : true;
    }

    /**
     * Gets a value indicating whether the supplied file- or directory path are considered equal when being in their {@link Form#NFC}
     * normalization from, i.e. canonical decomposition, followed by canonical composition.
     *
     * @param path1 The first path to check
     * @param path2 The second path to check
     * @return <code>true</code> if both paths are equal, <code>false</code>, otherwise
     */
    public static boolean equals(String path1, String path2) {
        if (null == path1) {
            return null == path2;
        } else if (null == path2) {
            return false;
        } else {
            return normalize(path1).equals(normalize(path2));
        }
    }

}
