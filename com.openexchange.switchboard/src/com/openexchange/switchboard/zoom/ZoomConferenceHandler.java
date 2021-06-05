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

package com.openexchange.switchboard.zoom;

import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.switchboard.zoom.Utils.getZoomConferences;
import static com.openexchange.switchboard.zoom.Utils.matches;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.switchboard.Switchboard;
import com.openexchange.switchboard.SwitchboardConfiguration;
import com.openexchange.switchboard.osgi.Services;

/**
 * {@link ZoomConferenceHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class ZoomConferenceHandler implements CalendarHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomConferenceHandler.class);

    @Override
    public void handle(CalendarEvent event) {
        // TODO: Check for necessity (UI inflicted?)

        List<UpdateResult> updates = new LinkedList<>(event.getUpdates());
        List<DeleteResult> deletions = new LinkedList<>(event.getDeletions());

        for (UpdateResult update : updates) {
            handleUpdate(update, event);
        }

        for (DeleteResult delete : deletions) {
            handleDelete(delete, event);
        }
    }

    private void handleUpdate(UpdateResult update, CalendarEvent event) {
        if (hasExternalOrganizer(update.getOriginal())) {
            return;
        }

        List<Conference> originalConferences = getZoomConferences(update.getOriginal());
        List<Conference> updateConferences = getZoomConferences(update.getUpdate());

        if (originalConferences.isEmpty() && updateConferences.isEmpty()) {
            return;
        }

        if (originalConferences.isEmpty()) {
            // all new, nothing todo since this is already handled by the client (only appsuite UI adds conferences at this point)
        } else if (updateConferences.isEmpty()) {
            // all removed
            delete(originalConferences, update.getUpdate(), event);
        } else {
            // calculate diffs. Again, "added" is not relevant
            List<Conference> removed = new ArrayList<>(originalConferences);
            List<Conference> changed = new ArrayList<>();
            for (Conference upd : updateConferences) {
                removed.removeIf(c -> matches(c, upd));
                Optional<Conference> optional = originalConferences.stream().filter(c -> matches(c, upd)).findAny();
                if (optional.isPresent()) {
                    changed.add(optional.get());
                }
            }
            if (timeHasChanged(update)) {
                changed(changed, update.getUpdate(), event);
            }
            delete(removed, update.getUpdate(), event);
        }
    }

    private void handleDelete(DeleteResult delete, CalendarEvent event) {
        Event original = delete.getOriginal();
        if (hasExternalOrganizer(original)) {
            return;
        }

        List<Conference> zoomConferences = getZoomConferences(original);
        delete(zoomConferences, original, event);
    }

    /**
     * Checks if start or end date of the event have changed.
     *
     * @param update The Update
     * @return true if start or end date have changed, false otherwise
     */
    private boolean timeHasChanged(UpdateResult update) {
        return update.getUpdatedFields().contains(EventField.START_DATE) || update.getUpdatedFields().contains(EventField.END_DATE);
    }

    /**
     * Loads the configuration.
     *
     * @return The {@link SwitchboardConfiguration}
     */
    private SwitchboardConfiguration getConfig(int user, int context) {
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        if (config != null) {
            return SwitchboardConfiguration.getConfig(config, user, context);
        }
        LOG.error("Unable to load Zoom Configuration.");

        return null;
    }

    /**
     * Sends delete notifications to the switchboard.
     *
     * @param conferences The list of deleted conferences
     * @param event The event
     * @param calendarEvent The calendarEvent
     */
    private void delete(List<Conference> conferences, Event event, CalendarEvent calendarEvent) {
        Switchboard switchboard = new Switchboard(getConfig(calendarEvent.getCalendarUser(), calendarEvent.getContextId()));
        for (Conference conf : conferences) {
            switchboard.delete(conf, event, calendarEvent.getTimestamp());
        }
    }

    /**
     * Sends change notifications to the switchboard.
     *
     * @param conferences The list of changed conferences
     * @param updatedEvent The changed event itself
     * @param event The event
     */
    private void changed(List<Conference> conferences, Event updatedEvent, CalendarEvent event) {
        Switchboard switchboard = new Switchboard(getConfig(event.getCalendarUser(), event.getContextId()));
        for (Conference conf : conferences) {
            switchboard.update(conf, updatedEvent, event.getTimestamp());
        }
    }

}
