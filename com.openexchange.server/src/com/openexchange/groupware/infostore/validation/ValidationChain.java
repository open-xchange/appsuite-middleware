/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.infostore.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;

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

    public void add(final InfostoreValidator validator) {
        validators.add(validator);
    }

    public void validate(final DocumentMetadata metadata) throws OXException {
        final StringBuilder message = new StringBuilder();
        boolean failed = false;
        OXException exception = null;
        for (final InfostoreValidator validator : validators) {
            final DocumentMetadataValidation validation = validator.validate(metadata);
            if (!validation.isValid()) {
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
                exception.setLogLevel(LogLevel.ERROR);
                throw exception;
            }
            throw InfostoreExceptionCodes.VALIDATION_FAILED.create(message.toString());
        }
    }

}
