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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

    private static Set<Method> getAllMethodsRecursively(Class<?> cl) {
        Set<Method> methods = new LinkedHashSet<>();
        for (Class<?> current = cl; current != null; current = current.getSuperclass()) {
            Collections.addAll(methods, current.getMethods());
            Collections.addAll(methods, current.getDeclaredMethods());
        }
        return methods;
    }

    private static Map<String, List<Method>> getAllMethods(Class<?> cl) {
        Set<Method> declaredMethods = getAllMethodsRecursively(cl);
        Map<String, List<Method>> m = new LinkedHashMap<>(declaredMethods.size());
        for (Method method : declaredMethods) {
            String name = method.getName();
            List<Method> list = m.get(name);
            if (null == list) {
                list = new ArrayList<>(2);
                m.put(name, list);
            }
            list.add(method);
        }

        ImmutableMap.Builder<String, List<Method>> b = ImmutableMap.builder();
        for (Map.Entry<String, List<Method>> e : m.entrySet()) {
            b.put(e.getKey(), ImmutableList.copyOf(e.getValue()));
        }
        return b.build();
    }

    /** The options for delegating to a certain method */
    public static class Options {

        /**
         * Creates a new <code>Options</code> instance.
         *
         * @return The new <code>Options</code> instance
         */
        public static Options options() {
            return new Options();
        }

        private static final Object[] EMPTY_ARGS = new Object[0];

        // -------------------------------------------------------------------------------------

        String optMethodName;
        Object[] args;

        Options() {
            super();
            optMethodName = null;
            args = EMPTY_ARGS;
        }

        /**
         * Sets the arguments to pass to delegate method.
         *
         * @param args The arguments
         * @return This options
         */
        public Options withArgs(Object... args) {
            this.args = args;
            return this;
        }

        /**
         * Sets the method name.
         *
         * @param methodName The method name
         * @return This options
         */
        public Options withMethodName(String methodName) {
            this.optMethodName = methodName;
            return this;
        }
    }

    // -----------------------------------------------------------------------------------

    private final Object source;
    private final Object delegate;
    final Class<C> superclass;
    private final Map<String, List<Method>> declaredMethods;

    /**
     * Initializes a new {@link Delegator}.
     *
     * @param source The source instance that wants to delegate
     * @param superclass The type to which shall be delegated
     * @param delegate The instance to which shall be delegated
     */
    public Delegator(Object source, Class<C> superclass, Object delegate) {
        this.source = source;
        this.superclass = superclass;
        this.delegate = delegate;
        this.declaredMethods = getAllMethods(superclass);
    }

    /**
     * Initializes a new {@link Delegator}.
     *
     * @param source The source instance that wants to delegate
     * @param superclass The type to which shall be delegated
     * @param delegateClassName The name of the class to which shall be delegated
     */
    public Delegator(Object source, Class<C> superclass, String delegateClassName) {
        try {
            this.source = source;
            this.superclass = superclass;
            Class<?> implCl = Class.forName(delegateClassName);
            Constructor<?> delegateConstructor = implCl.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);
            this.delegate = delegateConstructor.newInstance();
            this.declaredMethods = getAllMethods(superclass);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new DelegationException("Could not create delegate object", e);
        }
    }

    /**
     * Gets the delegate
     *
     * @return The delegate
     */
    public Object getDelegate() {
        return delegate;
    }

    /**
     * Delegates to the method invocation of the associated instance.
     *
     * @param options The options to pass
     * @return The invocation result
     * @throws DelegationException If delegation generally fails
     * @throws DelegationExecutionException If execution itself fails; providing the causing exception
     */
    public final <T> T invoke(Options options) {
        String methodName = null == options.optMethodName ? extractMethodName() : options.optMethodName;
        Object[] args = options.args;
        Method method = findMethod(methodName, args);
        @SuppressWarnings("unchecked") T t = (T) invoke0(method, args);
        return t;
    }

    /**
     * Delegates to the method invocation of the associated instance.
     *
     * @param args The method arguments to pass
     * @return The invocation result
     * @throws DelegationException If delegation generally fails
     * @throws DelegationExecutionException If execution itself fails; providing the causing exception
     */
    public final <T> T invoke(Object... args) {
        String methodName = extractMethodName();
        Method method = findMethod(methodName, args);
        @SuppressWarnings("unchecked") T t = (T) invoke0(method, args);
        return t;
    }

    Object invoke0(Method method, Object[] args) {
        try {
            writeFields(superclass, source, delegate);
            method.setAccessible(true);

            Object result = method.invoke(delegate, args);

            writeFields(superclass, delegate, source);
            return result;
        } catch (DelegationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DelegationException(e);
        } catch (InvocationTargetException e) {
            throw new DelegationExecutionException(e.getCause());
        } catch (Exception e) {
            throw new DelegationException(e);
        }
    }

    private void writeFields(Class<?> clazz, Object from, Object to) throws Exception {
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

    private Method findMethod(String methodName, Object[] args) {
        List<Method> methodsByName = declaredMethods.get(methodName);
        if (null == methodsByName) {
            throw new DelegationException("Could not find method " + methodName + " in class " + superclass.getName());
        }

        if (args.length == 0) {
            for (Method method : methodsByName) {
                Class<?>[] classes = method.getParameterTypes();
                if (classes.length == 0) {
                    return method;
                }
            }
        } else {
            Method match = null;
            next: for (Method candidate : methodsByName) {
                Class<?>[] classes = candidate.getParameterTypes();
                if (classes.length == args.length) {
                    for (int i = 0; i < classes.length; i++) {
                        Class<?> argType = classes[i];
                        argType = convertPrimitiveClass(argType);
                        if (!argType.isInstance(args[i])) {
                            continue next;
                        }
                    }
                    if (match != null) {
                        throw new DelegationException("Duplicate matches for " + methodName + " in class " + superclass.getName());
                    }
                    match = candidate;
                }
            }
            if (match != null) {
                return match;
            }
        }

        throw new DelegationException("Could not find method " + methodName + " in class " + superclass.getName());
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

        private static Method optMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
            try {
                return clazz.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static Method optDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private final Method method;
        private final Delegator<C> delegator;

        DelegatorMethodFinder(Delegator<C> delegator, String methodName, Class<?>... parameterTypes) {
            try {
                Class<C> clazz = delegator.superclass;
                Method m = null;
                for (Class<?> current = clazz; null == m && null != current; current = current.getSuperclass()) {
                    m = optMethod(current, methodName, parameterTypes);
                    if (null == m) {
                        m = optDeclaredMethod(current, methodName, parameterTypes);
                    }
                }
                if (null == m) {
                    throw new DelegationException("Could not find method " + methodName + " in class " + clazz.getName());
                }
                method = m;
                this.delegator = delegator;
            } catch (RuntimeException e) {
                throw e;
            }
        }

        public <T> T invoke(Object... parameters) {
            @SuppressWarnings("unchecked") T t = (T) delegator.invoke0(method, parameters);
            return t;
        }
    }

}
