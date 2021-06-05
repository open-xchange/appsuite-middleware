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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;

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
     * For this purpose it only considers relative alarms and only relative changes are applied to exceptions as well.
     *
     * @param originalAlarms The original alarms of the master event
     * @param updatedAlarms The updated alarms of the master event
     * @param exceptions The list of event exceptions for the master event containing the alarms for those events.
     * @return A mapping of event exceptions to updated alarms for this exceptions
     */
    public static Map<Event, List<Alarm>> getUpdatedExceptions(List<Alarm> originalAlarms, List<Alarm> updatedAlarms, List<Event> exceptions) {
        /*
         *  1. filter out relative triggers 
         */
        List<Alarm> filteredOriginal = AlarmUtils.filterRelativeTriggers(originalAlarms);
        List<Alarm> filteredUpdated = AlarmUtils.filterRelativeTriggers(updatedAlarms);
        
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = AlarmUtils.getAlarmUpdates(filteredOriginal, filteredUpdated);
        if (null == exceptions || alarmUpdates.isEmpty()) {
            return Collections.emptyMap();
        }
        /*
         *  2. find exceptions with matching alarms
         */
        Map<Event, List<Alarm>> unrelativeAlarms = new HashMap<>();
        Map<Event, Map<Integer, Alarm>> applicableExceptions = findExceptionsWithMatchingAlarms(exceptions, filteredOriginal, unrelativeAlarms);
        /*
         *  3. apply updates
         */
        List<Integer> removedAlarmIds = getRemovedAlarmIds(alarmUpdates);
        Map<Integer, ItemUpdate<Alarm, AlarmField>> updatedMasterAlarms = getUpdatedAlarms(alarmUpdates);
        Map<Event, List<Alarm>> result = new HashMap<Event, List<Alarm>>();
        for (Entry<Event, Map<Integer, Alarm>> entry : applicableExceptions.entrySet()) {
            List<Alarm> updatedExceptionAlarms = new ArrayList<>();
            /*
             *  3.1. remove alarms
             */
            entry.getValue().entrySet().removeIf((x) -> removedAlarmIds.contains(x.getKey()));
            /*
             *  3.2. update existing alarms
             */
            updatedExceptionAlarms.addAll(applyUpdate(entry.getValue(), updatedMasterAlarms));
            /*
             *  3.3 add new alarms
             */
            if (null != alarmUpdates.getAddedItems() && false == alarmUpdates.getAddedItems().isEmpty()) {
                try {
                    updatedExceptionAlarms.addAll(AlarmMapper.getInstance().copy(alarmUpdates.getAddedItems(), (AlarmField[]) null));
                } catch (OXException e) {
                    // Should never happen
                    LOGGER.debug("Unable to copy alarm", e);
                }
            }
            /*
             *  3.4. re-add previously filtered non relative alarms
             */
            updatedExceptionAlarms.addAll(unrelativeAlarms.get(entry.getKey()));
            
            result.put(entry.getKey(), updatedExceptionAlarms);
        }
        return result;
        
    }
    
    /**
     * Finds event exceptions with matching alarms.
     * 
     * @param exceptions The list of exceptions
     * @param originial The list of original alarms
     * @param unrelativeAlarms An empty map which is going to be filled with unrelative alarms
     * @return A mapping of event exceptions to a mapping of original alarm ids to exception alarms 
     */
    private static Map<Event, Map<Integer, Alarm>> findExceptionsWithMatchingAlarms(List<Event> exceptions, List<Alarm> originial, Map<Event, List<Alarm>> unrelativeAlarms){
        Map<Event, Map<Integer, Alarm>> applicableExceptions = new HashMap<>();
        for (Event exception : exceptions) {
            List<Alarm> exceptionAlarms = exception.getAlarms();
            List<Alarm> filtered = AlarmUtils.filterRelativeTriggers(exceptionAlarms);
            Optional<Map<Integer, Alarm>> optional = getRelations(originial, filtered);
            if (optional.isPresent()) {
                applicableExceptions.put(exception, optional.get());
                List<Alarm> copy = exceptionAlarms == null ? new ArrayList<>() : copyAlarms(exceptionAlarms);
                if (filtered != null && false == filtered.isEmpty()) {
                    copy.removeIf((alarm) ->  filtered.stream().anyMatch((other) -> alarm.getId() == other.getId()));
                }
                unrelativeAlarms.put(exception, copy);
            }
        }
        return applicableExceptions;
    }
    
    /**
     * Returns a list of updated alarms
     *
     * @param relations A mapping of relations between the original alarms and the exception alarms
     * @param updatedMasterAlarms The alarm updates for the master event
     * @return A list of updated alarms
     */
    private static List<Alarm> applyUpdate(Map<Integer, Alarm> relations, Map<Integer, ItemUpdate<Alarm, AlarmField>> updatedMasterAlarms) {
        List<Alarm> result = new ArrayList<>(relations.size());
        for (Entry<Integer, Alarm> relation : relations.entrySet()) {
            if (updatedMasterAlarms.containsKey(relation.getKey())) {
                Alarm copy = copyAlarm(updatedMasterAlarms.get(relation.getKey()), relation.getValue());
                if (copy != null) {
                    result.add(copy);
                }
            } else {
                // Add as-is
                Alarm copy = copyAlarm(relation.getValue(), (AlarmField[]) null);
                if (copy != null) {
                    result.add(copy);
                }
            }
        }
        return result;
    }
    
    /**
     * Get a {@link List} of remove alarm ids
     *
     * @param alarmUpdates {@link CollectionUpdate} containing the removed alarms
     * @return A {@link List} of removed alarm ids
     */
    private static List<Integer> getRemovedAlarmIds(CollectionUpdate<Alarm, AlarmField> alarmUpdates) {
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
     * @return A mapping of id to alarms or an empty map in case an alarm is unrelated
     */
    private static Optional<Map<Integer, Alarm>> getRelations(List<Alarm> originalAlarms, List<Alarm> exceptionAlarms) {
        if (originalAlarms == null && exceptionAlarms == null) {
            return Optional.of(Collections.emptyMap());
        }
        if (originalAlarms == null || exceptionAlarms == null || originalAlarms.size() != exceptionAlarms.size()) {
            // Avoid changing exceptions alarms if they do not match the alarms in the master event
            return Optional.empty();
        }

        Map<Integer, Alarm> result = new HashMap<>(originalAlarms.size());
        for (Alarm alarm : originalAlarms) {
            Optional<Alarm> related = getRelatedAlarm(alarm, exceptionAlarms);
            if (false == related.isPresent()) {
                // Alarms differ, avoid any overwrite
                return Optional.empty();
            }
            result.put(I(alarm.getId()), related.get());
        }
        return Optional.of(result);
    }

    /**
     * Optionally get the related alarm for the given alarm
     *
     * @param alarm The alarm to find a related alarm for
     * @param exceptionAlarms The list of alarms to check
     * @return An {@link Optional} containing the alarm or not
     */
    private static Optional<Alarm> getRelatedAlarm(Alarm alarm, List<Alarm> exceptionAlarms) {
        return exceptionAlarms.stream().filter((other) -> {
            return AlarmMapper.getInstance().getDifferentFields(alarm, other, true, AlarmField.ID, AlarmField.UID, AlarmField.TIMESTAMP, AlarmField.ACKNOWLEDGED).isEmpty();
        }).findAny();

    }

    /**
     * Copies a given alarm 
     *
     * @param alarm The alarm to copy
     * @param fields The fields to copy
     * @return The copied alarm or null
     */
    private static Alarm copyAlarm(Alarm alarm, AlarmField... fields) {
        try {
            return AlarmMapper.getInstance().copy(alarm, null, fields);
        } catch (OXException e) {
            // Should never happen
            LOGGER.debug("Unable to copy alarm", e);
        }
        return null;
    }

    /**
     * Copies a given alarm and applies the changes from the {@link ItemUpdate} to it
     *
     * @param update The {@link ItemUpdate}
     * @param alarm The alarm to copy
     * @return A copy of the alarm with the changes applied
     */
    private static Alarm copyAlarm(ItemUpdate<Alarm, AlarmField> update, Alarm alarm) {
        try {
            Alarm copy = copyAlarm(alarm, AlarmMapper.getInstance().getAssignedFields(alarm));
            return AlarmMapper.getInstance().copy(update.getUpdate(), copy, update.getUpdatedFields().toArray(new AlarmField[0]));
        } catch (OXException e) {
            // Should never happen
            LOGGER.debug("Unable to copy alarm", e);
        }
        return null;
    }
    
    /**
     * Copies a list of alarms
     *
     * @param alarmsToCopy The alarms to copy
     * @return A copy of the alarms 
     */
    private static List<Alarm> copyAlarms(List<Alarm> alarmsToCopy) {
        try {
            List<Alarm> copy = new ArrayList<>(alarmsToCopy.size());
            for(Alarm alarm: alarmsToCopy) {
                copy.add(AlarmMapper.getInstance().copy(alarm, null, AlarmMapper.getInstance().getAssignedFields(alarm)));
            }
            return copy;
        } catch (OXException e) {
            // should never happen
            LOGGER.debug("Unable to copy alarm list", e);
            return Collections.emptyList();
        }
    }

}
