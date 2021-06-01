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
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link ConfigCascade}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigCascade implements ConfigViewFactory {

    private final ConcurrentMap<String, ConfigProviderService> providers;
    private final SearchPath searchPath;
    private final AtomicReference<StringParser> stringParserReference;

    /**
     * Initializes a new {@link ConfigCascade}.
     */
    public ConfigCascade() {
        super();
        ConcurrentMap<String, ConfigProviderService> providers = new ConcurrentHashMap<String, ConfigProviderService>(8, 0.9F, 1);
        this.providers = providers;
        searchPath = new SearchPath(providers);
        stringParserReference = new AtomicReference<StringParser>(null);
    }

    public void setProvider(String scope, ConfigProviderService configProvider) {
        providers.put(scope, configProvider);
    }

    @Override
    public ConfigView getView(int userId, int contextId) {
        int user = userId <= 0 ? -1 : userId;
        int context = contextId <= 0 ? -1 : contextId;
        return new View(user, context, providers, searchPath.getSearchPath(), getConfigProviders(), stringParserReference.get());
    }

    @Override
    public ConfigView getView() {
        return new View(-1, -1, providers, searchPath.getSearchPath(), getConfigProviders(), stringParserReference.get());
    }

    public void setSearchPath(String... searchPath) {
        this.searchPath.setSearchPath(searchPath);
    }

    @Override
    public String[] getSearchPath() {
        return searchPath.getSearchPath();
    }

    protected List<ConfigProviderService> getConfigProviders() {
        return searchPath.getConfigProviders();
    }

    public void setStringParser(StringParser stringParser) {
        stringParserReference.set(stringParser);
    }

    // ------------------------------------------------------------------------------------------

    private static final class View implements ConfigView {

        final int context;
        final int user;
        final String[] searchPath;
        final ConcurrentMap<String, ConfigProviderService> providers;
        final StringParser stringParser;
        private final List<ConfigProviderService> configProviders;

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
        public <T> void set(String scope, String propertyName, T value) throws OXException {
            ((ConfigProperty<T>) property(scope, propertyName, value.getClass())).set(value);
        }

        @Override
        public <T> T get(String propertyName, Class<T> coerceTo) throws OXException {
            return property(propertyName, coerceTo).get();
        }

        @Override
        public <T> T opt(String propertyName, java.lang.Class<T> coerceTo, T defaultValue) throws OXException {
            ComposedConfigProperty<T> p = property(propertyName, coerceTo);
            return p.isDefined() ? p.get() : defaultValue;
        }

        @Override
        public <T> ConfigProperty<T> property(String scope, String propertyName, Class<T> coerceTo) throws OXException {
            ConfigProviderService configProviderService = providers.get(scope);
            if (configProviderService == null) {
                // No such config provider for specified scope
                return new CoercingConfigProperty<T>(coerceTo, new NonExistentBasicProperty(propertyName, scope), stringParser);
            }
            return new CoercingConfigProperty<T>(coerceTo, configProviderService.get(propertyName, context, user), stringParser);
        }

        @Override
        public <T> ComposedConfigProperty<T> property(String propertyName, Class<T> coerceTo) {
            return new CoercingComposedConfigProperty<T>(coerceTo, new DefaultComposedConfigProperty(propertyName, this), stringParser);
        }

        @Override
        public Map<String, ComposedConfigProperty<String>> all() throws OXException {
            Set<String> names = new HashSet<String>();
            for (ConfigProviderService provider : configProviders) {
                names.addAll(provider.getAllPropertyNames(context, user));
            }

            Map<String, ComposedConfigProperty<String>> properties = new HashMap<String, ComposedConfigProperty<String>>();
            for (String name : names) {
                properties.put(name, property(name, String.class));
            }

            return properties;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class DefaultComposedConfigProperty implements ComposedConfigProperty<String> {

        private final AtomicReference<String[]> overriddenStrings;
        private final String property;
        private final View view;

        DefaultComposedConfigProperty(String property, View view) {
            super();
            this.property = property;
            this.view = view;
            overriddenStrings = new AtomicReference<String[]>(null);
        }

        @Override
        public String get() throws OXException {
            String finalScope = getFinalScope();
            for (ConfigProviderService provider : getConfigProviders(finalScope)) {
                String value = provider.get(property, view.context, view.user).get();
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        private String getFinalScope() throws OXException {
            return view.property(property, String.class).precedence(ConfigViewScope.SERVER, ConfigViewScope.RESELLER, ConfigViewScope.CONTEXT, ConfigViewScope.USER).get("final");
        }

        @Override
        public String get(String metadataName) throws OXException {
            for (ConfigProviderService provider : getConfigProviders(null)) {
                String value = provider.get(property, view.context, view.user).get(metadataName);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public String getScope() throws OXException {
            String finalScope = getFinalScope();
            for (ConfigProviderService provider : getConfigProviders(finalScope)) {
                String value = provider.get(property, view.context, view.user).get();
                if (value != null) {
                    return provider.getScope();
                }
            }
            return null;
        }

        @Override
        public <M> M get(String metadataName, Class<M> m) throws OXException {
            for (ConfigProviderService provider : getConfigProviders(null)) {
                String value = provider.get(property, view.context, view.user).get(metadataName);
                if (value != null) {
                    M parsed = view.stringParser.parse(value, m);
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
            Set<String> metadataNames = new HashSet<String>();
            for (ConfigProviderService provider : getConfigProviders(null)) {
                BasicProperty basicProperty = provider.get(property, view.context, view.user);
                if (basicProperty != null) {
                    metadataNames.addAll(basicProperty.getMetadataNames());
                }
            }
            return new ArrayList<String>(metadataNames);
        }

        @Override
        public <M> ComposedConfigProperty<String> set(String metadataName, M value) throws OXException {
            throw new UnsupportedOperationException("Unscoped set is not supported");
        }

        @Override
        public ComposedConfigProperty<String> set(String value) throws OXException {
            throw new UnsupportedOperationException("Unscoped set is not supported");
        }

        @Override
        public ComposedConfigProperty<String> precedence(ConfigViewScope... scopes) throws OXException {
            String[] scopez = new String[scopes.length];
            for (int i = scopez.length; i-- > 0;) {
                scopez[i] = scopes[i].getScopeName();
            }
            overriddenStrings.set(scopez);
            return this;
        }

        @Override
        public ComposedConfigProperty<String> precedence(String... scopes) throws OXException {
            overriddenStrings.set(scopes);
            return this;
        }

        private List<ConfigProviderService> getConfigProviders(String finalScope) {
            String[] overriddenStrings = this.overriddenStrings.get();
            String[] s = (overriddenStrings == null) ? view.searchPath : overriddenStrings;

            List<ConfigProviderService> p = new ArrayList<ConfigProviderService>(s.length);
            boolean collect = false;
            for (String scope : s) {
                collect = collect || finalScope == null || finalScope.equals(scope);

                if (collect) {
                    ConfigProviderService providerService = view.providers.get(scope);
                    if (providerService != null) {
                        p.add(providerService);
                    }
                }
            }
            return p;
        }

        @Override
        public boolean isDefined() throws OXException {
            String finalScope = getFinalScope();
            for (ConfigProviderService provider : getConfigProviders(finalScope)) {
                boolean defined = provider.get(property, view.context,view.user).isDefined();
                if (defined) {
                    return defined;
                }
            }
            return false;
        }

        @Override
        public <M> ComposedConfigProperty<M> to(Class<M> otherType) throws OXException {
            return new CoercingComposedConfigProperty<M>(otherType, this, view.stringParser);
        }
    }

    private static class NonExistentBasicProperty implements BasicProperty {

        private final String property;
        private final String scope;

        NonExistentBasicProperty(String property, String scope) {
            super();
            this.property = property;
            this.scope = scope;
        }

        @Override
        public void set(String metadataName, String value) throws OXException {
            throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, scope);
        }

        @Override
        public void set(String value) throws OXException {
            throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(property, scope);
        }

        @Override
        public boolean isDefined() throws OXException {
            return false;
        }

        @Override
        public List<String> getMetadataNames() throws OXException {
            return Collections.emptyList();
        }

        @Override
        public String get(String metadataName) throws OXException {
            return null;
        }

        @Override
        public String get() throws OXException {
            return null;
        }
    }

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

}
