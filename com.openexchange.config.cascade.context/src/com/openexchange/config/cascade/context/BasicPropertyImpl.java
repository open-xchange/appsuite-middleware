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

package com.openexchange.config.cascade.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.ConvertUtils;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link BasicPropertyImplementation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class BasicPropertyImpl implements BasicProperty {

    private static final String DYNAMIC_ATTR_PREFIX = ContextConfigProvider.DYNAMIC_ATTR_PREFIX;

    private final ServiceLookup services;
    private boolean loaded;
    private String value;
    private final int contextId;
    private final String propertyName;
    private Map<String, String> metadata;

    /**
     * Initializes a new {@link BasicPropertyImplementation}.
     *
     * @param propertyName The property name
     * @param context The associated context
     * @param services The associated service look-up
     */
    BasicPropertyImpl(String propertyName, Context context, ServiceLookup services) {
        super();
        loaded = false;
        this.propertyName = propertyName;
        this.contextId = context.getContextId();
        this.services = services;
        forceLoad(context);
    }

    @Override
    public String get() throws OXException {
        load();
        return value;
    }

    @Override
    public String get(String metadataName) throws OXException {
        load();
        return null == metadataName ? null : metadata.get(metadataName);
    }

    @Override
    public boolean isDefined() throws OXException {
        load();
        return null != value;
    }

    @Override
    public void set(String newValue) throws OXException {
        load();
        if (Boolean.parseBoolean(metadata.get("protected"))) {
            throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(propertyName, ConfigViewScope.CONTEXT.getScopeName());
        }

        newValue = ConvertUtils.saveConvert(newValue, false, true);

        // Require service
        ContextService contextService = services.getOptionalService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        StringBuilder newValueBuilder = null;

        if (newValue != null) {
            // Compose the attribute string to set
            newValueBuilder = new StringBuilder(newValue.length() << 1).append(newValue.replace("%", "%25").replace(";", "%3B"));
            if (null == this.value) {
                // Newly set value
                newValueBuilder.append("; protected=false");
            } else {
                // Keep old meta-data
                int size = metadata.size();
                if (size > 0) {
                    if (1 == size) {
                        // Check if meta-data only contains "protected=true" (which is default)
                        if (false == Boolean.parseBoolean(metadata.get("protected"))) {
                            for (Map.Entry<String, String> metaEntry : metadata.entrySet()) {
                                newValueBuilder.append("; ").append(metaEntry.getKey()).append('=').append(metaEntry.getValue());
                            }
                        }
                    } else {
                        for (Map.Entry<String, String> metaEntry : metadata.entrySet()) {
                            newValueBuilder.append("; ").append(metaEntry.getKey()).append('=').append(metaEntry.getValue());
                        }
                    }
                }
            }
        }

        // Set and unload
        contextService.setAttribute(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(propertyName).toString(), newValueBuilder == null ? null : newValueBuilder.toString(), contextId);
        unload();
    }

    @Override
    public void set(String metadataName, String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, ConfigViewScope.CONTEXT.getScopeName());
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return new ArrayList<String>(metadata.keySet());
    }

    private void load() throws OXException {
        if (!loaded) {
            ContextService contextService = services.getOptionalService(ContextService.class);
            if (null == contextService) {
                throw ServiceExceptionCode.absentService(ContextService.class);
            }
            forceLoad(contextService.getContext(contextId));
        }
    }

    private void forceLoad(Context context) {
        List<String> values = context.getAttributes().get(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(propertyName).toString());
        if (values == null || values.isEmpty()) {
            // No such property
            this.value = null;
            metadata = Collections.emptyMap();
            loaded = true;
            return;
        }

        String value = values.get(0);
        int pos = value.indexOf(';');
        if (pos < 0) {
            value = Strings.replaceSequenceWith(value, "%3B", ";");
            value = Strings.replaceSequenceWith(value, "%25", "%");
            this.value = ConvertUtils.loadConvert(value);
            // Assume "protected=true" by default
            metadata = new HashMap<String, String>(2);
            metadata.put("protected", "true");
            loaded = true;
            return;
        }

        // Parameters available
        String params = value.substring(pos).trim();
        value = Strings.replaceSequenceWith(value.substring(0, pos).trim(), "%3B", ";");
        value = Strings.replaceSequenceWith(value, "%25", "%");
        this.value = ConvertUtils.loadConvert(value);
        metadata = new LinkedHashMap<String, String>(2);
        pos = 0;
        while (pos >= 0 && pos < params.length()) {
            int nextPos = params.indexOf(';', pos + 1);
            String param;
            if (nextPos > 0) {
                param = params.substring(pos+1, nextPos);
                pos = nextPos;
            } else {
                param = params.substring(pos+1);
                pos = -1;
            }
            int eq = param.indexOf('=');
            if (eq > 0) {
                String mName = param.substring(0, eq).trim();
                String mValue = param.substring(eq + 1).trim();
                metadata.put(mName, mValue);
            } else {
                metadata.put(param.trim(), "true");
            }
        }
        loaded = true;
    }

    private void unload() {
        if (loaded) {
            value = null;
            metadata = Collections.emptyMap();
            loaded = false;
        }
    }

}
