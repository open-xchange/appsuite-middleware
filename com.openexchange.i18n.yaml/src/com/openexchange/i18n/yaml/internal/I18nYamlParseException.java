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

package com.openexchange.i18n.yaml.internal;


/**
 * {@link I18nYamlParseException} - Indicates an error during parsing string literals from a YAML file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class I18nYamlParseException extends Exception {

    private static final long serialVersionUID = -738530317209728482L;

    /**
     * Wraps specified exception with a {@code I18nYamlParseException}.
     *
     * @param message The optional message
     * @param e The exception to wrap
     * @return The {@code I18nYamlParseException} instance
     */
    public static I18nYamlParseException wrapException(String message, Exception e) {
        if (null == e) {
            return null;
        }
        if (e instanceof I18nYamlParseException) {
            return (I18nYamlParseException) e;
        }
        I18nYamlParseException parseException = new I18nYamlParseException(null == message ? e.getMessage() : message);
        parseException.setStackTrace(e.getStackTrace());
        return parseException;
    }

    // --------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link I18nYamlParseException}.
     *
     * @param message
     */
    public I18nYamlParseException(String message) {
        super(message);
    }

}
