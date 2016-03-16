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

import java.util.UUID;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link LdapIDMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class LdapIDMapping extends LdapMapping<Integer> {

    @Override
    public Integer get(LdapResult result, LdapIDResolver idResolver) throws OXException {
        Object value = super.getValue(result);
        if (null != value) {
            String ldapID = null;
            if (byte[].class.isInstance(value) && UUIDs.UUID_BYTE_LENGTH == ((byte[])value).length) {
                ldapID = UUIDs.toUUID((byte[])value).toString();
            } else if (String.class.isInstance(value)) {
                ldapID = (String)value;
            } else {
                ldapID = value.toString();
            }
            return Integer.valueOf(idResolver.getContactID(ldapID));
        }
        return null;
    }

    @Override
    public String encodeForFilter(Object value, LdapIDResolver idResolver) throws OXException {
        // override to parse numerical IDs if necessary
        if (String.class.isInstance(value)) {
            return encode(Integer.valueOf(Tools.parse((String)value)), idResolver);
        } else {
            return super.encodeForFilter(value, idResolver);
        }
    }

    @Override
    public String encode(Integer value, LdapIDResolver idResolver) throws OXException {
        if (null != value) {
            String ldapID = idResolver.getLdapID(value.intValue());
            try {
                return encodeHex(UUIDs.toByteArray(UUID.fromString(ldapID)));
            } catch (IllegalArgumentException e) {
                // no valid UUID format
            }
            return ldapID;
        } else {
            return null;
        }
    }

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static String encodeHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(48);
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append('\\').append(DIGITS[(0xF0 & bytes[i]) >>> 4]).append(DIGITS[0x0F & bytes[i]]);
        }
        return stringBuilder.toString();
    }

}