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

package com.openexchange.user.json.anonymizer;

import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.session.Session;

/**
 * {@link ContactAnonymizerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ContactAnonymizerService implements AnonymizerService<Contact> {

    private static final ContactField[] WHITELIST_FIELDS = new ContactField[] {
        ContactField.INTERNAL_USERID, ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.UID, ContactField.CREATED_BY,
        ContactField.CREATION_DATE, ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED, ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT,
        ContactField.LAST_MODIFIED_UTC };

    /**
     * Initializes a new {@link ContactAnonymizerService}.
     */
    public ContactAnonymizerService() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.CONTACT;
    }

    @Override
    public Contact anonymize(Contact entity, Session session) throws OXException {
        if (null == entity) {
            return null;
        }

        // Anonymize the contact
        Contact anonymizedContact = new Contact();
        ContactMapper instance = ContactMapper.getInstance();
        for (ContactField contactField : WHITELIST_FIELDS) {
            try {
                JsonMapping<? extends Object, Contact> mapping = instance.get(contactField);
                mapping.copy(entity, anonymizedContact);
            } catch (Exception e) {
                // Failed to copy value
            }
        }

        int userId = entity.getInternalUserId();
        String i18n = Anonymizers.getUserI18nFor(session);

        anonymizedContact.setDisplayName(new StringBuilder(i18n).append(' ').append(userId).toString());
        anonymizedContact.setGivenName(Integer.toString(userId));
        anonymizedContact.setSurName(i18n);

        return anonymizedContact;
    }

}
