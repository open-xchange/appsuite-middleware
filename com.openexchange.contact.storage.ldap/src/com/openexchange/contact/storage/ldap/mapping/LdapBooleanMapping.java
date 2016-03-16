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
        if (Boolean.TRUE.equals(value)) {
            return null != this.match ? match : String.valueOf(true);
        } else {
            throw new UnsupportedOperationException("unable to encode other values than TRUE");
        }
    }

}