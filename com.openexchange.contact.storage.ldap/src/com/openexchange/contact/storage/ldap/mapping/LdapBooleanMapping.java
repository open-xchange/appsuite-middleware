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

package com.openexchange.contact.storage.ldap.mapping;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;

/**
 * {@link LdapBooleanMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class LdapBooleanMapping extends LdapMapping<Boolean> {

    private String match = null;

    @Override
    public void setLdapAttributeName(String ldapAttributeName) {
        if (null == ldapAttributeName) {
            this.match = null;
            super.setLdapAttributeName(ldapAttributeName);
        } else {
            String[] splitted = ldapAttributeName.split("=");
            super.setLdapAttributeName(splitted[0]);
            if (0 < splitted.length) {
                this.match = splitted[1];
            }
        }
    }

    @Override
    public Boolean get(LdapResult result, LdapIDResolver idResolver) throws OXException {
        Attribute attribute = super.getAttribute(result);
        if (null != attribute) {
            NamingEnumeration<?> values = null;
            try {
                values = attribute.getAll();
                String toMatch = null == this.match ? String.valueOf(true) : this.match;
                while (values.hasMoreElements()) {
                    String value = (String)values.nextElement();
                    if (null != value && toMatch.equalsIgnoreCase(value)) {
                        return Boolean.TRUE;
                    }
                }
            } catch (NamingException e) {
                throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
            } finally {
                Tools.close(values);
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public String encode(Boolean value, LdapIDResolver idResolver) throws OXException {
        if (!value.booleanValue()) {
            throw new UnsupportedOperationException("unable to encode other values than TRUE");
        }
        return null != this.match ? match : String.valueOf(true);
    }

}