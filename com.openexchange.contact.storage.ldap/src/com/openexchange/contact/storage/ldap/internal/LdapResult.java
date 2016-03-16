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
