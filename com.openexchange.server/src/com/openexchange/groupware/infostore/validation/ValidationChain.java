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
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;

/**
 * @author francisco.laguna@open-xchange.com
 *
 * A validation chain can be used to chain many validators together and have each one check
 * the DocumentMetadata in turn. If validation doesn't pass, an exception is thrown containing an error message.
 * The error message details for each validator the individual error messages. If more than one field is assigned
 * the same error, a list of invalid fields is prepended to the error message.
 */
public class ValidationChain {

    private final List<InfostoreValidator> validators = new ArrayList<InfostoreValidator>();

    /**
     * Initializes a new {@link ValidationChain}.
     *
     * @param validators The validators to add to the chain
     */
    public ValidationChain(InfostoreValidator...validators) {
        super();
        if (null != validators && 0 < validators.length) {
            for (InfostoreValidator validator : validators) {
                this.validators.add(validator);
            }
        }
    }

    public void add(final InfostoreValidator validator) {
        validators.add(validator);
    }

    public void validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns) throws OXException {
        final StringBuilder message = new StringBuilder();
        boolean failed = false;
        OXException exception = null;
        for (final InfostoreValidator validator : validators) {
            final DocumentMetadataValidation validation = validator.validate(session, metadata, originalDocument, updatedColumns);
            if (!validation.isValid()) {
                {
                    OXException fatalException = validation.getFatalException();
                    if (null != fatalException) {
                        throw fatalException;
                    }
                }

                failed = true;
                if (null == exception) {
                    exception = validation.getException();
                }

                final Map<String, List<Metadata>> errors = new HashMap<String, List<Metadata>>();
                for (final Metadata field : validation.getInvalidFields()) {
                    final String error = validation.getError(field);
                    List<Metadata> errorList = errors.get(error);
                    if (null == errorList) {
                        errorList = new ArrayList<Metadata>();
                        errors.put(error, errorList);
                    }
                    errorList.add(field);
                }

                message.append(validator.getName()).append(": ").append('(');
                for (final Map.Entry<String, List<Metadata>> entry : errors.entrySet()) {
                    for (final Metadata field : entry.getValue()) {
                        message.append(field.getName()).append(", ");
                    }
                    message.setLength(message.length() - 2);
                    message.append(") ").append(entry.getKey());
                    message.append('\n');
                }
                /*-
                 *
                 * Replaced:
                 *
                for(final String error : errors.keySet()) {
                    for(final Metadata field : errors.get(error)) {
                        message.append(field.getName()).append(", ");
                    }
                    message.setLength(message.length()-2);
                    message.append(") ").append(error);
                    message.append("\n");
                }
                 */
            }
        }
        if (failed) {
            if (null != exception) {
                exception.setLogMessage(InfostoreExceptionCodes.VALIDATION_FAILED.getMessage(), message.toString());
                throw exception;
            }
            throw InfostoreExceptionCodes.VALIDATION_FAILED.create(message.toString());
        }
    }

}
