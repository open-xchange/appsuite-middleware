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
        }
        return super.encodeForFilter(value, idResolver);
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
        }
        return null;
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