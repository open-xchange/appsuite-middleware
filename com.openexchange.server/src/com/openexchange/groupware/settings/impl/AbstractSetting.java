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

package com.openexchange.groupware.settings.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Ranked;
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
    private Map<String, PriorityQueue<T>> elements;

    /**
     * The ranking
     */
    private final int ranking;

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
        ranking = (shared instanceof Ranked) ? ((Ranked) shared).getRanking() : 0;
    }

    @Override
    public int getRanking() {
        return ranking;
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

        PriorityQueue<T> queue = optQueue(subName);
        if (null != queue) {
            element = queue.peek();
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

    PriorityQueue<T> getQueue(String name) {
        if (null == elements) {
            elements = new HashMap<String, PriorityQueue<T>>();
            PriorityQueue<T> newQueue = new PriorityQueue<T>(4, new Comparator<T>() {

                @Override
                public int compare(T o1, T o2) {
                    int r1 = o1.getRanking();
                    int r2 = o2.getRanking();
                    // Higher ranking first...
                    return r1 < r2 ? 1 : (r1 == r2 ? 0 : -1);
                }
            });
            elements.put(name, newQueue);
            return newQueue;
        }

        PriorityQueue<T> q = elements.get(name);
        if (null == q) {
            q = new PriorityQueue<T>(4, new Comparator<T>() {

                @Override
                public int compare(T o1, T o2) {
                    int r1 = o1.getRanking();
                    int r2 = o2.getRanking();
                    // Higher ranking first...
                    return r1 < r2 ? 1 : (r1 == r2 ? 0 : -1);
                }
            });
            elements.put(name, q);
        }
        return q;
    }

    PriorityQueue<T> optQueue(String name) {
        if (null == elements) {
            return null;
        }

        return elements.get(name);
    }

    boolean checkElement(T child) {
        PriorityQueue<T> q = optQueue(child.getName());
        if (null == q) {
            return true;
        }

        for (T t : q) {
            if (t.getRanking() == child.getRanking()) {
                // There is already a setting associated with that name having the same ranking
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a sub element to this element.
     * @param child sub element to add.
     */
    void addElement(T child) {
        getQueue(child.getName()).offer(child);
    }

    /**
     * Removes the sub element from this element.
     * @param child sub element to remove.
     */
    protected void removeElementInternal(T child) {
        PriorityQueue<T> q = optQueue(child.getName());
        if (null != q) {
            q.remove(child);
        }
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append(name);
        if (null != elements) {
            out.append('(');
            for (PriorityQueue<T> q : elements.values()) {
                Iterator<T> iter = q.iterator();
                while (iter.hasNext()) {
                    out.append(String.valueOf(iter.next()));
                    if (iter.hasNext()) {
                        out.append(',');
                    }
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
            for (PriorityQueue<T> q : elements.values()) {
                T element = q.peek();
                if (element != null) {
                    tmp.add(element);
                }
            }
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
            retval ^= Boolean.TRUE.hashCode();
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
        return null == parent ? name : new StringBuilder(parent.getPath()).append(SEPARATOR).append(name).toString();
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
