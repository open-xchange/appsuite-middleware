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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link ConfigCascade}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascade implements ConfigViewFactory {

    ConcurrentHashMap<String, ConfigProviderService> providers = new ConcurrentHashMap<String, ConfigProviderService>();

    String[] searchPath = null;

    private List<ConfigProviderService> path;

    StringParser stringParser;

    public void setProvider(String scope, ConfigProviderService configProvider) {
        providers.put(scope, configProvider);
    }

    public ConfigView getView(int user, int context) {
        return new View(user, context);
    }

    public ConfigView getView() {
        return new View(-1, -1);
    }

    public void setSearchPath(String... searchPath) {
        this.searchPath = searchPath;
        this.path = null;
    }
    
    public String[] getSearchPath() {
        return searchPath;
    }

    protected List<ConfigProviderService> getConfigProviders() {
        if (path != null) {
            return path;
        }

        List<ConfigProviderService> p = new ArrayList<ConfigProviderService>();
        for (String scope : searchPath) {
            p.add(providers.get(scope));
        }
        return path = p;
    }

    public void setStringParser(StringParser stringParser) {
        this.stringParser = stringParser;
    }

    private final class View implements ConfigView {

        int context;

        int user;

        public View(int user, int context) {
            this.user = user;
            this.context = context;
        }

        public <T> void set(String scope, String property, T value) throws ConfigCascadeException {
            ((ConfigProperty<T>) property(scope, property, value.getClass())).set(value);
        }

        public <T> T get(String property, Class<T> coerceTo) throws ConfigCascadeException {
            return property(property, coerceTo).get();
        }

        public <T> ConfigProperty<T> property(String scope, String property, Class<T> coerceTo) throws ConfigCascadeException {
            return new CoercingConfigProperty<T>(coerceTo, providers.get(scope).get(property, context, user), stringParser);
        }

        public <T> ComposedConfigProperty<T> property(final String property, Class<T> coerceTo) {
            return new CoercingComposedConfigProperty<T>(coerceTo, new ComposedConfigProperty<String>() {

                private String[] overriddenStrings;

                public String get() throws ConfigCascadeException {
                    String finalScope = getFinalScope();
                    for (ConfigProviderService provider : getConfigProviders(finalScope)) {
                        String value = provider.get(property, context, user).get();
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                private String getFinalScope() throws ConfigCascadeException {
                    return property(property, String.class).precedence("server", "context", "user").get("final");
                }

                public String get(String metadataName) throws ConfigCascadeException {
                    for (ConfigProviderService provider : getConfigProviders(null)) {
                        String value = provider.get(property, context, user).get(metadataName);
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                public <M> M get(String metadataName, Class<M> m) throws ConfigCascadeException {
                    for (ConfigProviderService provider : getConfigProviders(null)) {
                        String value = provider.get(property, context, user).get(metadataName);
                        if (value != null) {
                            M parsed = stringParser.parse(value, m);
                            if (parsed == null) {
                                throw ConfigCascadeExceptionCodes.COULD_NOT_COERCE_VALUE.create(value, m.getName());
                            }
                            return parsed;
                        }
                    }
                    return null;
                }
                
                public List<String> getMetadataNames() throws ConfigCascadeException {
                    Set<String> metadataNames = new HashSet<String>();
                    for (ConfigProviderService provider : getConfigProviders(null)) {
                        BasicProperty basicProperty = provider.get(property, context, user);
                        if(basicProperty != null) {
                            metadataNames.addAll(basicProperty.getMetadataNames());
                        }
                    }
                    return new ArrayList<String>(metadataNames);
                }


                public <M> ComposedConfigProperty<String> set(String metadataName, M value) {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                public ComposedConfigProperty<String> set(String value) {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                public ComposedConfigProperty<String> precedence(String... scopes) {
                    overriddenStrings = scopes;
                    return this;
                }

                private List<ConfigProviderService> getConfigProviders(String finalScope) {
                    String[] s = (overriddenStrings != null) ? overriddenStrings : searchPath;

                    List<ConfigProviderService> p = new ArrayList<ConfigProviderService>();
                    boolean collect = false;
                    for (String scope : s) {
                        collect = collect || finalScope == null || finalScope.equals(scope);

                        if (collect) {
                            p.add(providers.get(scope));
                        }
                    }
                    return p;
                }

                public boolean isDefined() throws ConfigCascadeException {
                    String finalScope = getFinalScope();
                    for (ConfigProviderService provider : getConfigProviders(finalScope)) {
                        boolean defined = provider.get(property, context, user).isDefined();
                        if (defined) {
                            return defined;
                        }
                    }
                    return false;
                }

                public <M> ComposedConfigProperty<M> to(Class<M> otherType) {
                    return new CoercingComposedConfigProperty<M>(otherType, this, stringParser);
                }


            }, stringParser);
        }

        public Map<String, ComposedConfigProperty<String>> all() throws ConfigCascadeException {
            Set<String> names = new HashSet<String>();
            for (ConfigProviderService provider : getConfigProviders()) {
                names.addAll(provider.getAllPropertyNames(context, user));
            }

            Map<String, ComposedConfigProperty<String>> properties = new HashMap<String, ComposedConfigProperty<String>>();
            for (String name : names) {
                properties.put(name, property(name, String.class));
            }

            return properties;
        }
    }

}
