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

package com.openexchange.contact.storage.ldap.id;

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
            this.ids = (BiMap<String, Integer>)session.getParameter(PARAMETER_LDAP_IDS);
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
        return contactID;
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
                throw LdapExceptionCodes.NO_MAPPED_LDAP_ID.create(contactID, folderID, contextID);
            }
        }
        return ldapID;
    }

    private void triggerIDMappings() throws OXException {
        this.storage.all(this.session, Integer.toString(storage.getFolderID()),
            new ContactField[] { ContactField.OBJECT_ID, ContactField.INTERNAL_USERID });
    }

}