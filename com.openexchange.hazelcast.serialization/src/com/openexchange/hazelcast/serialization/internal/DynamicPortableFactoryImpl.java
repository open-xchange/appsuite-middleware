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


package com.openexchange.hazelcast.serialization.internal;

import java.util.ArrayList;
import java.util.Collection;
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
        this.factories = new ConcurrentHashMap<Integer, CustomPortableFactory>();
    }

    @Override
    public Portable create(int classId) {
        CustomPortableFactory factory = factories.get(Integer.valueOf(classId));
        if (null == factory) {
            LOG.warn("No portable factory found for class ID {}, unable to instantiate Portable", classId, new Throwable());
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
        CustomPortableFactory previousRegistration = factories.put(Integer.valueOf(factory.getClassId()), factory);
        if (null == previousRegistration) {
            LOG.info("Registered custom portable factory for class ID {}: {}", factory.getClassId(), factory.getClass().getName());
        } else {
            LOG.warn("Replaced previously registered custom portable factory for class ID {} ({}) with new factory: {}",
                factory.getClassId(), previousRegistration.getClass().getName(), factory.getClass().getName());
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
         CustomPortableFactory removedRegistration = factories.remove(Integer.valueOf(factory.getClassId()));
         if (null != removedRegistration) {
             LOG.info("Unregistered custom portable factory for class ID {}: {}", factory.getClassId(), factory.getClass().getName());
         } else {
             LOG.warn("Unable to unregister not yet registered custom portable factory for class ID {}: {}",
                 factory.getClassId(), factory.getClass().getName());
         }
         return removedRegistration;
    }

    @Override
    public Collection<ClassDefinition> getClassDefinitions() {
        Collection<CustomPortableFactory> registeredFactories = factories.values();
        ArrayList<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(registeredFactories.size());
        for (CustomPortableFactory factory : registeredFactories) {
            ClassDefinition classDefinition = factory.getClassDefinition();
            if(classDefinition != null) {
                classDefinitions.add(classDefinition);
            }
        }
        return classDefinitions;
    }

}
