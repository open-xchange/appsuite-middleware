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

package com.openexchange.config.cascade.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link ConfigCascade}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascade implements ConfigViewFactory {

    private static final class SearchPath {

        private final ConcurrentMap<String, ConfigProviderService> providers;
        private final AtomicReference<String[]> searchPath;
        private final AtomicReference<List<ConfigProviderService>> path;

        SearchPath(ConcurrentMap<String, ConfigProviderService> providers) {
            super();
            this.providers = providers;
            searchPath = new AtomicReference<String[]>(null);
            List<ConfigProviderService> path = Collections.emptyList();
            this.path = new AtomicReference<List<ConfigProviderService>>(path);
        }

        void setSearchPath(String... searchPath) {
            this.searchPath.set(searchPath);
            this.path.set(null); // Enforce re-initialization
        }

        String[] getSearchPath() {
            return searchPath.get();
        }

        List<ConfigProviderService> getConfigProviders() {
            List<ConfigProviderService> path = this.path.get();
            if (null == path) {
                synchronized (this) {
                    path = this.path.get();
                    if (null == path) {
                        path = computePathFrom(searchPath.get(), providers);
                        this.path.set(path);
                    }
                }
            }
            return path;
        }

        private List<ConfigProviderService> computePathFrom(String[] searchPath, ConcurrentMap<String, ConfigProviderService> providers) {
            if (null == searchPath) {
                return Collections.emptyList();
            }

            ImmutableList.Builder<ConfigProviderService> p = ImmutableList.builder();
            for (String scope : searchPath) {
                ConfigProviderService configProvider = providers.get(scope);
                if (null == configProvider) {
                    throw new IllegalStateException("No such config provider for scope: " + scope);
                }
                p.add(configProvider);
            }
            return p.build();
        }
    }

    // -----------------------------------------------------------------------------------------

    final ConcurrentMap<String, ConfigProviderService> providers;
    final SearchPath searchPath;
    final AtomicReference<StringParser> stringParser;

    /**
     * Initializes a new {@link ConfigCascade}.
     */
    public ConfigCascade() {
        super();
        ConcurrentMap<String, ConfigProviderService> providers = new ConcurrentHashMap<String, ConfigProviderService>(8, 0.9F, 1);
        this.providers = providers;
        searchPath = new SearchPath(providers);
        stringParser = new AtomicReference<StringParser>(null);
    }

    public void setProvider(final String scope, final ConfigProviderService configProvider) {
        providers.put(scope, configProvider);
    }

    @Override
    public ConfigView getView(int userId, int contextId) {
        int user = userId <= 0 ? -1 : userId;
        int context = contextId <= 0 ? -1 : contextId;
        return new View(user, context, providers, searchPath.getSearchPath(), getConfigProviders(), stringParser.get());
    }

    @Override
    public ConfigView getView() {
        return new View(-1, -1, providers, searchPath.getSearchPath(), getConfigProviders(), stringParser.get());
    }

    public void setSearchPath(final String... searchPath) {
        this.searchPath.setSearchPath(searchPath);
    }

    @Override
    public String[] getSearchPath() {
        return searchPath.getSearchPath();
    }

    protected List<ConfigProviderService> getConfigProviders() {
        return searchPath.getConfigProviders();
    }

    public void setStringParser(final StringParser stringParser) {
        this.stringParser.set(stringParser);
    }

    // ------------------------------------------------------------------------------------------

    private static final class View implements ConfigView {

        final int context;
        final int user;
        final String[] searchPath;
        final ConcurrentMap<String, ConfigProviderService> providers;
        final StringParser stringParser;
        final List<ConfigProviderService> configProviders;

        View(int user, int context, ConcurrentMap<String, ConfigProviderService> providers, String[] searchPath, List<ConfigProviderService> configProviders, StringParser stringParser) {
            super();
            this.user = user;
            this.context = context;
            this.providers = providers;
            this.searchPath = searchPath;
            this.configProviders = configProviders;
            this.stringParser = stringParser;
        }

        @Override
        public <T> void set(final String scope, final String property, final T value) throws OXException {
            ((ConfigProperty<T>) property(scope, property, value.getClass())).set(value);
        }

        @Override
        public <T> T get(final String property, final Class<T> coerceTo) throws OXException {
            return property(property, coerceTo).get();
        }

        @Override
        public <T> T opt(final String property, final java.lang.Class<T> coerceTo, final T defaultValue) throws OXException {
            final ComposedConfigProperty<T> p = property(property, coerceTo);
            return p.isDefined() ? p.get() : defaultValue;
        }

        @Override
        public <T> ConfigProperty<T> property(final String scope, final String property, final Class<T> coerceTo) throws OXException {
            return new CoercingConfigProperty<T>(coerceTo, providers.get(scope).get(property, context, user), stringParser);
        }

        @Override
        public <T> ComposedConfigProperty<T> property(final String property, final Class<T> coerceTo) {
            return new CoercingComposedConfigProperty<T>(coerceTo, new ComposedConfigProperty<String>() {

                private final AtomicReference<String[]> overriddenStrings = new AtomicReference<String[]>(null);

                @Override
                public String get() throws OXException {
                    final String finalScope = getFinalScope();
                    for (final ConfigProviderService provider : getConfigProviders(finalScope)) {
                        final String value = provider.get(property, context, user).get();
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                private String getFinalScope() throws OXException {
                    return property(property, String.class).precedence("server", "context", "user").get("final");
                }

                @Override
                public String get(final String metadataName) throws OXException {
                    for (final ConfigProviderService provider : getConfigProviders(null)) {
                        final String value = provider.get(property, context, user).get(metadataName);
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }

                @Override
                public String getScope() throws OXException {
                	final String finalScope = getFinalScope();
                    for (final ConfigProviderService provider : getConfigProviders(finalScope)) {
                        final String value = provider.get(property, context, user).get();
                        if (value != null) {
                            return provider.getScope();
                        }
                    }
                    return null;
                }

                @Override
                public <M> M get(final String metadataName, final Class<M> m) throws OXException {
                    for (final ConfigProviderService provider : getConfigProviders(null)) {
                        final String value = provider.get(property, context, user).get(metadataName);
                        if (value != null) {
                            final M parsed = stringParser.parse(value, m);
                            if (parsed == null) {
                                throw ConfigCascadeExceptionCodes.COULD_NOT_COERCE_VALUE.create(value, m.getName());
                            }
                            return parsed;
                        }
                    }
                    return null;
                }

                @Override
                public List<String> getMetadataNames() throws OXException {
                    final Set<String> metadataNames = new HashSet<String>();
                    for (final ConfigProviderService provider : getConfigProviders(null)) {
                        final BasicProperty basicProperty = provider.get(property, context, user);
                        if(basicProperty != null) {
                            metadataNames.addAll(basicProperty.getMetadataNames());
                        }
                    }
                    return new ArrayList<String>(metadataNames);
                }


                @Override
                public <M> ComposedConfigProperty<String> set(final String metadataName, final M value) throws OXException {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                @Override
                public ComposedConfigProperty<String> set(final String value) throws OXException {
                    throw new UnsupportedOperationException("Unscoped set is not supported");
                }

                @Override
                public ComposedConfigProperty<String> precedence(final String... scopes) throws OXException {
                    overriddenStrings.set(scopes);
                    return this;
                }

                private List<ConfigProviderService> getConfigProviders(final String finalScope) {
                    String[] overriddenStrings = this.overriddenStrings.get();
                    final String[] s = (overriddenStrings == null) ? searchPath : overriddenStrings;

                    final List<ConfigProviderService> p = new ArrayList<ConfigProviderService>();
                    boolean collect = false;
                    for (final String scope : s) {
                        collect = collect || finalScope == null || finalScope.equals(scope);

                        if (collect) {
                            p.add(providers.get(scope));
                        }
                    }
                    return p;
                }

                @Override
                public boolean isDefined() throws OXException {
                    final String finalScope = getFinalScope();
                    for (final ConfigProviderService provider : getConfigProviders(finalScope)) {
                        final boolean defined = provider.get(property, context, user).isDefined();
                        if (defined) {
                            return defined;
                        }
                    }
                    return false;
                }

                @Override
                public <M> ComposedConfigProperty<M> to(final Class<M> otherType) throws OXException {
                    return new CoercingComposedConfigProperty<M>(otherType, this, stringParser);
                }


            }, stringParser);
        }

        @Override
        public Map<String, ComposedConfigProperty<String>> all() throws OXException {
            final Set<String> names = new HashSet<String>();
            for (final ConfigProviderService provider : configProviders) {
                names.addAll(provider.getAllPropertyNames(context, user));
            }

            final Map<String, ComposedConfigProperty<String>> properties = new HashMap<String, ComposedConfigProperty<String>>();
            for (final String name : names) {
                properties.put(name, property(name, String.class));
            }

            return properties;
        }
    }

}
