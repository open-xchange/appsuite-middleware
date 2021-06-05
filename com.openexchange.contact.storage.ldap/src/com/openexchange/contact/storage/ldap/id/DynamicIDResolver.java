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

package com.openexchange.contact.storage.ldap.id;

import static com.openexchange.java.Autoboxing.I;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.internal.LdapContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.session.Session;

/**
 * {@link DynamicIDResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DynamicIDResolver extends DefaultLdapIDResolver {

    private static final String PARAMETER_LDAP_IDS = "com.openexchange.contacts.ldap.storage.userids";
    private final BiMap<String, Integer> ids;
    private final LdapContactStorage storage;
    private final Session session;

    public DynamicIDResolver(Session session, LdapContactStorage storage) throws OXException {
        super(session.getContextId(), storage.getFolderID());
        this.storage = storage;
        this.session = session;
        if (session.containsParameter(PARAMETER_LDAP_IDS)) {
            @SuppressWarnings("unchecked") BiMap<String, Integer> biMap = (BiMap<String, Integer>)session.getParameter(PARAMETER_LDAP_IDS);
            this.ids = biMap;
        } else {
            this.ids = HashBiMap.create();
            session.setParameter(PARAMETER_LDAP_IDS, ids);
        }
    }

    @Override
    public int getContactID(String ldapID) throws OXException {
        Integer contactID = this.ids.get(ldapID);
        if (null == contactID) {
            synchronized (ids) {
                contactID = this.ids.get(ldapID);
                if (null == contactID) {
                    contactID = Integer.valueOf(1 + ids.size());
                    this.ids.put(ldapID, contactID);
                }
            }
        }
        return contactID.intValue();
    }

    @Override
    public String getLdapID(int contactID) throws OXException {
        String ldapID = this.ids.inverse().get(Integer.valueOf(contactID));
        if (null == ldapID) {
            synchronized (ids) {
                ldapID = this.ids.inverse().get(Integer.valueOf(contactID));
                if (null == ldapID) {
                    this.triggerIDMappings();
                    ldapID = this.ids.inverse().get(Integer.valueOf(contactID));
                }
            }
            if (null == ldapID) {
                throw LdapExceptionCodes.NO_MAPPED_LDAP_ID.create(I(contactID), I(folderID), I(contextID));
            }
        }
        return ldapID;
    }

    private void triggerIDMappings() throws OXException {
        this.storage.all(this.session, Integer.toString(storage.getFolderID()),
            new ContactField[] { ContactField.OBJECT_ID, ContactField.INTERNAL_USERID });
    }

}