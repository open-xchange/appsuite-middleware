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

package com.openexchange.groupware.settings.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;

/**
 * This class represents a single setting.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractSetting<T extends AbstractSetting<? extends T>> implements Setting {

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
    private T parent;

    /**
     * Reference to shared value reader.
     */
    private final IValueHandler shared;

    /**
     * Name of this setting.
     */
    private final String name;

    /**
     * Stores the sub elements.
     */
    private Map<String, T> elements;

    /**
     * Constructor for initializing especially shared values.
     * @param name Name.
     * @param shared shared value reader.
     * @param id for shared values normally <code>-1</code>.
     */
    protected AbstractSetting(final String name, final int id, final IValueHandler shared) {
        super();
        this.name = name;
        this.shared = shared;
        this.id = id;
    }
    
    /**
     * @return Returns the name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the sub setting that has the given name.
     * @param subName Name of the sub setting.
     * @return the sub setting or <code>null</code> if it doesn't exist.
     */
    @Override
    public T getElement(final String subName) {
        T element = null;
        if (null != elements) {
            element = elements.get(subName);
        }
        return element;
    }

    /**
     * @return Returns the id.
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @return Returns the leaf.
     */
    @Override
    public boolean isLeaf() {
        return null == elements || 0 == elements.size();
    }

    /**
     * Adds a sub element to this element.
     * @param child sub element to add.
     */
    void addElement(final T child) {
        if (null == elements) {
            elements = new HashMap<String, T>();
        }
        elements.put(child.getName(), child);
    }

    /**
     * Removes the sub element from this element.
     * @param child sub element to remove.
     */
    protected void removeElementInternal(final T child) {
        if (null != elements) {
            elements.remove(child.getName());
        }
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append(name);
        if (null != elements) {
            out.append('(');
            final Iterator<T> iter = elements.values().iterator();
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
    @Override
    public T[] getElements() {
        final List<T> tmp = new ArrayList<T>();
        if (null != elements) {
            tmp.addAll(elements.values());
        }
        @SuppressWarnings("unchecked")
        T[] retval = (T[]) Array.newInstance(this.getClass(), tmp.size());
        return tmp.toArray(retval);
    }

    /**
     * @return <code>true</code> if this setting is used in server and gui and
     * <code>false</code> if the setting is only used in gui.
     */
    @Override
    public boolean isShared() {
        return -1 == shared.getId();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AbstractSetting)) {
            return false;
        }
        final AbstractSetting<?> other = (AbstractSetting<?>) obj;
        if (id != other.getId() || !name.equals(other.getName())) {
            return false;
        }
        if (parent != null && !parent.equals(other.getParent())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int retval = id ^ name.hashCode();
        if (isShared()) {
            retval ^= Boolean.valueOf(isShared()).hashCode();
        }
        return retval;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(final T parent) {
        this.parent = parent;
    }

    /**
     * @return the path for this setting.
     */
    @Override
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
    @Override
    public T getParent() {
        return parent;
    }

    /**
     * @return the shared
     */
    @Override
    public IValueHandler getShared() {
        return shared;
    }

}
