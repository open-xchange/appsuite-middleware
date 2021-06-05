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
package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.util.Hashtable;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;

/**
 * This class defines all those methods which make and object capable of being dynamically extended by other
 * attributes.<p>
 *
 * To implement this in our class simply extend from this class.
 *
 * @author d7
 */
@SuppressWarnings("deprecation")
public abstract class ExtendableDataObject extends EnforceableDataObject implements Serializable, Cloneable {

    private static final long serialVersionUID = 5125311385480887183L;

    private Hashtable<String, OXCommonExtensionInterface> extensions = null;

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
    public void addExtension(final OXCommonExtensionInterface extension) throws DuplicateExtensionException {
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
    public Hashtable<String, OXCommonExtensionInterface> getAllExtensionsAsHash() {
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
    public OXCommonExtensionInterface getFirstExtensionByName(final String extname) {
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
     * Removes the given extension from this object. Note that only the name of the Class is interesting here
     * so you don't have to provide the exact Object but only an Object from the fitting type.
     *
     * @param extension An {@link OXCommonExtension} object specifying the extension to be removed
     * @return
     */
    public boolean removeExtension(final OXCommonExtension extension) {
        return null == extensions.remove(extension.getClass().getName()) ? false : true;
    }

    public final void setExtensionsok(boolean extensionsok) {
        this.extensionsok = extensionsok;
    }

    protected void initExtendable() {
        this.extensions = new Hashtable<String, OXCommonExtensionInterface>(3);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final ExtendableDataObject object = (ExtendableDataObject) super.clone();
        if ( this.extensions != null ) {
            object.extensions = new Hashtable<String, OXCommonExtensionInterface>(this.extensions);
        }
        return object;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(super.toString());
        for (final OXCommonExtensionInterface usrext : extensions.values()) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
        result = prime * result + (extensionsok ? 1231 : 1237);
        return result;
    }

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
        return true;
    }
}
