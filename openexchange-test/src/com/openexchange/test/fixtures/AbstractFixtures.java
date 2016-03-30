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
package com.openexchange.test.fixtures;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.transformators.Transformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Markus Wagner <markus.wagner@open-xchange.com>
 */
public abstract class AbstractFixtures<T> implements Fixtures<T> {

    private Class<T> klass = null;
    private final Map<Class, Transformator> transformators = new HashMap<Class, Transformator>();
    private final Map<String, Transformator> attributeTransformators = new HashMap<String, Transformator>();

    private final Map<String, List<String>> synonyms = new HashMap<String, List<String>>();
    private final List<String> entryNames;

    public AbstractFixtures(final Class<T> klass, final Map values) {
        this.klass = klass;
        this.entryNames = new ArrayList<String>(values.keySet());
    }

    public void addTransformator(final Transformator transformator, final Class into) {
        transformators.put(into, transformator);
    }

    public void addTransformator(final Transformator transformator, final String attribute) {
        attributeTransformators.put(attribute, transformator);
    }

    public void addSynoym(final String attribute, final String...syns) {
        List<String> synList = synonyms.get(attribute);
        if (synList == null) {
            synList = new ArrayList<String>();
            synonyms.put(attribute, synList);
        }
        for (String syn : syns) {
            synList.add(syn);
        }

    }

    @Override
    public List<String> getEntryNames() {
        return entryNames;
    }

    protected void apply(final T bean, final Map attributes) throws OXException {
        for (Object o : attributes.keySet()) {
            String value = null;
            if(attributes.containsKey(o) && null != attributes.get(o)){
            	value = attributes.get(o).toString();
            }

            Object param = value;
            final Method[] setters = discoverSetters(o.toString());
            for (int i = 0; i < setters.length; i++) {
                if (false == setters[i].getParameterTypes()[0].equals(String.class)) {
                    param = getTransformator(setters[i].getParameterTypes()[0], o.toString()).transform(value);
                } else if (null != value) {
                	param = value;
                } else {
                	param = null;
                }
                try {
                	setters[i].invoke(bean, param);
                } catch (IllegalArgumentException e) {
                	if (i < setters.length) {
                		continue;
                	} else {
                		throw new FixtureException(e);
                	}
                }
                catch (IllegalAccessException e) {
               		throw new FixtureException(e);
                } catch (InvocationTargetException e) {
               		throw new FixtureException(e);
                }
			}
        }
    }

    private Transformator getTransformator(final Class aClass, final String attribute) throws OXException {
        if(attributeTransformators.containsKey(attribute)) {
            return attributeTransformators.get(attribute);
        }
        if(!transformators.containsKey(aClass)) {
            transformators.put(aClass, new StringConstructorTransformator(aClass));
        }
        return transformators.get(aClass);
    }

    private Method discoverSetter(final String attribute) throws OXException {

    	final List<String> methodNames = new ArrayList<String>();
        methodNames.add( IntrospectionTools.setterName(attribute) );
        for(String synonym : getSynonyms(attribute)) {
            methodNames.add( IntrospectionTools.setterName(synonym) );
        }


        for(Method method : klass.getMethods()) {
            for(String acceptableName : methodNames) {
                if(method.getName().equalsIgnoreCase(acceptableName) && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        throw new FixtureException("Don't know how to set attribute "+attribute);
    }

    private Method[] discoverSetters(final String attribute) throws OXException {
    	final List<String> methodNames = new ArrayList<String>();
        methodNames.add(IntrospectionTools.setterName(attribute));
        methodNames.add(IntrospectionTools.adderName(attribute));
        for (final String synonym : getSynonyms(attribute)) {
            methodNames.add(IntrospectionTools.adderName(synonym));
        }
        final List<Method> methods = new ArrayList<Method>();
        for (final Method method : klass.getMethods()) {
            for (final String acceptableName : methodNames) {
                if (method.getName().equalsIgnoreCase(acceptableName) && method.getParameterTypes().length == 1) {
                	methods.add(method);
                }
            }
        }
        if (0 < methods.size()) {
        	return methods.toArray(new Method[methods.size()]);
        } else {
        	return new Method[0];
        }
    }

    private List<String> getSynonyms(final String attribute) {
        if(synonyms.containsKey(attribute)) {
            return synonyms.get(attribute);
        }
        return Collections.EMPTY_LIST ;
    }

    private class StringConstructorTransformator implements Transformator {
        private Constructor<?> constructor;

        public StringConstructorTransformator(final Class<?> aClass) throws OXException {
            try {
                this.constructor = aClass.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw new FixtureException("I don't know how to turn a String into a "+aClass.toString(), e);
            }
        }

        @Override
        public Object transform(final String value) throws OXException {
            try {
                return constructor.newInstance(value);
            } catch (InstantiationException e) {
                throw new FixtureException(e);
            } catch (IllegalAccessException e) {
                throw new FixtureException(e);
            } catch (InvocationTargetException e) {
                throw new FixtureException(e);
            }
        }
    }
}
