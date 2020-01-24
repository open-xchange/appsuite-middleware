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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttachmentDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttendeeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.DescriptionDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.LocationDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.OrganizerDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.RRuleDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.ReschedulingDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.SplitDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.SummaryDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.TransparencyDescriber;
import com.openexchange.chronos.service.EventUpdate;

/**
 * {@link DescriptionServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DescriptionServiceImpl implements DescriptionService {

    //@formatter:off
    private static final List<ChangeDescriber> DESCRIBERS = ImmutableList.of(
        new SplitDescriber(),
        new RRuleDescriber(),
        new ReschedulingDescriber(),
        new OrganizerDescriber(),
        new SummaryDescriber(),
        new LocationDescriber(),
        new DescriptionDescriber(), 
        new TransparencyDescriber(),
        new AttachmentDescriber(),
        new AttendeeDescriber()
    );
    //@formatter:on

    /**
     * Initializes a new {@link DescriptionServiceImpl}.
     */
    public DescriptionServiceImpl() {
        super();
    }

    @Override
    public List<Description> describe(EventUpdate eventUpdate, EventField... ignorees) {
        List<Description> descriptions = new LinkedList<>();
        for (ChangeDescriber describer : DESCRIBERS) {
            if ((null == ignorees || false == contains(describer.getFields(), ignorees)) && eventUpdate.containsAnyChangeOf(describer.getFields())) {
                Description description = describer.describe(eventUpdate);
                if (null != description) {
                    descriptions.add(description);
                }
            }
        }
        return descriptions;
    }

    @Override
    public List<Description> describeOnly(EventUpdate eventUpdate, EventField... toDescribe) {
        if (toDescribe == null || eventUpdate == null) {
            return Collections.emptyList();
        }
        List<Description> descriptions = new LinkedList<>();
        for (ChangeDescriber describer : DESCRIBERS) {
            if (contains(describer.getFields(), toDescribe) && eventUpdate.containsAnyChangeOf(describer.getFields())) {
                Description description = describer.describe(eventUpdate);
                if (null != description) {
                    descriptions.add(description);
                }
            }
        }
        return descriptions;
    }

    private static boolean contains(EventField[] describedFields, EventField... fieldsToMatch) {
        for (EventField f : describedFields) {
            for (EventField i : fieldsToMatch) {
                if (f.equals(i)) {
                    return true;
                }
            }
        }
        return false;
    }

}
