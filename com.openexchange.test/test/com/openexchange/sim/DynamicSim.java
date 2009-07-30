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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
    
    private Expectation expectation;

    public DynamicSim(Expectation expectation) {
        super();
        this.expectation = expectation;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        expectation.verify(method, args);
        wasCalled = true;
        if(exception != null) {
            throw exception;
        }
        if(retval != null) {
            return retval;
        }
        return null;
    }
    
    public <T> T become(Class<T> klass) {
        return (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, this);
    }
    
    public static <T> T compose(Class<T> klass, DynamicSim...sims) {
        SequenceInvocationHandler sequenceInvocationHandler = new SequenceInvocationHandler(sims);
        return (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, sequenceInvocationHandler);
    }
    
    public static<T> T compose(Class<T> klass, List<DynamicSim> dynamicSims) {
        return compose(klass, dynamicSims.toArray(new DynamicSim[dynamicSims.size()]));
    }
    
    private static final class SequenceInvocationHandler implements InvocationHandler {
        private InvocationHandler[] invocationHandlers;
        private int index = 0;
        
        public SequenceInvocationHandler(InvocationHandler[] handlers) {
            this.invocationHandlers = handlers;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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


    
    
    
}
