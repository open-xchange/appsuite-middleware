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
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.service.ConfigurationTreeTest;
import com.openexchange.onboarding.service.ConfigurationTree;
import com.openexchange.session.Session;

/**
 * {@link ConfigurationTreeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfigurationTreeImpl implements ConfigurationTree {

    private final RootElem root;
    private final Session session;

    /**
     * Initializes a new {@link ConfigurationTreeTest}.
     *
     * @param configurations The configurations from which to build the tree
     * @param session The session to use
     * @throws OXException If building the tree fails
     */
    public ConfigurationTreeImpl(Collection<OnboardingConfiguration> configurations, Session session) throws OXException {
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

        void addToJsonObject(String id, JSONObject jObject, Session session) throws JSONException, OXException {
            if (null != value) {
                JSONObject jConfig = new JSONObject(6);
                jConfig.put("displayName", value.getDisplayName(session));
                jConfig.put("description", value.getDescription(session));
                jObject.put(value.getId(), jConfig);
            } else {
                JSONObject jChildren = new JSONObject(children.size());
                for (Map.Entry<String, NodeElem> entry : children.entrySet()) {
                    NodeElem childElem = entry.getValue();
                    childElem.addToJsonObject(entry.getKey(), jChildren, session);
                }

                JSONObject jEntity = new JSONObject(4);
                jEntity.put("displayName", entity.getDisplayName(session));
                jEntity.put("children", jChildren);

                jObject.put(id, jEntity);
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
            Platform platform = configuration.getPlatform();

            NodeElem nodeElem = elems.get(platform);
            if (null == nodeElem) {
                nodeElem = new NodeElem(platform);
                elems.put(platform, nodeElem);
            }

            List<Entity> entityPath = configuration.getEntityPath(session);
            for (Iterator<Entity> it = entityPath.iterator(); it.hasNext();) {
                Entity entity = it.next();
                NodeElem treeElem = nodeElem.children.get(entity.getId());
                if (null == treeElem) {
                    NodeElem ne = new NodeElem(entity);
                    nodeElem.children.put(entity.getId(), ne);
                    nodeElem = ne;
                } else {
                    if (treeElem.isLeaf()) {
                        NodeElem ne = new NodeElem(entity);
                        nodeElem.children.put(entity.getId(), ne);
                        ne.children.put(treeElem.value.getId(), treeElem);
                        nodeElem = ne;
                    } else {
                        nodeElem = treeElem;
                    }
                }
            }

            nodeElem.children.put(configuration.getId(), new NodeElem(configuration));
        }
    }

}
