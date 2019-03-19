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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmUpdateProcessor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class AlarmUpdateProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmUpdateProcessor.class);

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
        if (null == exceptions || alarmUpdates.isEmpty()) {
            return Collections.emptyMap();
        }

        // Get removed and updated items
        List<Integer> removedItems = getRemovedItems(alarmUpdates);
        Map<Integer, ItemUpdate<Alarm, AlarmField>> updatedMasterAlarms = getUpdatedAlarms(alarmUpdates);

        Map<Event, List<Alarm>> result = new HashMap<Event, List<Alarm>>(updatedAlarms.size());
        for (Event exception : exceptions) {
            List<Alarm> newAlarms = new ArrayList<>(updatedAlarms.size());
            for (Iterator<Entry<Integer, Alarm>> iterator = getRelations(originalAlarms, exception.getAlarms(), removedItems).entrySet().iterator(); iterator.hasNext();) {
                // Alarm IDs mapped to the exceptions alarm
                Entry<Integer, Alarm> entry = iterator.next();
                if (updatedMasterAlarms.containsKey(entry.getKey())) {
                    newAlarms.add(copyAlarm(updatedMasterAlarms, entry));
                } else {
                    // Add as-is
                    newAlarms.add(copyAlarm(entry.getValue(), (AlarmField[]) null));
                }
            }
            // Finally add all new alarms
            if (null != alarmUpdates.getAddedItems() && false == alarmUpdates.getAddedItems().isEmpty()) {
                newAlarms.addAll(copyAlarms(alarmUpdates.getAddedItems()));
            }

            // Add to result
            result.put(exception, newAlarms);
        }
        return result;
    }

    /**
     * Get a {@link List} of identifier for removed alarms
     *
     * @param alarmUpdates {@link CollectionUpdate} containing the removed alarms
     * @return A {@link List} containing removed items
     */
    private static List<Integer> getRemovedItems(CollectionUpdate<Alarm, AlarmField> alarmUpdates) {
        if (null != alarmUpdates.getRemovedItems() && false == alarmUpdates.getRemovedItems().isEmpty()) {
            List<Integer> removedItems = new LinkedList<Integer>();
            for (Alarm a : alarmUpdates.getRemovedItems()) {
                removedItems.add(I(a.getId()));
            }
            return removedItems;
        }
        return Collections.emptyList();
    }

    /**
     * Get all alarm updates mapped by their ID
     *
     * @param alarmUpdates All alarm changes in an {@link CollectionUpdate}
     * @return A {@link Map} containing the {@link ItemUpdate}s identified by their ID
     */
    private static Map<Integer, ItemUpdate<Alarm, AlarmField>> getUpdatedAlarms(CollectionUpdate<Alarm, AlarmField> alarmUpdates) {
        if (null == alarmUpdates.getUpdatedItems() || alarmUpdates.getUpdatedItems().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, ItemUpdate<Alarm, AlarmField>> updated = new HashMap<>(alarmUpdates.getUpdatedItems().size());
        for (ItemUpdate<Alarm, AlarmField> itemUpdate : alarmUpdates.getUpdatedItems()) {
            updated.put(I(itemUpdate.getOriginal().getId()), itemUpdate);
        }
        return updated;
    }

    /**
     * Gets the original alarm ids mapped to the related alarm of the exception. Return an empty map in case the alarm lists contain unrelated items.
     *
     * @param originalAlarms The original alarms of the master event
     * @param exceptionAlarms The original alarms of the exception event
     * @param removedItems The removed alarms to skip implicit
     * @return A mapping of id to alarms or an empty map in case an alarm is unrelated
     */
    private static Map<Integer, Alarm> getRelations(List<Alarm> originalAlarms, List<Alarm> exceptionAlarms, List<Integer> removedItems) {
        if (originalAlarms == null || exceptionAlarms == null || originalAlarms.size() != exceptionAlarms.size()) {
            // Avoid changing exceptions alarms if they do not match the alarms in the master event
            return Collections.emptyMap();
        }

        Map<Integer, Alarm> result = new HashMap<>(originalAlarms.size());
        for (Alarm alarm : originalAlarms) {
            Optional<Alarm> related = getRelated(alarm, exceptionAlarms);
            if (false == related.isPresent()) {
                // Alarms differ, avoid any overwrite
                return Collections.emptyMap();
            }
            Alarm exceptionAlarm = related.get();
            // Skip removed alarms
            if (false == removedItems.contains(I(exceptionAlarm.getId())))
                result.put(I(alarm.getId()), exceptionAlarm);
        }
        return result;
    }

    /**
     * Optionally get the related alarm for the given alarm
     *
     * @param alarm The alarm to find a related alarm for
     * @param exceptionAlarms The list of alarms to check
     * @return An {@link Optional} containing the alarm or not
     */
    private static Optional<Alarm> getRelated(Alarm alarm, List<Alarm> exceptionAlarms) {
        return exceptionAlarms.stream().filter((other) -> {
            return AlarmMapper.getInstance().getDifferentFields(alarm, other, true, AlarmField.ID, AlarmField.UID, AlarmField.TIMESTAMP).isEmpty();
        }).findAny();

    }

    private static Alarm copyAlarm(Alarm alarm, AlarmField... fields) {
        try {
            return AlarmMapper.getInstance().copy(alarm, null, fields);
        } catch (OXException e) {
            LOGGER.debug("Unable to copy alarm", e);
        }
        return alarm;
    }

    private static List<Alarm> copyAlarms(List<Alarm> alarms) {
        List<Alarm> copied = new LinkedList<Alarm>();
        for (Alarm alarm : alarms) {
            copied.add(copyAlarm(alarm, (AlarmField[]) null));
        }
        return copied;
    }

    private static Alarm copyAlarm(Map<Integer, ItemUpdate<Alarm, AlarmField>> updatedMasterAlarms, Entry<Integer, Alarm> entry) {
        ItemUpdate<Alarm, AlarmField> masterAlarm = updatedMasterAlarms.get(entry.getKey());
        try {
            Alarm copy = copyAlarm(entry.getValue(), AlarmMapper.getInstance().getAssignedFields(entry.getValue()));
            return AlarmMapper.getInstance().copy(masterAlarm.getUpdate(), copy, masterAlarm.getUpdatedFields().toArray(new AlarmField[0]));
        } catch (OXException e) {
            LOGGER.debug("Unable to copy alarm", e);
        }
        return masterAlarm.getUpdate();
    }

}
