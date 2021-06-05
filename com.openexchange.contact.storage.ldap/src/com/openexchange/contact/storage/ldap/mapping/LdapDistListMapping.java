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

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link LdapDistListMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class LdapDistListMapping extends LdapMapping<DistributionListEntryObject[]> {

    @Override
    public DistributionListEntryObject[] get(LdapResult result, LdapIDResolver idResolver) throws OXException {
        Attribute attribute = super.getAttribute(result);
        if (null != attribute) {
            List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
            NamingEnumeration<?> values = null;
            try {
                values = attribute.getAll();
                while (values.hasMoreElements()) {
                    String value = (String) values.nextElement();
                    if (null != value) {
                        /*
                         * set the display name to the member attribute value temporary; will be
                         * resolved at a later stage by the storage.
                         */
                        DistributionListEntryObject member = new DistributionListEntryObject();
                        member.setDisplayname(value);
                        members.add(member);
                    }
                }
            } catch (NamingException e) {
                throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
            } finally {
                Tools.close(values);
            }
            return members.toArray(new DistributionListEntryObject[members.size()]);
        }
        return null;
    }

    @Override
    public String encode(DistributionListEntryObject[] value, LdapIDResolver idResolver) throws OXException {
        return (value.length == 1) ? Tools.escapeLDAPSearchFilter(value[0].getEmailaddress()) : null;
        //throw LdapExceptionCodes.SEARCHING_IN_DISTRIBUTION_LISTS_NOT_SUPPORTED.create();
    }
}
