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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.Scope;
import static com.openexchange.config.cascade.Scope.*;


/**
 * {@link ConfigCascade}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascade implements ConfigViewFactory {

    ConcurrentHashMap<Scope, ConfigProviderService> providers = new ConcurrentHashMap<Scope, ConfigProviderService>();
    Scope[] searchPath = Scope.searchPath();
    private List<ConfigProviderService> path;
    
    public void setProvider(Scope scope, ConfigProviderService configProvider) {
        providers.put(scope, configProvider);
    }

    public ConfigView getView(int user, int context) {
        return new View(user, context);
    }

    public void setSearchPath(Scope...searchPath) {
        this.searchPath = searchPath;
        this.path = null;
    }
    
    protected List<ConfigProviderService> getConfigProviders() {
        if (path != null) {
            return path;
        }
        
        List<ConfigProviderService> p = new ArrayList<ConfigProviderService>();
        for(Scope scope : searchPath) {
            p.add(providers.get(scope));
        }
        return path = p;
    }
 
    
    private final class View implements ConfigView {
        int context;
        int user;

        public View(int user, int context) {
            this.user = user;
            this.context = context;
        }

        public void set(Scope scope, String property, Object value) {
            property(scope, property).set(value);
        }

        public Object get(String property) {
            return property(property).get();
        }

        public ConfigProperty property(Scope scope, String property) {
            return providers.get(scope).get(property, context, user);
        }

        public ComposedConfigProperty property(final String property) {
            return new ComposedConfigProperty() {

                private Scope[] overriddenScopes;

                public Object get() {
                    Scope finalScope = getFinalScope();
                    for(ConfigProviderService provider : getConfigProviders(finalScope)) {
                        Object value = provider.get(property, context, user).get();
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                private Scope getFinalScope() {
                    String scopeS = (String) property(property).precedence(SERVER, CONTEXT, USER).get("final");
                    if(scopeS == null) {
                        return null;
                    }
                    return Scope.valueOf(scopeS.toUpperCase());
                }

                public Object get(String metadataName) {
                    for(ConfigProviderService provider : getConfigProviders(null)) {
                        Object value = provider.get(property, context, user).get(metadataName);
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                public void set(String metadataName, Object value) {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                public void set(Object value) {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                public ComposedConfigProperty precedence(Scope... scopes) {
                    overriddenScopes = scopes;
                    return this;
                }
                
                private List<ConfigProviderService> getConfigProviders(Scope finalScope) {
                    Scope[] s = (overriddenScopes != null) ? overriddenScopes : searchPath;
                    
                    List<ConfigProviderService> p = new ArrayList<ConfigProviderService>();
                    boolean collect = false;
                    for(Scope scope : s) {
                        collect = collect || finalScope == null || finalScope == scope;

                        if(collect) {
                            p.add(providers.get(scope));
                            if(scope == finalScope) {
                                return p;
                            }
                        }
                    }
                    return p;
                }

                public boolean isDefined() {
                    Scope finalScope = getFinalScope();
                    for(ConfigProviderService provider : getConfigProviders(finalScope)) {
                        boolean defined = provider.get(property, context, user).isDefined();
                        if (defined) {
                            return defined;
                        }
                    }
                    return false;
                }
                
            };
        }
    }


}
