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

package com.openexchange.realtime.payload;

import java.io.Serializable;
import com.openexchange.realtime.util.ElementPath;
import static com.openexchange.realtime.util.CopyObject.copyObject;

/**
 * {@link PayloadElement} - Represents a stanza's payload element that is any (POJO) object linked with its format identifier. Namespace and
 * elementName are used for unique identification (@see ElementPath) of payload elements and determine which
 * {@link PayloadElementConverter} to use.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
//TODO: Make cloneable or offer immutable copy
public class PayloadElement implements VisitablePayload, Serializable, Comparable<PayloadElement> {

    private static final long serialVersionUID = 2523897376910073662L;

    // Current format of the Payload e.g. json, xml or some Class.getSimpleName()
    private String format = "";

    // The namespace of this payload e.g.: http://jabber.org/protocol/disco.info
    private String namespace = "";

    // The elementname identifying the Payload element in a namespace
    private String elementName = "";

    // The actual payload
    private Object data = null;

    /**
     * Initializes a new {@link PayloadElement}.
     *
     * @param data The payload's data object
     * @param format The data object's format
     * @param namespace The namespace of this Payload element
     * @param elementName the unique element name within the namespace
     */
    public PayloadElement(Object data, String format, String namespace, String elementName) {
        this.data = data;
        this.elementName = elementName;
        this.format = format;
        this.namespace = namespace;
    }

    /**
     * Initializes a new {@link PayloadElement} based on another PayloadElement.
     * @param otherPayloadElement The other PayloadElement, must not be null
     * @throws IllegalArgumentException If the other PayloadElement is null
     */
    public PayloadElement(PayloadElement otherPayloadElement) {
        //TODO: Change object to serializable in Stanza + PayloadTree
        this.data = copyObject((Serializable)otherPayloadElement.data);
        this.elementName = otherPayloadElement.elementName;
        this.format = otherPayloadElement.format;
        this.namespace = otherPayloadElement.namespace;

    }

    /**
     * Gets the data object.
     *
     * @return The data object
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the payload data object and its format.
     *
     * @param data The data object
     * @param format The data object's format
     */
    public void setData(Object data, String format) {
        this.data = data;
        this.format = format;
    }

    /**
     * Gets the unique element name within the namespace
     *
     * @return the unique element name within the namespace
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Get the unique elementPath identifying the PayloadElement.
     * @return The unique elementPath identifying the PayloadElement
     */
    public ElementPath getElementPath() {
        return new ElementPath(namespace, elementName);
    }

    /**
     * Gets the data object's format identifier.
     *
     * @return The format identifier.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets the namespace of the payload element
     *
     * @return null if the element is from the the default namespace, otherwise the namespace of the payload element
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PayloadElement)) {
            return false;
        }
        PayloadElement other = (PayloadElement) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (elementName == null) {
            if (other.elementName != null) {
                return false;
            }
        } else if (!elementName.equals(other.elementName)) {
            return false;
        }
        if (format == null) {
            if (other.format != null) {
                return false;
            }
        } else if (!format.equals(other.format)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PayloadElement" + "@" + hashCode() + " " + "[format=" + format + ", namespace=" + namespace + ", elementName=" + elementName + ", data=" + data + "]";
    }

    @Override
    public void accept(PayloadVisitor visitor) {
        visitor.visit(this, data);
    }

    @Override
    public int compareTo(PayloadElement other) {
        return getElementPath().compareTo(other.getElementPath());
    }

}
