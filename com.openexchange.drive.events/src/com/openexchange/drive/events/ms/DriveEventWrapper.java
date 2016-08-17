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

package com.openexchange.drive.events.ms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.internal.DriveEventImpl;

/**
 * {@link DriveEventWrapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventWrapper {

    /**
     * Wraps the supplied drive event into a pojo map.
     *
     * @param driveEvent The drive event to wrap
     * @return The wrapped drive event
     */
    public static Map<String, Serializable> wrap(DriveEvent driveEvent) {
        if (null == driveEvent) {
            return null;
        }
        Map<String, Serializable> map = new LinkedHashMap<String, Serializable>(2);
        map.put("__contextID", Integer.valueOf(driveEvent.getContextID()));
        Set<String> folderIDs = driveEvent.getFolderIDs();
        if (null != folderIDs) {
            map.put("__folderIDs", driveEvent.getFolderIDs().toArray(new String[folderIDs.size()]));
        }
        String pushToken = driveEvent.getPushTokenReference();
        if (null != pushToken) {
            map.put("__pushToken", pushToken);
        }
        return map;
    }

    /**
     * Unwraps a drive event from the supplied pojo map. The <code>remote</code> flag in the event is set to <code>true</code> implicitly.
     *
     * @param map The wrapped drive event
     * @return The drive event
     */
    public static DriveEvent unwrap(Map<String, Serializable> map) {
        if (null == map) {
            return null;
        }
        Integer contextID = (Integer)map.get("__contextID");
        String[] folderIDs = (String[])map.get("__folderIDs");
        String pushToken = (String)map.get("__pushToken");
        if (null != folderIDs && null != contextID) {
            return new DriveEventImpl(contextID.intValue(), new HashSet<String>(Arrays.asList(folderIDs)), true, pushToken);
        } else {
            return null;
        }
    }

    private DriveEventWrapper() {
        super();
    }

}
