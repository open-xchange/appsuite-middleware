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
//
//package com.openexchange.chronos.ical.ical4j;
//
//import java.util.HashMap;
//import java.util.Map;
//import net.fortuna.ical4j.model.Property;
//import net.fortuna.ical4j.model.PropertyFactory;
//import net.fortuna.ical4j.model.PropertyFactoryRegistry;
//import net.fortuna.ical4j.model.property.XProperty;
//
///**
// * {@link ICalPropertyFactory}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// * @since v7.10.0
// */
//public class ICalPropertyFactory extends PropertyFactoryRegistry {
//
//    private static final long serialVersionUID = -3842934076531500493L;
//
//    private final boolean preferExtended;
//    private final Map<String, PropertyFactory> extendedFactories;
//
//    public ICalPropertyFactory() {
//        this(true);
//    }
//
//    public ICalPropertyFactory(boolean preferExtended) {
//        super();
//        this.preferExtended = preferExtended;
//        this.extendedFactories = new HashMap<String, PropertyFactory>();
//    }
//
//    @Override
//    public void register(String name, PropertyFactory factory) {
//        extendedFactories.put(name, factory);
//    }
//
//    private PropertyFactory getPropertyFactory(String name) {
//        PropertyFactory factory;
//        if (preferExtended) {
//            factory = extendedFactories.get(name);
//            if (null == factory) {
//                factory = (PropertyFactory) getFactory(name);
//            }
//        } else {
//            factory = (PropertyFactory) getFactory(name);
//            if (null == factory) {
//                factory = extendedFactories.get(name);
//            }
//        }
//        return factory;
//    }
//
//    @Override
//    public Property createProperty(String name) {
//        PropertyFactory factory = getPropertyFactory(name);
//        if (factory != null) {
//            return factory.createProperty(name);
//        }
//        if (allowIllegalNames() || name.startsWith(Property.EXPERIMENTAL_PREFIX) && name.length() > Property.EXPERIMENTAL_PREFIX.length()) {
//            return new XProperty(name);
//        }
//        throw new IllegalArgumentException("Illegal property [" + name + "]");
//    }
//
//}
