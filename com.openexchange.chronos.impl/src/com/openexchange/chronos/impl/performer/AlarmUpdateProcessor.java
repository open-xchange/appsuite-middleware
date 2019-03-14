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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmUpdateProcessor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class AlarmUpdateProcessor {

    /**
     * Initializes a new {@link AlarmUpdateProcessor}.
     */
    private AlarmUpdateProcessor() {}

    /**
     * Prepares a map of all exceptions which have the same alarms as the master event. Whereby the key is the exception event and the value is the list of updated alarms for this exception.
     *
     * @param originalAlarms The original alarms of the master event
     * @param updatedAlarms The updated alarms of the master event
     * @param exceptions The list of event exceptions for the master event containing the alarms for those events.
     * @return A mapping of event exceptions to updated alarms for this exceptions
     */
    public static Map<Event, List<Alarm>> getUpdatedExceptions(List<Alarm> originalAlarms, List<Alarm> updatedAlarms, List<Event> exceptions) {
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = AlarmUtils.getAlarmUpdates(originalAlarms, updatedAlarms);

        return exceptions.stream().collect(HashMap::new, (map, event) -> {
            Optional<Map<Integer, Alarm>> optRelations = getRelations(originalAlarms, event.getAlarms());
            if (optRelations.isPresent()) {
                Map<Integer, Alarm> relations = optRelations.get();
                List<Alarm> newAlarms = new ArrayList<>(updatedAlarms.size());
                alarmUpdates.getRemovedItems().forEach((removed) -> relations.remove(I(removed.getId())));
                alarmUpdates.getUpdatedItems().forEach((update) -> {
                    Alarm exceptionAlarm = relations.remove(I(update.getOriginal().getId()));
                    try {
                        Alarm copy = AlarmMapper.getInstance().copy(exceptionAlarm, null, AlarmMapper.getInstance().getAssignedFields(exceptionAlarm));
                        AlarmMapper.getInstance().copy(update.getUpdate(), copy, update.getUpdatedFields().stream().toArray(AlarmField[]::new));
                        // add updated alarms
                        newAlarms.add(copy);
                    } catch (@SuppressWarnings("unused") OXException e) {
                        // Should never happen
                    }
                });
                // add new alarms
                newAlarms.addAll(alarmUpdates.getAddedItems());
                // Add unchanged alarms
                newAlarms.addAll(relations.values());
                map.put(event, newAlarms);
            }
        }, HashMap::putAll);
    }

    /**
     * Optionally gets the original alarm ids mapped to the related alarm of the exception. Return an empty {@link Optional} in case the alarm lists contain unrelated items.
     *
     * @param tmpMaster The original alarms of the master event
     * @param tmpException The original alarms of the exception event
     * @return A mapping of id to alarms or an empty {@link Optional} in case an alarm is unrelated
     */
    private static Optional<Map<Integer, Alarm>> getRelations(List<Alarm> master, List<Alarm> exception) {
        List<Alarm> tmpMaster = master;
        if(tmpMaster == null) {
            tmpMaster = Collections.emptyList();
        }
        List<Alarm> tmpException = exception;
        if(tmpException == null) {
            tmpException = Collections.emptyList();
        }
        
        if (tmpMaster.size() != tmpException.size()) {
            return Optional.empty();
        }

        Map<Integer, Alarm> result = new HashMap<>(tmpMaster.size());
        for (Alarm alarm : tmpMaster) {
            Optional<Alarm> related = getRelated(alarm, tmpException);
            if (related.isPresent()) {
                result.put(I(alarm.getId()), related.get());
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(result);
    }

    /**
     * Optionally get the related alarm for the given alarm
     *
     * @param alarm The alarm to find a related alarm for
     * @param alarms The list of alarms to check
     * @return An {@link Optional} containing the alarm or not
     */
    private static Optional<Alarm> getRelated(Alarm alarm, List<Alarm> alarms) {
        return alarms.stream().filter((other) -> {
            return AlarmMapper.getInstance().getDifferentFields(alarm, other, true, AlarmField.ID, AlarmField.UID, AlarmField.TIMESTAMP).isEmpty();
        }).findFirst();

    }

}
