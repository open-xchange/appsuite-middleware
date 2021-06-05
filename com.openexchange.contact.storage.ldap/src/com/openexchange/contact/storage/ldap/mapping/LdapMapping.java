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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.java.Strings;

/**
 * {@link LdapMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public abstract class LdapMapping<T> extends DefaultMapping<T, Contact> implements Cloneable {

    protected String ldapAttributeName;
    protected String alternativeLdapAttributeName;
    protected boolean binary;

    /**
     * Initializes a new {@link LdapMapping}.
     */
    public LdapMapping() {
        this(null, null);
    }

    /**
     * Initializes a new {@link LdapMapping}.
     *
     * @param ldapAttributeName the name of the LDAP attribute
     */
    public LdapMapping(String ldapAttributeName) {
        this(ldapAttributeName, null);
    }

    /**
     * Initializes a new {@link LdapMapping}.
     *
     * @param ldapAttributeName the name of the LDAP attribute
     * @param alternativeLdapAttributeName the alternative name of the LDAP attribute
     */
    public LdapMapping(String ldapAttributeName, String alternativeLdapAttributeName) {
        super();
        this.ldapAttributeName = ldapAttributeName;
        this.alternativeLdapAttributeName = alternativeLdapAttributeName;
    }

    protected Object getValue(LdapResult result) throws OXException {
        Attribute attribute = getAttribute(result);
        if (null != attribute) {
            try {
                return 1 < attribute.size() ? attribute.get(0) : attribute.get();
            } catch (NamingException e) {
                throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
            }
        }
        return null;
    }

    protected Attribute getAttribute(LdapResult result) {
        if (null != result && null != result.getAttributes()) {
            Attribute attribute = result.getAttributes().get(getLdapAttributeName());
            if (null != attribute) {
                return attribute;
            } else if (null != getAlternativeLdapAttributeName()) {
                return result.getAttributes().get(getAlternativeLdapAttributeName());
            }
        }
        return null;
    }

    public void setFrom(LdapResult result, LdapIDResolver idResolver, Contact contact) throws OXException {
        T value = this.get(result, idResolver);
        if (null != value) {
            this.set(contact, value);
        }
    }

    public String getLdapAttributeName() {
        return this.ldapAttributeName;
    }

    public String getAlternativeLdapAttributeName() {
        return this.alternativeLdapAttributeName;
    }

    public String getAlternativeLdapAttributeName(boolean suppressOptions) {
        return suppressOptions && null != alternativeLdapAttributeName ?
            Strings.splitBySemiColon(this.alternativeLdapAttributeName)[0] : this.alternativeLdapAttributeName;
    }

    public boolean isBinary() {
        return this.binary;
    }

    public String getLdapAttributeName(boolean suppressOptions) {
        return null != ldapAttributeName && suppressOptions ? this.ldapAttributeName.split(";")[0] : this.ldapAttributeName;
    }

    public void setLdapAttributeName(String ldapAttributeName) {
        this.ldapAttributeName = ldapAttributeName;
        this.binary = null != ldapAttributeName && ldapAttributeName.contains(";binary");
    }

    public void setAlternativeLdapAttributeName(String alternativeLdapAttributeName) {
        this.alternativeLdapAttributeName = alternativeLdapAttributeName;
    }

    public abstract T get(LdapResult result, LdapIDResolver idResolver) throws OXException;

    protected abstract String encode(T value, LdapIDResolver idResolver) throws OXException;

    /**
     * Prepares the supplied value to be used in search filters for LDAP-
     * attribute based comparisons.
     *
     * @param value the value
     * @param idResolver
     * @return
     */
    public String encodeForFilter(Object value, LdapIDResolver idResolver) throws OXException {
        try {
            @SuppressWarnings("unchecked") T t = (T) value;
            return encode(t, idResolver);
        } catch (ClassCastException e) {
            throw LdapExceptionCodes.ERROR.create(e, "Error encoding value for '" + this + "'");
        }
    }

    @Override
    public Object clone() {
        LdapMapping<?> clone = null;
        try {
            clone = (LdapMapping<?>)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // shouldn't happen
        }
        clone.setLdapAttributeName(this.ldapAttributeName);
        return clone;
    }

    @Override
    public String toString() {
        return null == alternativeLdapAttributeName ? ldapAttributeName : ldapAttributeName + "/" + alternativeLdapAttributeName;
    }

}