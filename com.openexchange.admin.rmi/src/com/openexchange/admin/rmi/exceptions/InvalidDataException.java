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

package com.openexchange.admin.rmi.exceptions;

import java.util.ArrayList;

/**
 * Is thrown when user sends invalid data to the server.
 *
 * @author cutmasta
 *
 */
public class InvalidDataException extends AbstractAdminRmiException {

    /**
     * IF we need more granular exceptions for invalid filestore url or invalid
     * username for example, the new Exception must extend this exception.
     */

    /**
     * Contains the name of the object which is affected by this exception
     */
    private String objectname = null;

    /**
     * Contains the fieldnames in the object (if available) which are not correct
     */
    private ArrayList<String> fieldnames = null;
    /**
     * For serialization
     */
    private static final long serialVersionUID = 5803502090025698411L;

    /**
     *
     */
    public InvalidDataException() {
        super("Invalid data sent!");
    }

    /**
     * @param message
     */
    public InvalidDataException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public InvalidDataException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Gets the field names, which are not correct in the object
     *
     * @return The incorrect field names or <code>null</code>
     */
    public final ArrayList<String> getFieldnames() {
        return fieldnames;
    }

    /**
     * Sets the field names of the object which aren't correct (maybe null if no field names can be specified)
     *
     * @param fieldnames The field names to set
     */
    public final void setFieldnames(ArrayList<String> fieldnames) {
        this.fieldnames = fieldnames;
    }

    /**
     * Get the name of the object which is affected by this exception
     *
     * @return The object name or <code>null</code>
     */
    public final String getObjectname() {
        return objectname;
    }

    /**
     * Set the name of the object which is affected by this exception
     *
     * @param objectname The name to set
     */
    public final void setObjectname(String objectname) {
        this.objectname = objectname;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        if (null != fieldnames) {
            sb.append("The following field are invalid:\n");
            sb.append(fieldnames);
        }
        return sb.toString();
    }

}
