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

import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;

/**
 * {@link LdapStringMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class LdapStringMapping extends LdapMapping<String> {

    @Override
    public String get(LdapResult result, LdapIDResolver idResolver) throws OXException {
        return (String)super.getValue(result);
    }

    @Override
    public String encode(String value, LdapIDResolver idResolver) throws OXException {
        return null != value ? Tools.escapeLDAPSearchFilter(value) : null;
    }

}