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

package com.openexchange.contact.internal;

import java.util.EnumSet;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link QueryFields} - Prepares contact fields to pass-through to the storage.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryFields {

    /** Fields for necessary permission checks */
    private static final EnumSet<ContactField> PERMISSION_FIELDS = EnumSet.of(
        ContactField.CREATED_BY, ContactField.PRIVATE_FLAG, ContactField.FOLDER_ID, ContactField.OBJECT_ID, ContactField.CONTEXTID
    );

	private boolean needsAttachmentInfo;
	private final ContactField[] queriedFields;

	public QueryFields() {
		this(null);
	}

	public QueryFields(final ContactField[] fields) {
		this(fields, null);
	}

	public QueryFields(ContactField[] fields, ContactField[] allowedFields) {
		super();
		if (null == fields) {
		    if (null == allowedFields) {
	            /*
	             * query all fields
	             */
	            this.queriedFields = ContactField.values();
	            this.needsAttachmentInfo = true;
		    } else {
		        /*
		         * query permission- and other allowed fields
		         */
		        this.needsAttachmentInfo = false;
	            EnumSet<ContactField> preparedFields = EnumSet.copyOf(PERMISSION_FIELDS);
	            for (ContactField allowedField : allowedFields) {
                    preparedFields.add(allowedField);
                    if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(allowedField)) {
                        // add attachment information
                        this.needsAttachmentInfo = true;
                        preparedFields.add(ContactField.NUMBER_OF_ATTACHMENTS);
                    }
                }
                this.queriedFields = preparedFields.toArray(new ContactField[preparedFields.size()]);
		    }
		} else {
            this.needsAttachmentInfo = false;
            EnumSet<ContactField> preparedFields = EnumSet.copyOf(PERMISSION_FIELDS);
		    if (null == allowedFields) {
		        /*
		         * query permission- and supplied fields
		         */
                for (ContactField field : fields) {
                    preparedFields.add(field);
                    if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(field)) {
                        // add attachment information
                        this.needsAttachmentInfo = true;
                        preparedFields.add(ContactField.NUMBER_OF_ATTACHMENTS);
                    } else if (ContactField.IMAGE1_URL.equals(field)) {
                        preparedFields.add(ContactField.IMAGE_LAST_MODIFIED);
                    }
                }
		    } else {
                /*
                 * query permission- and supplied fields if allowed
                 */
                for (ContactField field : fields) {
                    if (contains(allowedFields, field)) {
                        preparedFields.add(field);
                        if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(field)) {
                            // add attachment information
                            this.needsAttachmentInfo = true;
                            preparedFields.add(ContactField.NUMBER_OF_ATTACHMENTS);
                        } else if (ContactField.IMAGE1_URL.equals(field)) {
                            preparedFields.add(ContactField.IMAGE_LAST_MODIFIED);
                        }
                    }
                }
		    }
            this.queriedFields = preparedFields.toArray(new ContactField[preparedFields.size()]);
		}
	}

	private static boolean contains(final ContactField[] fields, final ContactField field) {
		for (final ContactField presentField : fields) {
			if (presentField.equals(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the needsAttachmentInfo
	 */
	public boolean needsAttachmentInfo() {
		return needsAttachmentInfo;
	}

	/**
	 * @return the fields
	 */
	public ContactField[] getFields() {
		return queriedFields;
	}

}
