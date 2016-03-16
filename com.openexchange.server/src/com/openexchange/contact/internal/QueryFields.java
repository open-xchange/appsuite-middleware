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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
