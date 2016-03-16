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

package com.openexchange.realtime.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Asynchronous;
import com.openexchange.realtime.osgi.RealtimeServiceRegistry;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * An {@link ActionHandler} inspects (via introspection) a class and looks for public methods in this form:
 * 
 * public void handleSomething(Stanza stanza) 
 * or
 * public void handleSomething(Stanza stanza, Map<String, Object> options)
 * 
 * these methods may also return a boolean if they like:
 * 
 * public void handleSomething(Stanza stanza) 
 * or
 * public void handleSomething(Stanza stanza, Map<String, Object> options)
 * 
 * "Something" being the variable part. Methods may then be referenced by a message that looks like this:
 * {
 *    element: "message",
 *    payloads: [
 *         { element: "action", data: "something" },
 *          ...
 *    ],
 *    to: ...,
 *    session: "...
 *  }
 *  
 *  With "something" being the name of the method to call (or rather the name of the method is handle + the data of the "action" element of the stanza) 
 *  The method may then use the Stanzas payload trees retrieval methods e.g. {@link Stanza#getPayloadTrees(ElementPath)} to retrieve additional data
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ActionHandler {
    private final Map<String, Method> methodsWithProperties = new HashMap<String, Method>();
    private final Map<String, Method> methods = new HashMap<String, Method>();

    public ActionHandler(Class<?> klass) {
        for (Method method: klass.getMethods()) {
            boolean accessible = Modifier.isPublic(method.getModifiers());
            
            if (accessible && method.getName().startsWith("handle")) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0) {
                    if (parameterTypes[0].isAssignableFrom(Stanza.class)) {
                        String name = method.getName().substring(6).toLowerCase();
                        if (parameterTypes.length == 2 && parameterTypes[1].isAssignableFrom(Map.class)) {
                            methodsWithProperties.put(name, method);
                        } else if (parameterTypes.length == 1) {
                            methods.put(name, method);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Have the ActionHandler choose an appropriate method for the given stanza and calls it on the given handler.
     * @return true when a method is found and it in turn didn't return true, false if either no matching method was found or the method returned false
     */
    public boolean callMethod(Object handler, Stanza stanza) throws OXException {
        return callMethod(handler, stanza, null);
    }
    /**
     * Have the ActionHandler choose an appropriate method for the given stanza and calls it on the given handler. You can pass additional options to the method
     * via the options map
     * @return true when a method is found and it in turn didn't return true, false if either no matching method was found or the method returned false
     */
    public boolean callMethod(Object handler, Stanza stanza, Map<String, Object> options) throws OXException {
        Collection<PayloadTree> payloads = stanza.getPayloadTrees(new ElementPath("action"));
        for (PayloadTree payloadTree : payloads) {
            String name = payloadTree.getRoot().getData().toString();
            if (options == null) {
                Method method = methods.get(name.toLowerCase());
                if (method != null) {
                   return invoke(method, handler, stanza, null);
                }
                options = new HashMap<String, Object>();
            }
            
            Method method = methodsWithProperties.get(name.toLowerCase());
            if (method != null) {
                return invoke(method, handler, stanza, options);
            }
        }
        
        return false;
    }
    
    
    private boolean invoke(final Method method, final Object handler, final Stanza stanza, final Map<String, Object> options) throws OXException {
        try {
            if (isAsynchronous(method)) {
                ThreadPoolService threads = RealtimeServiceRegistry.getInstance().getService(ThreadPoolService.class);
                threads.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        if (options != null) {
                            method.invoke(handler, new Object[]{stanza, options}); 
                        } else {
                            method.invoke(handler, new Object[]{stanza});
                        }
                        return null;
                    }
                    
                });
                return true;
            } else {
                Object result = null;
                if (options != null) {
                    result = method.invoke(handler, new Object[]{stanza, options}); 
                } else {
                    result = method.invoke(handler, new Object[]{stanza});
                }
                if (result != null && Boolean.class.isInstance(result)) {
                    return (Boolean) result;
                } else {
                    return true;
                }
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (OXException.class.isInstance(cause)) {
                throw (OXException) cause;
            }
            throw new OXException(e);
        } catch (IllegalArgumentException e) {
            throw new OXException(e);
        } catch (IllegalAccessException e) {
            throw new OXException(e);
        }
   }

    public static boolean isAsynchronous(Method method) {
        Asynchronous annotation = method.getAnnotation(Asynchronous.class);
        return annotation != null;
    }

    public static PayloadTree getMethodCall(String methodName) {
        return new PayloadTree(
            PayloadTreeNode.builder()
            .withPayload(
                new PayloadElement(methodName, "json", null, "action") 
            ).build()
        );
    }

}
