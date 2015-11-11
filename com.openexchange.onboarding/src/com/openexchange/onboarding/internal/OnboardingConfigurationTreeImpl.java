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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.internal;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.onboarding.OnboardingConfigurationTree;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.service.OnboardingConfigurationTreeTest;
import com.openexchange.session.Session;

/**
 * {@link OnboardingConfigurationTreeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingConfigurationTreeImpl implements OnboardingConfigurationTree {

    private final RootElem root;
    private final Session session;

    /**
     * Initializes a new {@link OnboardingConfigurationTreeTest}.
     *
     * @param configurations The configurations from which to build the tree
     * @param session The session to use
     * @throws OXException If building the tree fails
     */
    public OnboardingConfigurationTreeImpl(Collection<OnboardingConfiguration> configurations, Session session) throws OXException {
        super();
        this.session = session;
        root = new RootElem();
        for (OnboardingConfiguration configuration : configurations) {
            root.add(configuration, session);
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException, OXException {
        JSONObject jObject = new JSONObject(root.elems.size());
        for (Map.Entry<Platform, NodeElem> entry : root.elems.entrySet()) {
            NodeElem nodeElem = entry.getValue();
            nodeElem.addToJsonObject(entry.getKey().getId(), jObject, session);
        }
        return jObject;
    }

    @Override
    public String toString() {
        try {
            return toJsonObject().toString();
        } catch (Exception e) {
            return "toString() failed: " + e.getMessage();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static class NodeElem {

        final Map<String, NodeElem> children;
        final Entity entity;
        final OnboardingConfiguration value;

        NodeElem(Entity entity) {
            super();
            children = new HashMap<String, NodeElem>(4);
            this.entity = entity;
            value = null;
        }

        NodeElem(OnboardingConfiguration value) {
            super();
            children = null;
            entity = null;
            this.value = value;
        }

        boolean isLeaf() {
            return null != value;
        }

        void addToJsonObject(String entityId, JSONObject jObject, Session session) throws JSONException, OXException {
            if (null != value) {
                JSONObject jConfig = new JSONObject(6);
                put2Json("displayName", value.getDisplayName(session), jConfig);
                put2Json("description", value.getDescription(session), jConfig);
                put2Json("icon", value.getIcon(session), jConfig);
                jConfig.put("id", value.getId());
                jConfig.put("entityId", entityId);
                jObject.put(entityId, jConfig);
            } else {
                JSONObject jChildren = new JSONObject(children.size());
                for (Map.Entry<String, NodeElem> entry : children.entrySet()) {
                    NodeElem childElem = entry.getValue();
                    childElem.addToJsonObject(entry.getKey(), jChildren, session);
                }

                JSONObject jEntity = new JSONObject(4);
                put2Json("displayName", entity.getDisplayName(session), jEntity);
                put2Json("description", entity.getDescription(session), jEntity);
                put2Json("icon", entity.getIcon(session), jEntity);
                jEntity.put("children", jChildren);

                jObject.put(entityId, jEntity);
            }
        }

        private static void put2Json(String key, Object value, JSONObject jObject) throws JSONException {
            if (null == value) {
                jObject.put(key, JSONObject.NULL);
            } else {
                if (value instanceof Icon) {
                    jObject.put(key, Charsets.toAsciiString(Base64.encodeBase64(((Icon) value).getData(), false)));
                } else {
                    jObject.put(key, value);
                }
            }
        }
    }

    private static class RootElem {

        final Map<Platform, NodeElem> elems;

        RootElem() {
            super();
            elems = new EnumMap<Platform, NodeElem>(Platform.class);
        }

        void add(OnboardingConfiguration configuration, Session session) throws OXException {
            for (EntityPath entityPath : configuration.getEntityPaths(session)) {
                Platform platform = entityPath.getPlatform();

                NodeElem prevElem = elems.get(platform);
                if (null == prevElem) {
                    prevElem = new NodeElem(platform);
                    elems.put(platform, prevElem);
                }

                boolean leaf = false;
                for (Iterator<Entity> it = entityPath.iterator(); !leaf;) {
                    Entity entity = it.next();

                    leaf = !it.hasNext();
                    if (leaf) {
                        prevElem.children.put(entity.getId(), new NodeElem(configuration));
                    } else {
                        NodeElem nextElem = prevElem.children.get(entity.getId());
                        if (null == nextElem) {
                            NodeElem ne = new NodeElem(entity);
                            prevElem.children.put(entity.getId(), ne);
                            prevElem = ne;
                        } else {
                            prevElem = nextElem;
                        }
                    }
                }
            }
        }
    }

}
