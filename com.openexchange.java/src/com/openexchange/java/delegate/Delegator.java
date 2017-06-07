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

package com.openexchange.java.delegate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link Delegator} - Allows to automatically delegate the method call to the correct matching method. See <a href="http://www.javaspecialists.eu/archive/Issue168.html">http://www.javaspecialists.eu/archive/Issue168.html</a>.
 * <p>
 * Example:
 * <pre>
 * public void close() throws IOException {
 *   delegator.invoke();
 * }
 * </pre>
 * This even works when the method has parameters, such as:
 * <pre>
 * public void listen(int backlog) throws IOException {
 *   delegator.invoke(backlog);
 * }
 * </pre>
 * It cannot handle methods where the parameters are subclasses of each other. However, in these cases it will throw an exception, rather than do the wrong thing.<br>
 * If a method call is not delegated correctly, you can specify the method name and parameter types explicitly, like this:
 *
 * <pre>
 * public void connect(InetAddress address, int port) throws IOException {
 *   delegator
 *    .delegateTo("connect", InetAddress.class, int.class)
 *    .invoke(address, port);
 * }
 * </pre>
 *
 * @author Heinz Kabutz
 */
public class Delegator<C> {

    private final Object source;
    private final Object delegate;
    private final Class<C> superclass;

    public Delegator(Object source, Class<C> superclass, Object delegate) {
        this.source = source;
        this.superclass = superclass;
        this.delegate = delegate;
    }

    public Delegator(Object source, Class<C> superclass, String delegateClassName) {
        try {
            this.source = source;
            this.superclass = superclass;
            Class<?> implCl = Class.forName(delegateClassName);
            Constructor<?> delegateConstructor = implCl.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);
            this.delegate = delegateConstructor.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new DelegationException("Could not create delegate object", e);
        }
    }

    public final <T> T invoke(Object... args) {
        try {
            String methodName = extractMethodName();
            Method method = findMethod(methodName, args);
            @SuppressWarnings("unchecked") T t = (T) invoke0(method, args);
            return t;
        } catch (NoSuchMethodException e) {
            throw new DelegationException(e);
        }
    }

    private Object invoke0(Method method, Object[] args) {
        try {
            writeFields(superclass, source, delegate);
            method.setAccessible(true);
            Object result = method.invoke(delegate, args);
            writeFields(superclass, delegate, source);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new DelegationException(e.getCause());
        } catch (Exception e) {
            throw new DelegationException(e);
        }
    }

    private void writeFields(Class clazz, Object from, Object to) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            field.set(to, field.get(from));
        }
    }

    private String extractMethodName() {
        Throwable t = new Throwable();
        String methodName = t.getStackTrace()[2].getMethodName();
        return methodName;
    }

    private Method findMethod(String methodName, Object[] args) throws NoSuchMethodException {
        Class<?> clazz = superclass;
        if (args.length == 0) {
            return clazz.getDeclaredMethod(methodName);
        }
        Method match = null;
        next: for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] classes = method.getParameterTypes();
                if (classes.length == args.length) {
                    for (int i = 0; i < classes.length; i++) {
                        Class<?> argType = classes[i];
                        argType = convertPrimitiveClass(argType);
                        if (!argType.isInstance(args[i])) {
                            continue next;
                        }
                    }
                    if (match == null) {
                        match = method;
                    } else {
                        throw new DelegationException("Duplicate matches");
                    }
                }
            }
        }
        if (match != null) {
            return match;
        }
        throw new DelegationException("Could not find method: " + methodName);
    }

    private Class<?> convertPrimitiveClass(Class<?> primitive) {
        if (primitive.isPrimitive()) {
            if (primitive == int.class) {
                return Integer.class;
            }
            if (primitive == boolean.class) {
                return Boolean.class;
            }
            if (primitive == float.class) {
                return Float.class;
            }
            if (primitive == long.class) {
                return Long.class;
            }
            if (primitive == double.class) {
                return Double.class;
            }
            if (primitive == short.class) {
                return Short.class;
            }
            if (primitive == byte.class) {
                return Byte.class;
            }
            if (primitive == char.class) {
                return Character.class;
            }
        }
        return primitive;
    }

    public DelegatorMethodFinder<C> delegateTo(String methodName, Class<?>... parameters) {
        return new DelegatorMethodFinder<C>(this, methodName, parameters);
    }

    /**
     * Looks-up methods explicitly by name.
     */
    public static class DelegatorMethodFinder<C> {

        private final Method method;
        private final Delegator<C> delegator;

        DelegatorMethodFinder(Delegator<C> delegator, String methodName, Class<?>... parameterTypes) {
            try {
                method = delegator.superclass.getDeclaredMethod(methodName, parameterTypes);
                this.delegator = delegator;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new DelegationException(e);
            }
        }

        public <T> T invoke(Object... parameters) {
            @SuppressWarnings("unchecked") T t = (T) delegator.invoke0(method, parameters);
            return t;
        }
    }

}
