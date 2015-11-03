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

package com.openexchange.onboarding.registry;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.Platform;
import com.openexchange.session.Session;

/**
 * {@link ConfigurationTree}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfigurationTree {

    private final NodeElem root;

    /**
     * Initializes a new {@link ConfigurationTree}.
     *
     * @param configurations The configurations from which to build the tree
     */
    public ConfigurationTree(Collection<OnboardingConfiguration> configurations, Session session) {
        super();
        root = new NodeElem();
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    public static class NodeElem {

        final Map<String, NodeElem> children;
        final OnboardingConfiguration value;

        NodeElem() {
            super();
            children = new HashMap<String, NodeElem>(4);
            value = null;
        }

        NodeElem(OnboardingConfiguration value) {
            super();
            children = null;
            this.value = value;
        }

        public boolean isLeaf() {
            return null != value;
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
                nodeElem = new NodeElem();
                elems.put(platform, nodeElem);
            }

            List<Entity> entityPath = configuration.getEntityPath(session);
            for (Iterator<Entity> it = entityPath.iterator(); it.hasNext();) {
                Entity entity = it.next();
                NodeElem treeElem = nodeElem.children.get(entity.getId());
                if (null == treeElem) {
                    NodeElem ne = new NodeElem();
                    nodeElem.children.put(entity.getId(), ne);
                    nodeElem = ne;
                } else {
                    if (treeElem.isLeaf()) {
                        NodeElem ne = new NodeElem();
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
