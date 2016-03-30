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

package com.openexchange.drive.impl.sync.optimize;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.VersionMapper;

/**
 * {@link AbstractActionOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractActionOptimizer<T extends DriveVersion> implements ActionOptimizer<T> {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractActionOptimizer.class);

    protected final VersionMapper<T> mapper;

    /**
     * Initializes a new {@link AbstractActionOptimizer}.
     *
     * @param mapper The version mapper
     */
    public AbstractActionOptimizer(VersionMapper<T> mapper) {
        super();
        this.mapper = mapper;
    }

    protected static <T extends DriveVersion> boolean matchesByChecksum(T v1, T v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return null == v1;
        } else {
            return null == v1.getChecksum() ? null == v2.getChecksum() : v1.getChecksum().equals(v2.getChecksum());
        }
    }

    protected static <T extends DriveVersion> List<AbstractAction<T>> filterByAction(List<AbstractAction<T>> driveActions, Action action) {
        List<AbstractAction<T>> filteredActions = new ArrayList<AbstractAction<T>>();
        for (AbstractAction<T> driveAction : driveActions) {
            if (null != driveAction && action.equals(driveAction.getAction())) {
                filteredActions.add(driveAction);
            }
        }
        return filteredActions;
    }

}
