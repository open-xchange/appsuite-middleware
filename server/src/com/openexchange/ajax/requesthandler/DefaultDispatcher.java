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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.AjaxException.Code;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcher}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDispatcher implements Dispatcher {

    private Map<String, AJAXActionServiceFactory> actionFactories = new ConcurrentHashMap<String, AJAXActionServiceFactory>();

    private Queue<AJAXActionCustomizerFactory> customizerFactories = new ConcurrentLinkedQueue<AJAXActionCustomizerFactory>();

    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws AbstractOXException {
        List<AJAXActionCustomizer> outgoing = new ArrayList<AJAXActionCustomizer>(customizerFactories.size());
        List<AJAXActionCustomizer> todo = new LinkedList<AJAXActionCustomizer>();
        
        for (AJAXActionCustomizerFactory customizerFactory : customizerFactories) {
            AJAXActionCustomizer customizer = customizerFactory.createCustomizer(request, session);
            if(customizer != null) {
                todo.add(customizer);
            }
        }
        
        while(!todo.isEmpty()) {
            Iterator<AJAXActionCustomizer> iterator = todo.iterator();
            while(iterator.hasNext()) {
                AJAXActionCustomizer customizer = iterator.next();
                try {
                    AJAXRequestData modified = customizer.incoming(request, session);
                    if (modified != null) {
                        request = modified;
                    }

                    outgoing.add(customizer);
                    iterator.remove();
                } catch (FlowControl.Later l) {
                    // Remains in list and is therefore retried
                }
            }
        }
        
        AJAXActionServiceFactory factory = lookupFactory(request.getModule());
        
        if (factory == null) {
            throw new AjaxException(AjaxException.Code.UNKNOWN_MODULE, request.getModule());
        }
        AJAXActionService action = factory.createActionService(request.getAction());
        if (action == null) {
            throw new AjaxException(Code.UnknownAction, request.getAction());
        }
        Action actionMetadata = getActionMetadata(action);
        
        if (actionMetadata != null) {
            if (request.getFormat() == null) {
                request.setFormat(actionMetadata.defaultFormat());
            }
        } else {
            if (request.getFormat() == null) {
                request.setFormat("apiResponse");
            }
        }
        
        AJAXRequestResult result = action.perform(
            request,
            session);
        
        Collections.reverse(outgoing);
        outgoing = new LinkedList<AJAXActionCustomizer>(outgoing);
        while(!outgoing.isEmpty()) {
            Iterator<AJAXActionCustomizer> iterator = outgoing.iterator();
            
            while (iterator.hasNext()) {
                AJAXActionCustomizer customizer = iterator.next();
                try {
                    AJAXRequestResult modified = customizer.outgoing(request, result, session);
                    if (modified != null) {
                        result = modified;
                    }
                    iterator.remove();
                } catch (FlowControl.Later l) {
                    // Remains in list and is therefore retried
                }
            }
            
        }
        return result;
    }

    private AJAXActionServiceFactory lookupFactory(String module) {
        AJAXActionServiceFactory serviceFactory = actionFactories.get(module);
        if (serviceFactory == null && module.contains("/")) {
            // Fallback for backwards compatibility. File Download Actions sometimes append the filename to the module.
            serviceFactory = actionFactories.get(module.split("/")[0]);
        }
        return serviceFactory;
    }

    private Action getActionMetadata(AJAXActionService action) {
        return action.getClass().getAnnotation(Action.class);
    }

    public void register(String module, AJAXActionServiceFactory factory) {
        actionFactories.put(module, factory);
    }

    public void addCustomizer(AJAXActionCustomizerFactory factory) {
        this.customizerFactories.add(factory);
    }

    public void remove(String module) {
        actionFactories.remove(module);
    }

}
