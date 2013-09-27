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

package com.openexchange.config.cascade.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.context.matching.ContextSetTerm;
import com.openexchange.config.cascade.context.matching.ContextSetTermParser;
import com.openexchange.config.cascade.context.matching.UserConfigurationAnalyzer;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link ContextSetConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextSetConfigProvider extends AbstractContextBasedConfigProvider {

    private static final String TAXONOMY_TYPES = "taxonomy/types";

    public static final String SCOPE = "contextSets";

    private static final String TYPE_PROPERTY = "com.openexchange.config.cascade.types";

    private final List<ContextSetConfig> contextSetConfigs;
    private final List<AdditionalPredicates> additionalPredicates;

    private final UserPermissionService userPermissions;
    private final UserConfigurationAnalyzer userConfigAnalyzer = new UserConfigurationAnalyzer();

    private final ConfigViewFactory configViews;

    public ContextSetConfigProvider(final ContextService contexts, final ConfigurationService config, final UserPermissionService userPermissions, final ConfigViewFactory configViews) {
        super(contexts);

        final Map<String, Object> yamlInFolder = config.getYamlInFolder("contextSets");
        if (yamlInFolder == null) {
            contextSetConfigs = Collections.emptyList();
            additionalPredicates = Collections.emptyList();
        } else {
            contextSetConfigs = new LinkedList<ContextSetConfig>();
            additionalPredicates = new LinkedList<AdditionalPredicates>();
            prepare(yamlInFolder);
        }

        this.userPermissions = userPermissions;
        this.configViews = configViews;
    }

    protected Set<String> getSpecification(final Context context, final UserPermissionBits perms) throws OXException {
        Set<String> typeValues = context.getAttributes().get(TAXONOMY_TYPES);
        if (typeValues == null) {
            typeValues = Collections.emptySet();
        }
        // Gather available tags
        final Set<String> tags = new HashSet<String>(64);

        // Special tag that applies to the context
        tags.add(context.getName());
        tags.add(Integer.toString(context.getContextId()));

        // The ones from context attributes
        for (final String string : typeValues) {
            tags.addAll(Arrays.asList(Strings.splitByComma(string)));
        }

        // The ones from user configuration
        tags.addAll(userConfigAnalyzer.getTags(perms));

        // Now let's try modifications by cascade, first those below the contextSet level
        final ConfigView view = configViews.getView(perms.getUserId(), context.getContextId());

        final String[] searchPath = configViews.getSearchPath();
        for (final String scope : searchPath) {
            if (!scope.equals(SCOPE)) {
                final String types = view.property(TYPE_PROPERTY, String.class).precedence(scope).get();
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
    protected BasicProperty get(final String property, final Context context, final int user) throws OXException {
        final List<Map<String, Object>> config = getConfigData(getSpecification(context, getUserPermissionBits(context, user)));

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
            public void set(final String value) throws OXException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(property, SCOPE);
            }

            @Override
            public void set(final String metadataName, final String value) throws OXException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, SCOPE);
            }

            @Override
            public List<String> getMetadataNames() throws OXException {
                return Collections.emptyList();
            }

        };
    }


    private UserPermissionBits getUserPermissionBits(final Context ctx, final int user) throws OXException {
        return userPermissions.getUserPermissionBits(user, ctx);
    }

    @Override
    protected Collection<String> getAllPropertyNames(final Context context) {
        return Collections.emptyList();
    }

    protected String findFirst(final List<Map<String, Object>> configData, final String property) {
        for (final Map<String, Object> map : configData) {
            final Object object = map.get(property);
            if(object != null) {
                return object.toString();
            }
        }
        return null;
    }



    protected List<Map<String, Object>> getConfigData(final Set<String> tags) {
        final List<Map<String, Object>> retval = new LinkedList<Map<String, Object>>();
        for (final ContextSetConfig c : contextSetConfigs) {
            if (c.matches(tags)) {
                retval.add(c.getConfiguration());
            }
        }
        return retval;
    }

    protected void prepare(final Map<String, Object> yamlFiles) {
        final ContextSetTermParser parser = new ContextSetTermParser();
        for(final Map.Entry<String, Object> file : yamlFiles.entrySet()) {
            final String filename = file.getKey();
            final Map<Object, Map<String, Object>> content = (Map<Object, Map<String, Object>>) file.getValue();
            for(final Map.Entry<Object, Map<String, Object>> configData : content.entrySet()) {
                final Object configName = configData.getKey();
                final Map<String, Object> configuration = configData.getValue();

                final Object withTags = configuration.get("withTags");
                if(withTags == null) {
                    throw new IllegalArgumentException("Missing withTags specification in configuration "+configName+" in file "+filename);
                }
                try {
                    final ContextSetTerm term = parser.parse(withTags.toString());
                    contextSetConfigs.add(new ContextSetConfig(term, configuration));
                    final Object addTags = configuration.get("addTags");
                    if(addTags != null) {
                        final String additional = addTags.toString();
                        final List<String> additionalList = Arrays.asList(additional.split("\\s*,\\s*"));
                        additionalPredicates.add(new AdditionalPredicates(term, additionalList));
                    }
                } catch (final IllegalArgumentException x) {
                    throw new IllegalArgumentException("Could not parse withTags expression '"+withTags+"' in configuration "+configName+" in file "+filename+": "+x.getMessage());
                }
            }

        }
    }

    private class ContextSetConfig {
        private final ContextSetTerm term;
        private final Map<String, Object> configuration;

        public ContextSetConfig(final ContextSetTerm term, final Map<String, Object> configuration) {
            super();
            this.term = term;
            this.configuration = configuration;
        }

        public boolean matches(final Set<String> tags) {
            return term.matches(tags);
        }

        public Map<String, Object> getConfiguration() {
            return configuration;
        }

    }

    private class AdditionalPredicates {
        private final ContextSetTerm term;
        private final List<String> additionalTags;

        public AdditionalPredicates(final ContextSetTerm term, final List<String> additionalTags) {
            super();
            this.term = term;
            this.additionalTags = additionalTags;
        }

        public boolean apply(final Set<String> terms) {
            if(term.matches(terms)) {
                return terms.addAll(additionalTags);
            }
            return false;
        }

    }

}
