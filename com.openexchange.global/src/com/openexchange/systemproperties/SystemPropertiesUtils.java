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

package com.openexchange.systemproperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;

/**
 * {@link SystemPropertiesUtils} - Utility class for system properties to avoid thread contention.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class SystemPropertiesUtils {

    /**
     * Initializes a new {@link SystemPropertiesUtils}.
     */
    private SystemPropertiesUtils() {
        super();
    }

    private static volatile Map<Object, Object> copiedSystemProperties;

    /**
     * (Re-)Initialized the copy of system properties.
     */
    public static void initSystemProperties() {
        Properties props = new ChangeSignalingProperties((Properties) System.getProperties().clone());
        ImmutableMap.Builder<Object, Object> copiedProperties = ImmutableMap.builderWithExpectedSize(props.size());
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            copiedProperties.put(entry.getKey(), entry.getValue());
        }
        copiedSystemProperties = copiedProperties.build();
        System.setProperties(props);
    }

    /**
     * Drops the copy of system properties.
     */
    public static void dropSystemProperties() {
        copiedSystemProperties = null;
    }

    /**
     * Gets the immutable copy of the system properties.
     *
     * @return The copy of the system properties
     */
    public static Map<Object, Object> getSystemProperties() {
        Map<Object, Object> copiedProperties = copiedSystemProperties;
        if (copiedProperties == null) {
            synchronized (SystemPropertiesUtils.class) {
                copiedProperties = copiedSystemProperties;
                if (copiedProperties == null) {
                    Properties props = System.getProperties();
                    ImmutableMap.Builder<Object, Object> copiedPropertiesBuilder = ImmutableMap.builderWithExpectedSize(props.size());
                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        copiedPropertiesBuilder.put(entry.getKey(), entry.getValue());
                    }
                    copiedProperties = copiedPropertiesBuilder.build();
                    copiedSystemProperties = copiedProperties;
                }
            }
        }
        return copiedProperties;
    }

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param key The property key
     * @return The property value or <code>null</code>
     */
    public static String getProperty(String key) {
        if (key == null) {
            return null;
        }

        Object oval = getSystemProperties().get(key);
        return (oval instanceof String) ? oval.toString() : null;
    }

    /**
     * Creates a clone of the system properties.
     *
     * @return The clone of the system properties
     */
    public static Properties cloneSystemProperties() {
        // Avoid mutex on System.getProperties()
        Map<Object, Object> copiedProperties = getSystemProperties();
        Properties retval = new Properties();
        retval.putAll(copiedProperties);
        return retval;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ChangeSignalingIterator<E> implements Iterator<E> {

        private final Iterator<E> delegate;

        /**
         * Initializes a new {@link ChangeSignalingIterator}.
         *
         * @param delegate The delegate
         */
        ChangeSignalingIterator(Iterator<E> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            delegate.remove();
            dropSystemProperties();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            delegate.forEachRemaining(action);
        }
    }

    private static class ChangeSignalingSet<E> implements Set<E> {

        private final Set<E> delegate;

        /**
         * Initializes a new {@link ChangeSignalingSet}.
         *
         * @param delegate The delegate
         */
        ChangeSignalingSet(Set<E> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new ChangeSignalingIterator<E>(delegate.iterator());
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(E e) {
            boolean added = delegate.add(e);
            if (added) {
                dropSystemProperties();
            }
            return added;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = delegate.remove(o);
            if (removed) {
                dropSystemProperties();
            }
            return removed;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean changed = delegate.addAll(c);
            if (changed) {
                dropSystemProperties();
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = delegate.retainAll(c);
            if (changed) {
                dropSystemProperties();
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
            dropSystemProperties();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            boolean anyRemoved = delegate.removeIf(filter);
            if (anyRemoved) {
                dropSystemProperties();
            }
            return anyRemoved;
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }
    }

    private static class ChangeSignalingCollection<E> implements Collection<E> {

        private final Collection<E> delegate;

        /**
         * Initializes a new {@link ChangeSignalingCollection}.
         *
         * @param delegate The delegate
         */
        ChangeSignalingCollection(Collection<E> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new ChangeSignalingIterator<E>(delegate.iterator());
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(E e) {
            boolean added = delegate.add(e);
            if (added) {
                dropSystemProperties();
            }
            return added;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = delegate.remove(o);
            if (removed) {
                dropSystemProperties();
            }
            return removed;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean changed = delegate.addAll(c);
            if (changed) {
                dropSystemProperties();
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = delegate.removeAll(c);
            if (changed) {
                dropSystemProperties();
            }
            return changed;
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            boolean removed = delegate.removeIf(filter);
            if (removed) {
                dropSystemProperties();
            }
            return removed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = delegate.retainAll(c);
            if (changed) {
                dropSystemProperties();
            }
            return changed;
        }

        @Override
        public void clear() {
            delegate.clear();
            dropSystemProperties();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

    }

    private static class ChangeSignalingProperties extends Properties {

        private static final long serialVersionUID = -6198696951470526331L;
        
        private final Properties delegate;

        /**
         * Initializes a new {@link ChangeSignalingProperties}.
         *
         * @param delegate The delegate
         */
        ChangeSignalingProperties(Properties delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public Object setProperty(String key, String value) {
            Object prev = delegate.setProperty(key, value);
            dropSystemProperties();
            return prev;
        }

        @Override
        public void load(Reader reader) throws IOException {
            delegate.load(reader);
            dropSystemProperties();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Enumeration<Object> keys() {
            return delegate.keys();
        }

        @Override
        public Enumeration<Object> elements() {
            return delegate.elements();
        }

        @Override
        public boolean contains(Object value) {
            return delegate.contains(value);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public Object get(Object key) {
            return delegate.get(key);
        }

        @Override
        public void load(InputStream inStream) throws IOException {
            delegate.load(inStream);
            dropSystemProperties();
        }

        @Override
        public Object put(Object key, Object value) {
            Object prev = delegate.put(key, value);
            dropSystemProperties();
            return prev;
        }

        @Override
        public Object remove(Object key) {
            Object removed = delegate.remove(key);
            dropSystemProperties();
            return removed;
        }

        @Override
        public void putAll(Map<? extends Object, ? extends Object> t) {
            delegate.putAll(t);
            dropSystemProperties();
        }

        @Override
        public void clear() {
            delegate.clear();
            dropSystemProperties();
        }

        @Override
        public Object clone() {
            return delegate.clone();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public Set<Object> keySet() {
            return new ChangeSignalingSet<Object>(delegate.keySet());
        }

        @Override
        public Set<java.util.Map.Entry<Object, Object>> entrySet() {
            return new ChangeSignalingSet<java.util.Map.Entry<Object, Object>>(delegate.entrySet());
        }

        @Override
        public Collection<Object> values() {
            return new ChangeSignalingCollection<>(delegate.values());
        }

        @SuppressWarnings("deprecation")
        @Override
        public void save(OutputStream out, String comments) {
            delegate.save(out, comments);
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public void store(Writer writer, String comments) throws IOException {
            delegate.store(writer, comments);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public Object getOrDefault(Object key, Object defaultValue) {
            return delegate.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super Object, ? super Object> action) {
            delegate.forEach(action);
        }

        @Override
        public void store(OutputStream out, String comments) throws IOException {
            delegate.store(out, comments);
        }

        @Override
        public void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
            delegate.replaceAll(function);
            dropSystemProperties();
        }

        @Override
        public Object putIfAbsent(Object key, Object value) {
            Object opt = delegate.putIfAbsent(key, value);
            dropSystemProperties();
            return opt;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = delegate.remove(key, value);
            dropSystemProperties();
            return removed;
        }

        @Override
        public boolean replace(Object key, Object oldValue, Object newValue) {
            boolean replaced = delegate.replace(key, oldValue, newValue);
            dropSystemProperties();
            return replaced;
        }

        @Override
        public void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
            delegate.loadFromXML(in);
            dropSystemProperties();
        }

        @Override
        public Object replace(Object key, Object value) {
            Object replaced = delegate.replace(key, value);
            dropSystemProperties();
            return replaced;
        }

        @Override
        public Object computeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
            Object computed = delegate.computeIfAbsent(key, mappingFunction);
            dropSystemProperties();
            return computed;
        }

        @Override
        public void storeToXML(OutputStream os, String comment) throws IOException {
            delegate.storeToXML(os, comment);
        }

        @Override
        public Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
            Object computed = delegate.computeIfPresent(key, remappingFunction);
            dropSystemProperties();
            return computed;
        }

        @Override
        public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
            delegate.storeToXML(os, comment, encoding);
        }

        @Override
        public Object compute(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
            Object computed = delegate.compute(key, remappingFunction);
            dropSystemProperties();
            return computed;
        }

        @Override
        public Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
            Object merged = delegate.merge(key, value, remappingFunction);
            dropSystemProperties();
            return merged;
        }

        @Override
        public String getProperty(String key) {
            return delegate.getProperty(key);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return delegate.getProperty(key, defaultValue);
        }

        @Override
        public Enumeration<?> propertyNames() {
            return delegate.propertyNames();
        }

        @Override
        public Set<String> stringPropertyNames() {
            return delegate.stringPropertyNames();
        }

        @Override
        public void list(PrintStream out) {
            delegate.list(out);
        }

        @Override
        public void list(PrintWriter out) {
            delegate.list(out);
        }
    }

}
