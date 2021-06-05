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

package com.openexchange.tools.file;

/**
 * Replacing of tags in TemplateParser takes place through the methods of this interface.
 *
 * @author <a href="mailto:m.klein@open-xchange.com">Marcus Klein</a>
 */
public interface TagFiller {

    /**
     * Implementation of this method returns the value that should be inserted into the place of the given tag.
     *
     * @param tag The tag that is found by the TemplateParser.
     * @return The replacement for the tag.
     */
    public String replace(String tag);

    /**
     * Implementation of this method returns the value that should be inserted into the place of the given tag. If you use the parse method
     * with parameter data this data object is passed to this replace method.
     *
     * @param tag The tag that is found by the TemplateParser.
     * @param data A user defined data object.
     * @return The replacement for the tag.
     */
    public String replace(String tag, Object data);
}
