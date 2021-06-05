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

import java.util.Set;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * @author francisco.laguna@open-xchange.com
 *
 * An InfostoreValidator is used to validate a DocumentMetadata for according to some criterium
 *
 */
public interface InfostoreValidator {

	/**
	 * This method is expected to check a DocumentMetadata and fill a new instance of DocumentMetadataValidation with the relevant errors.
	 * <p />
	 * A few implementation hints:
	 * <ol>
     * <li>Don't include the field name in the error messages, it will be included by the ValidationChain.</li>
     * <li>Try to reuse error messages. All fields with the same error message are collected and displayed together.</li>
     * </ol>
	 *
     * @param session The session
     * @param metadata The DocumentMetadata to check
     * @param originalDocument The original document in case of an update, or <code>null</code> for new documents
     * @param updatedColumns The updated columns, or <code>null</code> for new documents
	 * @return A DocumentMetadataValidation filled with the relevant errors
	 */
	DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns);

	/**
	 * Returns the name used for displaying error messages
	 */
	String getName();

}
