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

package com.openexchange.subscribe;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.generic.SessionAwareTargetFolderDefinition;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Subscription extends TargetFolderDefinition implements SessionAwareTargetFolderDefinition {

    private int id;

    private long lastUpdate;
    
    private long created;

    private SubscriptionSource source;

    private Map<String, Object> configuration = new HashMap<String, Object>();

    private String displayName;

    private Boolean enabled;

    private String secret;

    private ServerSession session;

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean containsLastUpdate() {
        return getLastUpdate() > 0;
    }
    
    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public boolean containsCreated() {
        return getCreated() > 0;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public void setSource(SubscriptionSource source) {
        this.source = source;
    }

    public SubscriptionSource getSource() {
        return source;
    }

    public boolean containsSource() {
        return getSource() != null;
    }

    public DynamicFormDescription getDescription() {
        return source.getFormDescription();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled == null ? true : enabled.booleanValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = Boolean.valueOf(enabled);
    }

    public boolean containsEnabled() {
        return enabled != null;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setSession(ServerSession session) {
        this.session = session;
    }

    @Override
    public ServerSession getSession() {
        if (session != null) {
            return session;
        }
        try {
            return ServerSessionAdapter.valueOf(new TargetFolderSession(this));
        } catch (OXException e) {
            return null;
        }
    }




}
