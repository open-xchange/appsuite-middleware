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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.ldap.ldap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;
import com.openexchange.contacts.ldap.exceptions.LdapExceptionCode;
import com.openexchange.exception.OXException;

/**
 * An implementation of the {@link LdapGetter} interface which used JNDI to
 * contact the LDAP server
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class LdapGetterJNDIImpl implements LdapGetter {

    private final Attributes attributes;

    private final LdapContext context;

    private final String objectfullname;

    public LdapGetterJNDIImpl(Attributes attributes, LdapContext context, String objectfullname) {
        super();
        this.attributes = attributes;
        this.context = context;
        this.objectfullname = objectfullname;
    }

    @Override
    public String getAttribute(final String attributename) throws OXException {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    // If we have multi-value attributes we only pick up the first one
                    return (String) attribute.get(0);
                } else {
                    return (String) attribute.get();
                }
            } else {
                return null;
            }
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        }
    }

    @Override
    public Date getDateAttribute(final String attributename) throws OXException {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    throw LdapExceptionCode.MULTIVALUE_NOT_ALLOWED_DATE.create(attributename);
                } else {
                    // final DirContext attributeDefinition = attribute.getAttributeDefinition();
                    // final Attributes attributes2 = attributeDefinition.getAttributes("");
                    // final Attribute syntaxattribute = attributes2.get("syntax");
                    // final String value = (String) syntaxattribute.get();
                    // if ("1.3.6.1.4.1.1466.115.121.1.24".equals(value)) {
                    // // We have a "Generalized Time syntax"
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    final Date date = simpleDateFormat.parse((String) attribute.get());
                    return date;
                    // } else {
                    // final DateFormat dateInstance = DateFormat.getDateInstance();
                    // return dateInstance.parse((String) attribute.get());
                    // }
                }
            } else {
                return null;
            }
        } catch (final ParseException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
        }
    }

    @Override
    public int getIntAttribute(final String attributename) throws OXException {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    throw LdapExceptionCode.MULTIVALUE_NOT_ALLOWED_INT.create(attributename);
                } else {
                    return Integer.parseInt((String) attribute.get());
                }
            } else {
                return -1;
            }
        } catch (final NumberFormatException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create("Attributename: " + attributename + " - " + e.getMessage());
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create("Attributename: " + attributename + " - " + e.getMessage());
        }
    }

    @Override
    public LdapGetter getLdapGetterForDN(final String dn, String[] attributes) throws OXException {
        try {
            return new LdapGetterJNDIImpl(context.getAttributes(dn, attributes), context, dn);
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create("AttributeDN: " + dn + " - " + e.getMessage());
        }
    }

    @Override
    public List<String> getMultiValueAttribute(final String attributename) throws OXException {
        final List<String> retval = new ArrayList<String>();
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    final NamingEnumeration<?> all = attribute.getAll();
                    while (all.hasMoreElements()) {
                        retval.add((String) all.nextElement());
                    }
                } else {
                    try {
                        retval.add((String) attribute.get());
                    } catch (final NoSuchElementException e) {
                        // We ignore this if the list has no member
                    }
                }
                return retval;
            } else {
                return retval;
            }
        } catch (final NamingException e) {
            throw LdapExceptionCode.ERROR_GETTING_ATTRIBUTE.create(e.getMessage());
        }
    }

    @Override
    public String getObjectFullName() throws OXException {
        return objectfullname;
    }

}
