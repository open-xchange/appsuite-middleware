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

package com.openexchange.serialization.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.serialization.ClassResolver;

/**
 * {@link FilteringObjectInputStream} prevents invalid deserialization by using a blacklist
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class FilteringObjectInputStream extends ObjectInputStream {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FilteringObjectInputStream.class);
    }

    private final SerializationFilteringConfig config;
    private final Set<ClassResolver> classResolvers;

    /**
     * Initializes a new {@link FilteringObjectInputStream}.
     *
     * @param in The {@link InputStream}
     * @param optContext An optional context object from which class loading has to be done
     * @param optClassResolver The class resolver or <code>null</code>
     * @param config The configuration to use
     * @throws IOException If an I/O error occurs
     */
    FilteringObjectInputStream(InputStream in, Object optContext, ClassResolver optClassResolver, SerializationFilteringConfig config) throws IOException {
        super(in);
        Set<ClassResolver> classResolvers = new LinkedHashSet<ClassResolver>(8);
        if (null != optClassResolver) {
            classResolvers.add(optClassResolver);
        }
        if (null != optContext) {
            classResolvers.add(new ClassLoaderClassResolver(optContext.getClass().getClassLoader()));
        }
        this.classResolvers = classResolvers;
        this.config = config;
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass input) throws IOException, ClassNotFoundException {
        String name = input.getName();

        for (Pattern blackPattern : config.getBlacklist()) {
            if (blackPattern.matcher(name).find()) {
                LoggerHolder.LOG.error("Blocked by blacklist '{}'. Match found for '{}'", blackPattern.pattern(), name);
                throw new InvalidClassException(name, "Class blocked from deserialization (blacklist)");
            }
        }

        for (ClassResolver classResolver : classResolvers) {
            try {
                Class<?> clazz = classResolver.resolveClass(name);
                if (clazz != null) {
                    classResolvers.add(new ClassLoaderClassResolver(clazz.getClassLoader()));
                    return clazz;
                }
            } catch (@SuppressWarnings("unused") Exception e) {
                // Ignore
            }
        }

        return super.resolveClass(input);

    }

}
