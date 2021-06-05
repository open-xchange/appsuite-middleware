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


package com.openexchange.hazelcast.serialization.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.Portable;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;

/**
 * {@link HazelcastConfigurationServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DynamicPortableFactoryImpl implements DynamicPortableFactory {

    /** Named logger instance */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DynamicPortableFactoryImpl.class);

    private final ConcurrentMap<Integer, CustomPortableFactory> factories;

    /**
     * Initializes a new {@link DynamicPortableFactoryImpl}.
     */
    public DynamicPortableFactoryImpl() {
        super();
        this.factories = new ConcurrentHashMap<Integer, CustomPortableFactory>(128, 0.9F, 1);
    }

    @Override
    public Portable create(int classId) {
        CustomPortableFactory factory = factories.get(Integer.valueOf(classId));
        if (null == factory) {
            LOG.warn("No portable factory found for class ID {}, unable to instantiate Portable", Integer.valueOf(classId), new Throwable());
            return null; // will throw com.hazelcast.nio.serialization.HazelcastSerializationException afterwards
        }
        return factory.create();
    }

    /**
     * Registers a custom portable factory.
     *
     * @param factory The factory to register
     * @return The previously registered factory for the class ID, or <code>null</code> if there was no such factory registered before
     */
    public CustomPortableFactory register(CustomPortableFactory factory) {
        Integer classId = Integer.valueOf(factory.getClassId());
        CustomPortableFactory previousRegistration = factories.put(classId, factory);
        if (null == previousRegistration) {
            LOG.info("Registered custom portable factory for class ID {}: {}", classId, factory.getClass().getName());
        } else {
            LOG.warn("Replaced previously registered custom portable factory for class ID {} ({}) with new factory: {}", classId, previousRegistration.getClass().getName(), factory.getClass().getName());
        }
        return previousRegistration;
    }

    /**
     * Unregisters a previously registered custom portable factory.
     *
     * @param factory The factory to unregister
     * @return The previously registered factory for the class ID, or <code>null</code> if there was no such factory registered before
     */
    public CustomPortableFactory unregister(CustomPortableFactory factory) {
        Integer classId = Integer.valueOf(factory.getClassId());
        CustomPortableFactory removedRegistration = factories.remove(classId);
        if (null != removedRegistration) {
            LOG.info("Unregistered custom portable factory for class ID {}: {}", classId, factory.getClass().getName());
        } else {
            LOG.warn("Unable to unregister not yet registered custom portable factory for class ID {}: {}", classId, factory.getClass().getName());
        }
        return removedRegistration;
    }

    @Override
    public Collection<ClassDefinition> getClassDefinitions() {
        Collection<CustomPortableFactory> registeredFactories = factories.values();
        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(registeredFactories.size());
        for (CustomPortableFactory factory : registeredFactories) {
            ClassDefinition classDefinition = factory.getClassDefinition();
            if (classDefinition != null) {
                classDefinitions.add(classDefinition);
            }
        }
        return classDefinitions;
    }

}
