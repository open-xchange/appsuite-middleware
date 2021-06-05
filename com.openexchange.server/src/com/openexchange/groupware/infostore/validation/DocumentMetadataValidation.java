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

package com.openexchange.groupware.infostore.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.Metadata;

/**
 * {@link DocumentMetadataValidation}
 */
public class DocumentMetadataValidation {

    private final Map<Metadata, String> errors;
    private final List<Metadata> errorFields;
    private OXException exception;
    private OXException fatalException;

    /**
     * Initializes a new {@link DocumentMetadataValidation}.
     */
    public DocumentMetadataValidation() {
        super();
        errors = new HashMap<Metadata, String>();
        errorFields = new ArrayList<Metadata>();
    }

    public boolean isValid() {
        return errors.isEmpty() && null == fatalException;
    }

    public boolean hasErrors(final Metadata field) {
        return errors.containsKey(field);
    }

    public String getError(final Metadata field) {
        return errors.get(field);
    }

    public void setError(final Metadata field, final String error) {
        errors.put(field, error);
        errorFields.add(field);
    }

    public List<Metadata> getInvalidFields() {
        return errorFields;
    }

    /**
     * Gets the fatal exception
     *
     * @return The fatal exception
     */
    public OXException getFatalException() {
        return fatalException;
    }

    /**
     * Sets the fatal exception
     *
     * @param fatalException The fatal exception to set
     */
    public void setFatalException(OXException fatalException) {
        this.fatalException = fatalException;
    }

    /**
     * Gets the exception
     *
     * @return The exception
     */
    public OXException getException() {
        return exception;
    }

    /**
     * Sets the exception
     *
     * @param exception The exception to set
     */
    public void setException(OXException exception) {
        this.exception = exception;
    }

}
