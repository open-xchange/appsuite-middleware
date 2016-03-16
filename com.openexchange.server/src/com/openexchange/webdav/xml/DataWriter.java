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

package com.openexchange.webdav.xml;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Verifier;
import org.jdom2.output.XMLOutputter;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * {@link DataParser} - The base class for writing XML content
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DataWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataWriter.class);

    public static final int ACTION_MODIFIED = 1;

    public static final int ACTION_DELETE = 2;

    public static final int ACTION_LIST = 3;

    private static final Namespace dav = Namespace.getNamespace("D", "DAV:");

    public static final Namespace namespace = Namespace.getNamespace(XmlServlet.PREFIX, XmlServlet.NAMESPACE);

    protected Session sessionObj;

    protected Context ctx;

    protected User userObj;

    protected void writeResponseElement(final Element e_prop, final int object_id, final int status, final String description, final XMLOutputter xo, final OutputStream os) throws Exception {
        final Element e_response = new Element("response", dav);
        e_response.addNamespaceDeclaration(Namespace.getNamespace(XmlServlet.PREFIX, XmlServlet.NAMESPACE));
        e_response.addContent(new Element("href", dav).addContent(Integer.toString(object_id)));

        final Element e_propstat = new Element("propstat", dav);
        e_response.addContent(e_propstat);

        e_propstat.addContent(e_prop);
        e_propstat.addContent(new Element("status", dav).addContent(Integer.toString(status)));
        e_propstat.addContent(new Element("responsedescription", dav).addContent(correctCharacterData(description)));

        xo.output(e_response, os);
        os.flush();
    }

    protected void writeDataElements(final DataObject dataobject, final Element e_prop) {
        if (dataobject.containsCreatedBy()) {
            addElement(DataFields.CREATED_BY, dataobject.getCreatedBy(), e_prop);
        }

        if (dataobject.containsCreationDate()) {
            addElement(DataFields.CREATION_TIME, dataobject.getCreationDate(), e_prop);
        }

        if (dataobject.containsModifiedBy()) {
            addElement(DataFields.MODIFIED_BY, dataobject.getModifiedBy(), e_prop);
        }

        if (dataobject.containsLastModified()) {
            addElement(DataFields.LAST_MODIFIED, dataobject.getLastModified(), e_prop);
        }

        if (dataobject.containsObjectID()) {
            addElement(DataFields.OBJECT_ID, dataobject.getObjectID(), e_prop);
        }
    }

    public static Element addElement(final String name, final String value, final Element parent) {
        if (value != null) {
            final Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
            e.addContent(correctCharacterData(value));
            parent.addContent(e);
            return e;
        }
        return null;
    }

    public static Element addElement(final String name, final Date value, final Element parent) {
        if (value != null) {
            final Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
            e.addContent(Long.toString(value.getTime()));
            parent.addContent(e);
            return e;
        }
        return null;
    }

    public static Element addElement(final String name, final int value, final Element parent) {
        final Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
        e.addContent(Integer.toString(value));
        parent.addContent(e);
        return e;
    }

    public static Element addElement(String name, BigDecimal value, Element parent) {
        Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
        e.addContent(value.toPlainString());
        parent.addContent(e);
        return e;
    }

    public static Element addElement(final String name, final long value, final Element parent) throws Exception {
        final Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
        e.addContent(Long.toString(value));
        parent.addContent(e);
        return e;
    }

    public static Element addElement(final String name, final boolean value, final Element parent) {
        final Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
        if (value) {
            e.addContent("true");
        } else {
            e.addContent("false");
        }

        parent.addContent(e);
        return e;
    }

    /**
     * This will correct the supplied string to ensure it only contains characters allowed by the XML 1.0 specification. The C0 controls
     * (e.g. null, vertical tab, form-feed, etc.) are specifically excluded except for carriage return, line-feed, and the horizontal tab.
     * Surrogates are also excluded. Thus the returned string will pass the {@link Verifier#checkCharacterData(String) check} internally
     * performed inside JDOM library.
     * <p>
     * <ul>
     * <li>A <code>null</code> value is transformed to an empty string</li>
     * <li>A truncated or illegal surrogate pair is transformed to an empty string</li>
     * <li>Any non-XML character is omitted</li>
     * </ul>
     * <p>
     * This method is useful for correcting element content and attribute values. Note that characters like " and &lt; are allowed in
     * attribute values and element content. They will simply be escaped as &quot; or &lt; when the value is serialized.
     *
     * @param text The value to correct.
     * @return The corrected text (if passed to {@link Verifier#checkCharacterData(String)} <code>null</code> would be returned)
     */
    public static final String correctCharacterData(final String text) {
        if (text == null) {
            /*
             * null is not a legal XML value, transform to an empty string
             */
            LOG.debug("null is not a legal XML value");
            return "";
        }
        /*
         * Check non-null text
         */
        final int length = text.length();
        final StringBuilder retvalBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int ch = text.charAt(i);
            /*
             * Check for high part of a surrogate pair
             */
            if (ch >= 0xD800 && ch <= 0xDBFF) {
                /*
                 * Check if next char is the low-surrogate
                 */
                if (++i < length) {
                    final char low = text.charAt(i);
                    if (low >= 0xDC00 && low <= 0xDFFF) {
                        /*
                         * Good pair, calculate character's true value to check for a valid XML character
                         */
                        ch = 0x10000 + (ch - 0xD800) * 0x400 + (low - 0xDC00);
                        if (Verifier.isXMLCharacter(ch)) {
                            retvalBuilder.append((char) ch);
                        } else {
                            LOG.debug("0x{} is not a legal XML character", Integer.toHexString(ch));
                        }
                    } else {
                        LOG.debug("illegal surrogate pair");
                    }
                } else {
                    LOG.debug("truncated surrogate pair");
                }
            } else {
                /*
                 * A common character, check if it is according XML specification
                 */
                if (Verifier.isXMLCharacter(ch)) {
                    retvalBuilder.append((char) ch);
                } else {
                    LOG.debug("0x{} is not a legal XML character", Integer.toHexString(ch));
                }
            }
        }
        /*
         * Return cleansed string
         */
        return retvalBuilder.toString();
    }

    /**
     * Gets the error message for given arguments.
     *
     * @param message The message template in <tt>printf</tt> style
     * @param arg The message argument
     * @return The error message
     */
    protected static String getErrorMessage(final String message, final String arg) {
        return String.format(message, arg);
    }

    /**
     * Gets the error message for given arguments.
     *
     * @param message The message template in <tt>printf</tt> style
     * @param errorCode The error code
     * @return The error message
     */
    protected static String getErrorMessage(final String message, final int errorCode) {
        return getErrorMessage(message, Integer.toString(errorCode));
    }

}
