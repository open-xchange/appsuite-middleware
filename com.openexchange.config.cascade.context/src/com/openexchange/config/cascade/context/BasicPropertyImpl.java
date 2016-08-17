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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
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
    private final String property;
    private Map<String, String> metadata;

    /**
     * Initializes a new {@link BasicPropertyImplementation}.
     *
     * @param property The property name
     * @param context The associated context
     * @param services The associated service look-up
     */
    BasicPropertyImpl(String property, Context context, ServiceLookup services) {
        super();
        loaded = false;
        this.property = property;
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
            throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(property, "context");
        }
        if (Strings.isEmpty(newValue)) {
            throw ConfigCascadeExceptionCodes.UNEXPECTED_ERROR.create("New value is null");
        }

        // Require service
        ContextService contextService = services.getOptionalService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        // Compose the attribute string to set
        StringBuilder newValueBuilder = new StringBuilder(newValue.length() << 1).append(newValue.replace("%", "%25").replace(";", "%3B"));
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

        // Set and unload
        contextService.setAttribute(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString(), newValueBuilder.toString(), contextId);
        unload();
    }

    @Override
    public void set(String metadataName, String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, "context");
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
        boolean error = false;
        try {
            List<String> values = context.getAttributes().get(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString());
            if (values == null || values.isEmpty()) {
                // No such property
                this.value = null;
                metadata = Collections.emptyMap();
                return;
            }

            String value = values.get(0);
            int pos = value.indexOf(';');
            if (pos < 0) {
                this.value = value.replaceAll("%3B", ";").replace("%25", "%");
                // Assume "protected=true" by default
                metadata = new HashMap<String, String>(2);
                metadata.put("protected", "true");
                return;
            }

            // Parameters available
            String params = value.substring(pos).trim();
            this.value = value.substring(0, pos).trim().replaceAll("%3B", ";").replace("%25", "%");
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
        } catch (RuntimeException x) {
            error = true;
            throw x;
        } catch (Error x) {
            error = true;
            throw x;
        } finally {
            if (!error) {
                loaded = true;
            }
        }
    }

    private void unload() {
        if (loaded) {
            value = null;
            metadata = Collections.emptyMap();
            loaded = false;
        }
    }

}
