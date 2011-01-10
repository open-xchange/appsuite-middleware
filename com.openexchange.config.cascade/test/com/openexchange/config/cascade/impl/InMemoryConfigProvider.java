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

package com.openexchange.config.cascade.impl;

import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigProviderService;


/**
 * {@link InMemoryConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryConfigProvider implements ConfigProviderService{

    ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<String, Object>();
    ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> metadata = new ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>();
    
    
    public ConfigProperty get(final String property, final int context, final int user) {
        return new ConfigProperty() {

            public Object get() {
                return values.get(property);
            }

            public void set(Object value) {
                values.put(property, value);
            }

            public Object get(String metadataName) {
                return getMetadata().get(metadataName);
            }

            public void set(String metadataName, Object value) {
                getMetadata().put(metadataName, value);
            }

            private ConcurrentHashMap<String, Object> getMetadata() {
                ConcurrentHashMap<String, Object> retval = metadata.get(property);
                if(retval == null) {
                    retval = metadata.putIfAbsent(property, new ConcurrentHashMap<String, Object>());
                    if(retval == null) {
                        retval = metadata.get(property);
                    }
                }
                return retval;
            }

            public boolean isDefined() {
                return values.contains(property);
            }
            
        };
    }

}
