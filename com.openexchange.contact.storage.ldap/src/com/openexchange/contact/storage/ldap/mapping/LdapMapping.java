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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.DefaultMapping;

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
        return null != alternativeLdapAttributeName && suppressOptions ?
            this.alternativeLdapAttributeName.split(";")[0] : this.alternativeLdapAttributeName;
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
            return encode((T)value, idResolver);
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