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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a single setting.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Setting {

    /**
     * Separator for pathes.
     */
    public static final char SEPARATOR = '/';

    /**
     * Unique identifier of this setting.
     */
    private final int id;

    /**
     * Reference to the parent Setting.
     */
    private Setting parent;

    /**
     * Reference to shared value reader.
     */
    private final IValueHandler shared;

    /**
     * Name of this setting.
     */
    private final String name;

    /**
     * Single value of this setting.
     */
    private Object singleValue;

    /**
     * Multi value of this setting.
     */
    private ArrayList<Object> multiValue;

    /**
     * Stores the sub elements.
     */
    private Map<String, Setting> elements;

    /**
     * Constructor for initializing especially shared values.
     * @param name Name.
     * @param shared shared value reader.
     * @param id for shared values normally <code>-1</code>.
     */
    public Setting(final String name, final int id, final IValueHandler shared) {
        super();
        this.name = name;
        this.shared = shared;
        this.id = id;
    }

    /**
     * Copy constructor.
     * @param toCopy object to copy.
     */
    public Setting(final Setting toCopy) {
        this(toCopy.name, toCopy.id, toCopy.shared);
        parent = toCopy.parent;
        if (null != toCopy.elements) {
            elements = new HashMap<String, Setting>(toCopy.elements.size());
            for (Setting element : toCopy.elements.values()) {
                addElement(new Setting(element));
            }
        }
    }

    /**
     * @return the multi value.
     */
    public Object[] getMultiValue() {
        Object[] retval;
        synchronized (this) {
            if (null == multiValue || 0 == multiValue.size()) {
                retval = null;
            } else {
                retval = multiValue.toArray(new Object[multiValue.size()]);
            }
        }
        return retval;
    }

    /**
     * @return the single value.
     */
    public Object getSingleValue() {
        return singleValue;
    }

    /**
     * @param value The value to set.
     */
    public void setSingleValue(final Object value) {
        this.singleValue = value;
    }

    /**
     * @param value Value to add.
     */
    public void addMultiValue(final Object value) {
        if (null != value) {
            synchronized (this) {
                if (null == multiValue) {
                    multiValue = new ArrayList<Object>();
                }
                multiValue.add(value);
            }
        }
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the sub setting that has the given name.
     * @param subName Name of the sub setting.
     * @return the sub setting or <code>null</code> if it doesn't exist.
     */
    public Setting getElement(final String subName) {
        Setting element = null;
        if (null != elements) {
            element = elements.get(subName);
        }
        return element;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @return Returns the leaf.
     */
    public boolean isLeaf() {
        return null == elements || 0 == elements.size();
    }

    /**
     * Adds a sub element to this element.
     * @param child sub element to add.
     */
    public void addElement(final Setting child) {
        if (null == elements) {
            elements = new HashMap<String, Setting>();
        }
        elements.put(child.getName(), child);
        child.setParent(this);
    }

    /**
     * Removes the sub element from this element.
     * @param child sub element to remove.
     */
    public void removeElement(final Setting child) {
        if (null != elements) {
            elements.remove(child.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append(name);
        out.append('=');
        if (null == elements) {
            if (null != multiValue && multiValue.size() > 0) {
                out.append(multiValue);
            } else {
                out.append(singleValue);
            }
        } else {
            out.append('(');
            final Iterator<Setting> iter = elements.values().iterator();
            while (iter.hasNext()) {
                out.append(iter.next().toString());
                if (iter.hasNext()) {
                    out.append(',');
                }
            }
            out.append(')');
        }
        return out.toString();
    }

    /**
     * @return the sub elements of this element.
     */
    public Setting[] getElements() {
        Setting[] retval;
        if (null == elements) {
            retval = new Setting[0];
        } else {
            retval = elements.values().toArray(new Setting[elements.size()]);
        }
        return retval;
    }

    /**
     * @return <code>true</code> if this setting is used in server and gui and
     * <code>false</code> if the setting is only used in gui.
     */
    public boolean isShared() {
        return -1 == shared.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Setting)) {
            return false;
        }
        final Setting other = (Setting) obj;
        if (id != other.id || !name.equals(other.name)) {
            return false;
        }
        if (parent != null && !parent.equals(other.parent)) {
            return false;
        }
        if (singleValue != null && !singleValue.equals(other.singleValue)) {
            return false;
        }
        if (multiValue != null && !multiValue.equals(other.multiValue)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int retval = id ^ name.hashCode();
        if (singleValue != null) {
            retval ^= singleValue.hashCode();
        }
        if (multiValue != null) {
            retval ^= multiValue.hashCode();
        }
        if (isShared()) {
            retval ^= Boolean.valueOf(isShared()).hashCode();
        }
        return retval;
    }

    /**
     * @param parent the parent to set
     */
    private void setParent(final Setting parent) {
        this.parent = parent;
    }

    /**
     * @return the path for this setting.
     */
    public String getPath() {
        final String retval;
        if (null == parent) {
            retval = name;
        } else {
            retval = parent.getPath() + SEPARATOR + name;
        }
        return retval;
    }

    /**
     * @return the parent
     */
    Setting getParent() {
        return parent;
    }

    /**
     * @return the shared
     */
    IValueHandler getShared() {
        return shared;
    }
}
