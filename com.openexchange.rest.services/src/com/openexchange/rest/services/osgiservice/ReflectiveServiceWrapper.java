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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services.osgiservice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.OXRESTMatch;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.OXRESTService.HALT;
import com.openexchange.rest.services.internal.OXRESTServiceWrapper;
import com.openexchange.rest.services.Response;


/**
 * The {@link ReflectiveServiceWrapper} wraps an {@link OXRESTService} and knows how to execute a service method
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReflectiveServiceWrapper implements OXRESTServiceWrapper {

    private Method method;
    private OXRESTService delegate;
    private OXRESTMatch match;
    private AJAXRequestData request;

    public ReflectiveServiceWrapper(Method method, OXRESTService newInstance, OXRESTMatch match) {
        super();
        this.method = method;
        this.delegate = newInstance;
        this.match = match;
        delegate.setMatch(match);
    }

    @Override
    public void setRequest(AJAXRequestData request) {
        this.request = request;
        delegate.setRequest(request);
    }

    @Override
    public OXRESTMatch getMatch() {
        return match;
    }

    @Override
    public Response execute() throws OXException {
        try {
            delegate.before();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object result;
            if (parameterTypes.length == 0) {
                // Just invoke the method
                result = method.invoke(delegate);                
            } else {
                Object[] args = new Object[parameterTypes.length];
                int i = 0;
                for (Class<?> paramType : parameterTypes) {
                    if (paramType != String.class) {
                        args[i] = request.getParameter(match.getParameterName(i), paramType);
                    } else {
                        args[i] = request.getParameter(match.getParameterName(i));
                    }
                    i++;
                }
                result = method.invoke(delegate, args);
            }
            
            if (result != null) {
                if (result instanceof Iterable) {
                    delegate.body((Iterable<String>) result);
                } else if (result instanceof String){
                    delegate.body((String) result);
                } else {
                    try {
                        delegate.body(JSONCoercion.coerceToJSON(result).toString());
                    } catch (JSONException x) {
                        delegate.body(result.toString());
                    }
                }
            }
        } catch (InvocationTargetException x) {
            if (x.getCause() instanceof OXException) {
                throw (OXException) x.getCause();
            } else if (x.getCause() instanceof HALT) {
                // processing has finished
                
            } else {
                // TODO
                x.printStackTrace();
            }
            
        } catch (IllegalArgumentException e) {
            // TODO
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO
            e.printStackTrace();
        } finally {
            delegate.after();
        }
        return delegate.getResponse();
    }
    
    
    public <T> OXRESTService<T> getDelegate() {
        return delegate;
    }
    

}
