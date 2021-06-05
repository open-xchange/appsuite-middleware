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

package com.openexchange.drive.impl.actions;

import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

/**
 * {@link AbstractDirectoryAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractDirectoryAction extends AbstractAction<DirectoryVersion> {

    private final int hashCode;
    private final Action action;

    /**
     * Initializes a new {@link AbstractDirectoryAction}.
     *
     * @param action The action
     * @param version The directory version
     * @param newVersion The new directory version
     * @param comparison The underlying comparison
     */
    protected AbstractDirectoryAction(Action action, DirectoryVersion version, DirectoryVersion newVersion, ThreeWayComparison<DirectoryVersion> comparison) {
        super(version, newVersion, comparison);
        this.action = action;
        this.hashCode = calculateHash();
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractDirectoryAction)) {
            return false;
        }
        AbstractDirectoryAction other = (AbstractDirectoryAction) obj;
        if (newVersion == null) {
            if (other.newVersion != null) {
                return false;
            }
        } else if (!newVersion.equals(other.newVersion)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    private int calculateHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + DirectoryVersion.class.hashCode();
        result = prime * result + ((null == action) ? 0 : action.hashCode());
        if (null != version) {
            result = prime * result + version.getChecksum().hashCode();
            result = prime * result + version.getPath().hashCode();
        }
        if (null != newVersion) {
            result = prime * result + newVersion.getChecksum().hashCode();
            result = prime * result + newVersion.getPath().hashCode();
        }
        result = prime * result + ((null == parameters) ? 0 : parameters.hashCode());
        return result;
    }

}

