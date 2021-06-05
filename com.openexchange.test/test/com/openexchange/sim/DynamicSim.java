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

package com.openexchange.sim;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * {@link DynamicSim}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DynamicSim implements InvocationHandler{

    private Throwable exception;
    private Object retval;
    private boolean wasCalled;

    private final Expectation expectation;
    private Block block;

    public DynamicSim(Expectation expectation) {
        super();
        this.expectation = expectation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        expectation.verify(method, args);
        wasCalled = true;
        if (exception != null) {
            throw exception;
        }
        if (retval != null) {
            return retval;
        }
        if (block != null) {
            return block.perform(proxy, args);
        }
        return null;
    }

    public <T> T become(Class<T> klass) {
        @SuppressWarnings("unchecked")
        T returnVal = (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, this);
        return returnVal;
    }

    public static <T> T compose(Class<T> klass, DynamicSim...sims) {
        return compose(klass, new Class[0], sims);
    }

    public static<T> T compose(Class<T> klass, List<DynamicSim> dynamicSims) {
        return compose(klass, new Class[0], dynamicSims);
    }

    public static <T> T compose(Class<T> klass, Class<?>[] additionalClasses, DynamicSim...sims) {
        SequenceInvocationHandler sequenceInvocationHandler = new SequenceInvocationHandler(sims);
        Class<?>[] classes = new Class[additionalClasses.length+1];
        classes[0] = klass;
        int i = 1;
        for(Class<?> c : additionalClasses) {
            classes[i++] = c;
        }
        @SuppressWarnings("unchecked")
        T retval = (T) Proxy.newProxyInstance(klass.getClassLoader(), classes, sequenceInvocationHandler);
        return retval;
    }

    public static<T> T compose(Class<T> klass, Class<?>[] additionalClasses, List<DynamicSim> dynamicSims) {
        return compose(klass, additionalClasses, dynamicSims.toArray(new DynamicSim[dynamicSims.size()]));
    }

    private static final class SequenceInvocationHandler implements InvocationHandler {
        private final InvocationHandler[] invocationHandlers;
        private int index = 0;

        public SequenceInvocationHandler(InvocationHandler[] handlers) {
            this.invocationHandlers = handlers;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (index == invocationHandlers.length) {
                throw new IllegalStateException("Didn't expect method call: "+method);
            }
            return invocationHandlers[index++].invoke(proxy, method, args);
        }


    }

    public boolean wasCalled() {
        return wasCalled;
    }


    public void setReturnValue(Object retval) {
        this.retval = retval;
    }

    public Expectation getExpectation() {
        return expectation;
    }

    public void setException(Throwable x) {
        this.exception = x;
    }

    public void setBlock(Block block) {
        this.block = block;
    }





}
