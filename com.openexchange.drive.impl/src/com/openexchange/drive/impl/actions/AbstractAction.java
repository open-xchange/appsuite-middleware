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

package com.openexchange.drive.impl.actions;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

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
        return getAction() + " [version=" + version + ", newVersion=" + newVersion + ", parameters=" + parameters + "]";
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

}

