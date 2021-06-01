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

package com.openexchange.contact.storage.ldap.internal;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link LdapResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapResult {

    private final Attributes attributes;
    private final String name;

    public LdapResult(SearchResult result) {
        this(result.getAttributes(), result.getNameInNamespace());
    }

    public LdapResult(Attributes attributes, String name) {
        super();
        this.attributes = attributes;
        this.name = name;
    }

    public Object getAttribute(String attributeName) throws OXException {
        if (null != this.attributes) {
            Attribute attribute = attributes.get(attributeName);
            if (null != attribute) {
                try {
                    return 1 < attribute.size() ? attribute.get(0) : attribute.get();
                } catch (NamingException e) {
                    throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
                }
            }
        }
        return null;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static List<LdapResult> getResults(NamingEnumeration<SearchResult> results) throws OXException {
        List<LdapResult> ldapResults = new ArrayList<LdapResult>();
        if (null != results) {
            try {
                while (results.hasMoreElements()) {
                    ldapResults.add(new LdapResult(results.next()));
                }
            } catch (NamingException e) {
                throw LdapExceptionCodes.LDAP_ERROR.create(e, e.getMessage());
            } finally {
                Tools.close(results);
            }
        }
        return ldapResults;
    }

}
