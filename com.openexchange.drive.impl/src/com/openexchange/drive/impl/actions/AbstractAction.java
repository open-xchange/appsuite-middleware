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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.Tracer;

/**
 * {@link AbstractAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractAction<T extends DriveVersion> implements DriveAction<T> {

    protected final T version;
    protected final T newVersion;
    protected final Map<String, Object> parameters;

    protected ThreeWayComparison<T> comparison;
    protected T resultingVersion;

    /**
     * Initializes a new {@link AbstractAction}.
     *
     * @param version
     * @param newVersion
     */
    protected AbstractAction(T version, T newVersion, ThreeWayComparison<T> comparison) {
        super();
        this.comparison = comparison;
        this.version = version;
        this.newVersion = newVersion;
        this.parameters = new HashMap<String, Object>();
    }

    @Override
    public T getVersion() {
        return version;
    }

    @Override
    public T getNewVersion() {
        return newVersion;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public int compareTo(DriveAction<T> other) {
        Action thisAction = this.getAction();
        Action otherAction = null != other ? other.getAction() : null;
        if (null == otherAction) {
            return null == thisAction ? 0 : -1;
        }
        return thisAction.compareTo(otherAction);
    }

    @Override
    public String toString() {
        return getAction() + " [version=" + version + ", newVersion=" + newVersion + ", parameters=" + printParameters(parameters) + "]";
    }

    public boolean wasCausedBy(Change clientChange, Change serverChange) {
        if (null == this.comparison) {
            throw new UnsupportedOperationException("no comparison available");
        }
        return comparison.getClientChange().equals(clientChange) && comparison.getServerChange().equals(serverChange);
    }

    /**
     * Gets the comparison
     *
     * @return The comparison
     */
    public ThreeWayComparison<T> getComparison() {
        return comparison;
    }

    /**
     * Gets the resulting version after this action was executed, if applicable.
     *
     * @return The resulting version, or <code>null</code> if not specified
     */
    public T getResultingVersion() {
        return resultingVersion;
    }

    /**
     * Sets a version representing the result of this action.
     *
     * @param version The resulting version to set
     */
    public void setResultingVersion(T version) {
        this.resultingVersion = version;
    }

    private static String printParameters(Map<String, Object> parameters) {
        int maxLength = (int) (Tracer.MAX_SIZE * 0.9);
        StringBuilder stringBuilder = new StringBuilder().append('{');
        for (Entry<String, Object> entry : parameters.entrySet()) {
            if (PARAMETER_NAMES.contains(entry.getKey())) {
                if (1 < stringBuilder.length()) {
                    stringBuilder.append(',').append(' ');
                    if (maxLength < stringBuilder.length()) {
                        stringBuilder.append("...");
                        break;
                    }
                }
                stringBuilder.append(entry.getKey()).append('=');
                if (PARAMETER_DATA.equals(entry.getKey())) {
                    stringBuilder.append("...");
                } else {
                    stringBuilder.append(entry.getValue());
                }
            }
        }
        return stringBuilder.append('}').toString();
    }
}

