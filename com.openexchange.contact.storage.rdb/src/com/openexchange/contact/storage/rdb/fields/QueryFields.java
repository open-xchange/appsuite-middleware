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

package com.openexchange.contact.storage.rdb.fields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link QueryFields} - Determines which {@link ContactField}s are needed in database query statements from each of the database
 * tables where contact information is stored. By adding the appropriate fields on demand, it also ensures that the
 * <code>ContactField.NUMBER_OF_IMAGES</code> and <code>ContactField.NUMBER_OF_DISTRIBUTIONLIST</code> are queried automatically when
 * needed.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryFields {

    private ContactField[] contactDataFields;
    private ContactField[] imageDataFields;
    private DistListMemberField[] distListDataFields;
    private boolean hasImageData;
    private boolean hasContactData;
    private boolean hasDistListData;
    private boolean hasAttachmentData;

    /**
     * Initializes a new {@link QueryFields}.
     *
     * @param fields
     */
    public QueryFields(final ContactField[] fields, ContactField...mandatoryFields) {
        this.update(fields, mandatoryFields);
    }

    /**
     * Updates the current instance with the supplied fields.
     *
     * @param fields
     */
    public void update(final ContactField[] fields, ContactField...mandatoryFields) {
        if (null == fields) {
            throw new IllegalArgumentException("fields");
        }
        /*
         * check fields
         */
        final Set<ContactField> imageDataFieldsSet = new HashSet<ContactField>();
        final Set<ContactField> contactDataFieldsSet = new HashSet<ContactField>();
        boolean loadImageUrl = false;
        for (final ContactField field : fields) {
        	if (Fields.CONTACT_DATABASE.contains(field)) {
        		contactDataFieldsSet.add(field);
        	} else if (Fields.IMAGE_DATABASE_ADDITIONAL.contains(field)) {
        		imageDataFieldsSet.add(field);
        	} else if (ContactField.DISTRIBUTIONLIST.equals(field)) {
                this.hasDistListData = true;
        	} else if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(field)) {
        		this.hasAttachmentData = true;
        	} else if (ContactField.IMAGE1_URL.equals(field)) {
                loadImageUrl = true;
            }
		}
        if (null != mandatoryFields) {
	        for (final ContactField field : mandatoryFields) {
	        	if (Fields.CONTACT_DATABASE.contains(field)) {
	        		contactDataFieldsSet.add(field);
	        	} else if (Fields.IMAGE_DATABASE_ADDITIONAL.contains(field)) {
	        		imageDataFieldsSet.add(field);
	        	} else if (ContactField.DISTRIBUTIONLIST.equals(field)) {
	                this.hasDistListData = true;
	        	} else if (ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.equals(field)) {
	        		this.hasAttachmentData = true;
	        	} else if (ContactField.IMAGE1_URL.equals(field)) {
	                loadImageUrl = true;
	            }
			}
        }
        /*
         * check image data fields
         */
        if (0 < imageDataFieldsSet.size() || loadImageUrl) {
        	imageDataFieldsSet.add(ContactField.OBJECT_ID);
        	this.hasImageData = true;
        	this.imageDataFields = imageDataFieldsSet.toArray(new ContactField[imageDataFieldsSet.size()]);
        	contactDataFieldsSet.add(ContactField.NUMBER_OF_IMAGES);
        }
        /*
         * check distlist data fields
         */
        if (this.hasDistListData) {
        	this.distListDataFields = Fields.DISTLIST_DATABASE_ARRAY;
        	contactDataFieldsSet.add(ContactField.NUMBER_OF_DISTRIBUTIONLIST);
        }
        /*
         * check attachment data fields
         */
        if (this.hasAttachmentData) {
        	contactDataFieldsSet.add(ContactField.NUMBER_OF_ATTACHMENTS);
        }
        /*
         * check contact data fields
         */
        if (0 < contactDataFieldsSet.size()) {
        	this.contactDataFields = contactDataFieldsSet.toArray(new ContactField[contactDataFieldsSet.size()]);
        	this.hasContactData = true;
        }
    }

    /**
     * Gets the contact data fields.
     *
     * @return the fields
     */
    public ContactField[] getContactDataFields() {
        return this.contactDataFields;
    }

    /**
     * Gets the image data fields.
     *
     * @return the fields
     */
    public ContactField[] getImageDataFields() {
        return this.imageDataFields;
    }

    /**
     * Gets the image data fields.
     *
     * @param forUpdate whether the fields should be used for an update or not
     * @return the fields
     */
    public ContactField[] getImageDataFields(boolean forUpdate) {
    	if (forUpdate) {
    		List<ContactField> updateFields = new ArrayList<ContactField>();
    		for (ContactField field : imageDataFields) {
				if (false == ContactField.OBJECT_ID.equals(field) && false == ContactField.CONTEXTID.equals(field)) {
					updateFields.add(field);
				}
			}
    		return updateFields.toArray(new ContactField[updateFields.size()]);
    	}
        return this.imageDataFields;
    }

    public ContactField[] getContactDataFields(boolean forUpdate) {
    	if (forUpdate) {
    		List<ContactField> updateFields = new ArrayList<ContactField>();
    		for (ContactField field : imageDataFields) {
				if (false == ContactField.OBJECT_ID.equals(field) && false == ContactField.CONTEXTID.equals(field)) {
					updateFields.add(field);
				}
			}
    		return updateFields.toArray(new ContactField[updateFields.size()]);
    	}
        return this.imageDataFields;
    }

    /**
     * Gets the distribution list data fields.
     *
     * @return the fields
     */
    public DistListMemberField[] getDistListDataFields() {
        return this.distListDataFields;
    }

    /**
     * Gets a value indicating whether image data fields are present or not.
     *
     * @return <code>true</code>, if there are image data fields,
     * <code>false</code>, otherwise
     */
    public boolean hasImageData() {
        return this.hasImageData;
    }

    /**
     * Gets a value indicating whether contact data fields are present or not.
     *
     * @return <code>true</code>, if there are contact data fields,
     * <code>false</code>, otherwise
     */
    public boolean hasContactData() {
        return this.hasContactData;
    }

    /**
     * Gets a value indicating whether distribution list data fields are
     * present or not.
     *
     * @return <code>true</code>, if there are distribution list data fields,
     * <code>false</code>, otherwise
     */
    public boolean hasDistListData() {
        return this.hasDistListData;
    }

    /**
     * Gets a value indicating whether attachment data fields are present or
     * not.
     *
     * @return <code>true</code>, if there are attachment data fields,
     * <code>false</code>, otherwise
     */
    public boolean hasAttachmentData() {
        return this.hasAttachmentData;
    }

}
