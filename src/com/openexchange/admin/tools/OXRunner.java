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
package com.openexchange.admin.tools;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.cache.Configuration;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderCacheNotEnabledException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.database.DatabaseInit;
import com.openexchange.event.EventInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.server.DBPoolingException;

/**
 * 
 * @author cutmasta
 */
public class OXRunner {
    private final Log log = LogFactory.getLog(this.getClass());

    private void fatal(Exception e) {
        this.log.fatal("Unable to initialize OX Process",e);
    }
    
    public void init() {
        try {
            SystemConfig.init();
        } catch (ConfigurationException e1) {
            fatal(e1);
        }
        // ComfireConfig.loadProperties(System.getProperty("openexchange.propfile"));
        try {
            ConfigurationInit.init();
        } catch (AbstractOXException e) {
            fatal(e);
        }
        try {
            Configuration.load();
        } catch (IOException e) {
            fatal(e);
        }
        try {
            DatabaseInit.init();
            ContextStorage.init();
            /*
             * Force cache initialization
             */
            FolderCacheManager.getInstance();
            EventInit.init();
        } catch (DBPoolingException e) {
            fatal(e);
        } catch (ContextException e) {
            fatal(e);
        } catch (FolderCacheNotEnabledException e) {
            fatal(e);
        } catch (OXException e) {
            fatal(e);
        }
    }
}
