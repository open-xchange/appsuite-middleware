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

package com.openexchange.realtime.handle.impl.message;

import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link DestinationSelector}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DestinationSelector {
    
    public static IDMap<Resource> select(ID recipient) throws OXException {
        ResourceDirectory resourceDirectory = Services.optService(ResourceDirectory.class);
        if (resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class.getName());
        }
        
        IDMap<Resource> resources = resourceDirectory.get(recipient);
        if (resources.isEmpty()) {
            return resources;
        }
        
        IDMap<Resource> receivers = new IDMap<Resource>();
        byte highest = 0;
        for (Entry<ID, Resource> entry : resources.entrySet()) {
            ID id = entry.getKey();
            Resource resource = entry.getValue();
            byte priority = 0;
            if (resource.getPresence() != null) {
                priority = resource.getPresence().getPriority();
            }
            if (priority == highest) {
                receivers.put(id, resource);
            } else if (priority > highest) {
                receivers.clear();
                receivers.put(id, resource);
            }
        }
        
        return receivers;
    }

}
