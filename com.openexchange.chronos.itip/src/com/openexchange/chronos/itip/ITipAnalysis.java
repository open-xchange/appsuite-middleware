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

package com.openexchange.chronos.itip;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * {@link ITipAnalysis}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipAnalysis {

    private final List<ITipChange> changes = new ArrayList<ITipChange>();
    private final List<ITipAnnotation> annotations = new ArrayList<ITipAnnotation>();
    private final Set<ITipAction> actions = EnumSet.noneOf(ITipAction.class);
    private ITipMessage message = null;
    private String uid;
    private Map<String, Object> attributes = new HashMap<String, Object>();

    public ITipMessage getMessage() {
        return message;
    }

    public void setMessage(ITipMessage message) {
        this.message = message;
    }

    public List<ITipChange> getChanges() {
        return changes;
    }

    public void addChange(ITipChange change) {
        if (null != change) {
            changes.add(change);
        }
    }

    public List<ITipAnnotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(ITipAnnotation annotation) {
        annotations.add(annotation);
    }

    public Set<ITipAction> getActions() {
        return actions;
    }

    public void recommendAction(ITipAction action) {
        actions.add(action);
    }

    public void recommendActions(ITipAction... actions) {
        for (ITipAction action : actions) {
            this.actions.add(action);
        }
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
