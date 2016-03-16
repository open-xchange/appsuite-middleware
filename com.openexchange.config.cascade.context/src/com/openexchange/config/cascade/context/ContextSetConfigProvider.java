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

package com.openexchange.config.cascade.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;
import com.openexchange.config.cascade.context.matching.ContextSetTerm;
import com.openexchange.config.cascade.context.matching.ContextSetTermParser;
import com.openexchange.config.cascade.context.matching.UserConfigurationAnalyzer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ContextSetConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContextSetConfigProvider extends AbstractContextBasedConfigProvider implements ReinitializableConfigProviderService {

    private static final String TAXONOMY_TYPES = "taxonomy/types";

    public static final String SCOPE = "contextSets";

    private static final String TYPE_PROPERTY = "com.openexchange.config.cascade.types";

    // -----------------------------------------------------------------------------------------------------

    private final Queue<ContextSetConfig> contextSetConfigs;
    private final Queue<AdditionalPredicates> additionalPredicates;
    private final UserConfigurationAnalyzer userConfigAnalyzer;

    /**
     * Initializes a new {@link ContextSetConfigProvider}.
     *
     * @param services The service look-up
     */
    public ContextSetConfigProvider(ServiceLookup services) {
        super(services);
        userConfigAnalyzer = new UserConfigurationAnalyzer();
        contextSetConfigs = new ConcurrentLinkedQueue<ContextSetConfig>();
        additionalPredicates = new ConcurrentLinkedQueue<AdditionalPredicates>();
        init();
    }

    private final void init() {
        ConfigurationService config = services.getService(ConfigurationService.class);
        Map<String, Object> yamlInFolder = config.getYamlInFolder("contextSets");
        if (yamlInFolder != null) {
            prepare(yamlInFolder);
        }
    }

    @Override
    public void reinit() throws OXException {
        contextSetConfigs.clear();
        additionalPredicates.clear();
        init();
    }

    protected Set<String> getSpecification(Context context, UserPermissionBits perms) throws OXException {
        // Gather available tags
        final Set<String> tags = new HashSet<String>(64);

        // Special tag that applies to the context
        tags.add(context.getName());
        tags.add(Integer.toString(context.getContextId()));

        // The ones from context attributes
        {
            List<String> typeValues = context.getAttributes().get(TAXONOMY_TYPES);
            if (typeValues == null) {
                typeValues = Collections.emptyList();
            }
            for (String string : typeValues) {
                tags.addAll(Arrays.asList(Strings.splitByComma(string)));
            }
        }

        // The ones from user configuration
        tags.addAll(userConfigAnalyzer.getTags(perms));

        // Now let's try modifications by cascade, first those below the contextSet level
        ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        final ConfigView view = configViews.getView(perms.getUserId(), context.getContextId());

        String[] searchPath = configViews.getSearchPath();
        for (String scope : searchPath) {
            if (!SCOPE.equals(scope)) {
                String types = view.property(TYPE_PROPERTY, String.class).precedence(scope).get();
                if (types != null) {
                    tags.addAll(Arrays.asList(Strings.splitByComma(types)));
                }
            }
        }

        // Add additional predicates. Do so until no modification has been done
        boolean goOn = true;
        while (goOn) {
            goOn = false;
            for (final AdditionalPredicates additional : additionalPredicates) {
                goOn = goOn || additional.apply(tags);
            }
        }

        return tags;
    }

    @Override
    protected BasicProperty get(final String property, Context context, int user) throws OXException {
        if (user == NO_USER) {
            return NO_PROPERTY;
        }
        List<Map<String, Object>> config = getConfigData(getSpecification(context, getUserPermissionBits(context, user)));

        final String value = findFirst(config, property);
        return new BasicProperty() {

            @Override
            public String get() {
                return value;
            }

            @Override
            public String get(final String metadataName) {
                return null;
            }

            @Override
            public boolean isDefined() {
                return value != null;
            }

            @Override
            public void set(String value) throws OXException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(property, SCOPE);
            }

            @Override
            public void set(String metadataName, String value) throws OXException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, SCOPE);
            }

            @Override
            public List<String> getMetadataNames() throws OXException {
                return Collections.emptyList();
            }

        };
    }

    private UserPermissionBits getUserPermissionBits(Context ctx, int user) throws OXException {
        UserPermissionService userPermissions = services.getService(UserPermissionService.class);
        return userPermissions.getUserPermissionBits(user, ctx);
    }

    @Override
    protected Collection<String> getAllPropertyNames(Context context) {
        return Collections.emptyList();
    }

    protected String findFirst(List<Map<String, Object>> configData, String property) {
        for (Map<String, Object> map : configData) {
            Object object = map.get(property);
            if (object != null) {
                return object.toString();
            }
        }
        return null;
    }

    protected List<Map<String, Object>> getConfigData(Set<String> tags) {
        List<Map<String, Object>> retval = new LinkedList<Map<String, Object>>();
        for (ContextSetConfig c : contextSetConfigs) {
            if (c.matches(tags)) {
                retval.add(c.getConfiguration());
            }
        }
        return retval;
    }

    protected void prepare(Map<String, Object> yamlFiles) {
        ContextSetTermParser parser = new ContextSetTermParser();
        for (Map.Entry<String, Object> file : yamlFiles.entrySet()) {
            String filename = file.getKey();

            try {
                @SuppressWarnings("unchecked")
                Map<Object, Map<String, Object>> content = (Map<Object, Map<String, Object>>) file.getValue();
                for (Map.Entry<Object, Map<String, Object>> configData : content.entrySet()) {
                    Object configName = configData.getKey();

                    // Check value validity
                    {
                        Object value = configData.getValue();
                        if (!(configData.getValue() instanceof Map)) {
                            throw new IllegalArgumentException("Invalid value. Expected " + Map.class.getName() + ", but was " + (null == value ? "null" : value.getClass().getName()) + ". Please check syntax of file " + filename);
                        }
                    }

                    Map<String, Object> configuration = configData.getValue();

                    Object withTags = configuration.get("withTags");
                    if (withTags == null) {
                        throw new IllegalArgumentException("Missing withTags specification in configuration " + configName + " in file " + filename);
                    }

                    try {
                        ContextSetTerm term = parser.parse(withTags.toString());
                        contextSetConfigs.add(new ContextSetConfig(term, configuration));
                        Object addTags = configuration.get("addTags");
                        if (addTags != null) {
                            final String additional = addTags.toString();
                            final List<String> additionalList = Arrays.asList(additional.split("\\s*,\\s*"));
                            additionalPredicates.add(new AdditionalPredicates(term, additionalList));
                        }
                    } catch (IllegalArgumentException x) {
                        throw new IllegalArgumentException("Could not parse withTags expression '" + withTags + "' in configuration " + configName + " in file " + filename, x);
                    }
                }
            } catch (IllegalArgumentException x) {
                throw x;
            } catch (RuntimeException x) {
                throw new IllegalArgumentException("Failed to process file " + filename + " due to error: " + x.getMessage(), x);
            }
        }
    }

    @Override
    public String getScope() {
        return "contextSets";
    }

    // -----------------------------------------------------------------------------------------------------------

    private static class ContextSetConfig {

        private final ContextSetTerm term;
        private final Map<String, Object> configuration;

        public ContextSetConfig(ContextSetTerm term, Map<String, Object> configuration) {
            super();
            this.term = term;
            this.configuration = configuration;
        }

        public boolean matches(Set<String> tags) {
            return term.matches(tags);
        }

        public Map<String, Object> getConfiguration() {
            return configuration;
        }

    }

    private static class AdditionalPredicates {

        private final ContextSetTerm term;
        private final List<String> additionalTags;

        public AdditionalPredicates(ContextSetTerm term, List<String> additionalTags) {
            super();
            this.term = term;
            this.additionalTags = additionalTags;
        }

        public boolean apply(Set<String> terms) {
            if (term.matches(terms)) {
                return terms.addAll(additionalTags);
            }
            return false;
        }

    }

}
