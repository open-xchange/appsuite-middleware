
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
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;

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

    public String getAttribute(final String attributename) throws LdapException {
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
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
    }

    public Date getDateAttribute(final String attributename) throws LdapException {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    throw new LdapException(Code.MULTIVALUE_NOT_ALLOWED_DATE, attributename);
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
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e, e.getMessage());
        } catch (final NamingException e) {
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e, e.getMessage());
        }
    }

    public int getIntAttribute(final String attributename) throws LdapException {
        try {
            final Attribute attribute = attributes.get(attributename);
            if (null != attribute) {
                if (1 < attribute.size()) {
                    throw new LdapException(Code.MULTIVALUE_NOT_ALLOWED_INT, attributename);
                } else {
                    return Integer.parseInt((String) attribute.get());
                }
            } else {
                return -1;
            }
        } catch (final NumberFormatException e) {
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "Attributename: " + attributename + " - " + e.getMessage());
        } catch (final NamingException e) {
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "Attributename: " + attributename + " - " + e.getMessage());
        }
    }

    public LdapGetter getLdapGetterForDN(final String dn) throws LdapException {
        try {
            return new LdapGetterJNDIImpl(context.getAttributes(dn), context, dn);
        } catch (final NamingException e) {
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, "AttributeDN: " + dn + " - " + e.getMessage());
        }
    }

    public List<String> getMultiValueAttribute(final String attributename) throws LdapException {
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
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
    }

    public String getObjectFullName() throws LdapException {
        return objectfullname;
    }

}
