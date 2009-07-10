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

package com.openexchange.subscribe.darkside;

import java.lang.reflect.Field;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;


/**
 * {@link WebClientCloser}
 *
 * User Black Magic to close all connections of the web client.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class WebClientCloser {
    private Field HTTP_CLIENT_FIELD;
    private Field CONNECTION_MANAGER_FIELD;
    
    private static final Log LOG = LogFactory.getLog(WebClientCloser.class);
    
    private boolean active = false;
    
    public WebClientCloser() {
        
        try {
            HTTP_CLIENT_FIELD = HttpWebConnection.class.getDeclaredField("httpClient_");
            HTTP_CLIENT_FIELD.setAccessible(true);
            CONNECTION_MANAGER_FIELD = HttpClient.class.getDeclaredField("httpConnectionManager");
            CONNECTION_MANAGER_FIELD.setAccessible(true);
            active = true;
        } catch (SecurityException e) {
            LOG.fatal(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            LOG.fatal(e.getMessage(), e);
        }
    }
    
    public void close(WebClient client) {
        
        if(!active) {
            LOG.error("Cannot close webclient");
        }
        
        MultiThreadedHttpConnectionManager manager = getManager(client);
        if(manager != null) {
            manager.shutdown();
        }
        
    }

    private MultiThreadedHttpConnectionManager getManager(WebClient client) {
        try {
            WebConnection webConnection = client.getWebConnection();
            if(webConnection == null) {
                return null;
            }
            if(!HttpWebConnection.class.isInstance(webConnection)) {
                LOG.error("Cannot close webclient: webConnection is not of class "+HttpWebConnection.class.getName());
                return null;
            }
            Object httpClient = HTTP_CLIENT_FIELD.get(webConnection);
            if(httpClient == null) {
                return null;
            }
            if(!HttpClient.class.isInstance(httpClient)) {
                LOG.error("Cannot close webclient: httpClient_ is not of class "+HttpClient.class.getName());
                return null;
            }
            Object manager = CONNECTION_MANAGER_FIELD.get(httpClient);
            if(manager == null) {
                return null;
            }
            
            if(!MultiThreadedHttpConnectionManager.class.isInstance(manager)) {
                LOG.error("Cannot close webclient: httpConnectionManager is not of class "+MultiThreadedHttpConnectionManager.class.getName());
                return null;
            }
            
            return (MultiThreadedHttpConnectionManager) manager;

        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
}
