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
package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.util.Hashtable;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;

/**
 * This class defines all those methods which make and object capable of being dynamically extended by other
 * attributes.<p>
 *
 * To implement this in our class simply extend from this class.
 *
 * @author d7
 *
 */
public abstract class ExtendableDataObject extends EnforceableDataObject implements Serializable, Cloneable {

    private static final long serialVersionUID = 5125311385480887183L;

    private Hashtable<String, OXCommonExtension> extensions = null;

    private final boolean extensionsset = false;

    /**
     * This field is used to show if all extension have run fine and inserted their
     * data correctly
     */
    private boolean extensionsok = true;

    /**
     * Adds an extension to an object
     *
     * @param extension An {@link OXCommonExtension} object
     * @throws DuplicateExtensionException
     */
    public void addExtension(final OXCommonExtension extension) throws DuplicateExtensionException {
        final String extensionName = extension.getClass().getName();
        if (this.extensions.containsKey(extensionName)) {
            throw new DuplicateExtensionException(extensionName);
        }
        this.extensions.put(extensionName, extension);
    }

    /**
     * Returns a {@link Hashtable} of all extensions with the name of the extensions as key
     *
     * @return A {@link Hashtable}
     */
    public Hashtable<String, OXCommonExtension> getAllExtensionsAsHash() {
        return this.extensions;
    }

    /**
     * A convenience method for getting the first extension in a list of equal extension names. The
     * use of this method is not recommended because you won't get notifications how many extensions
     * of the same name exist.
     *
     * @param extname
     * @return
     */
    public OXCommonExtension getFirstExtensionByName(final String extname) {
        return this.extensions.get(extname);
    }

    /**
     * Shows if an error occurred in any of the extensions of this object. If you get {@code true} here everything
     * is fine. Otherwise an error occurred and you have to go through all extensions to find out in which one this
     * was happening
     *
     * @return A {@link boolean} value
     */
    public boolean isExtensionsok() {
        return extensionsok;
    }

    /**
     * This method will be used in the future
     *
     * @return
     */
    public boolean isExtensionsset() {
        return extensionsset;
    }

    /**
     * Removes the given extension from this object. Note that only the name of the Class is interesting here
     * so you don't have to provide the exact Object but only an Object from the fitting type.
     *
     * @param extension An {@link OXCommonExtension} object specifying the extension to be removed
     * @return
     */
    public boolean removeExtension(final OXCommonExtension extension) {
        if (null == extensions.remove(extension.getClass().getName())) {
            return false;
        } else {
            return true;
        }
    }

    public final void setExtensionsok(boolean extensionsok) {
        this.extensionsok = extensionsok;
    }

    protected void initExtendable() {
        this.extensions = new Hashtable<String, OXCommonExtension>(3);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final ExtendableDataObject object = (ExtendableDataObject) super.clone();
        if( this.extensions != null ) {
            object.extensions = new Hashtable<String, OXCommonExtension>(this.extensions);
        }
        return object;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(super.toString());
        for (final OXCommonExtension usrext : extensions.values()) {
            ret.append("  ");
            ret.append("Extension ");
            ret.append(usrext.getClass().getName());
            ret.append(" contains: \n");
            ret.append("  ");
            ret.append(usrext.toString());
            ret.append("\n");
        }

        return ret.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
        result = prime * result + (extensionsok ? 1231 : 1237);
        result = prime * result + (extensionsset ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ExtendableDataObject)) {
            return false;
        }
        final ExtendableDataObject other = (ExtendableDataObject) obj;
        if (extensions == null) {
            if (other.extensions != null) {
                return false;
            }
        } else if (!extensions.equals(other.extensions)) {
            return false;
        }
        if (extensionsok != other.extensionsok) {
            return false;
        }
        if (extensionsset != other.extensionsset) {
            return false;
        }
        return true;
    }
}
