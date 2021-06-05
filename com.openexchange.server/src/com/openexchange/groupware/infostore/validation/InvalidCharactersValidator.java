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
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;

public class InvalidCharactersValidator implements InfostoreValidator{

	@Override
    public DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns) {
		final DocumentMetadataValidation validation = new DocumentMetadataValidation();
		final GetSwitch get = new GetSwitch(metadata);
		for(final Metadata field : Metadata.VALUES_ARRAY){
			final Object value = field.doSwitch(get);
			if (value != null && value instanceof String) {
				final String error = check((String)value);
				if (null != error) {
					validation.setError(field, error);
				}
			}
		}
		validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_CHARACTERS.create());
		return validation;
	}

	public String check(final String string) {
		return Check.containsInvalidChars(string);
	}

	@Override
    public String getName() {
		return InvalidCharactersValidator.class.getSimpleName();
	}

}
