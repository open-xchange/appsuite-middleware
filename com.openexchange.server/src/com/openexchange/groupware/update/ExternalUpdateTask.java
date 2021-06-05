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

package com.openexchange.groupware.update;

/**
 * This is only a marker interface for third party update tasks. In the past there have been reasons for introducing this marker interface.
 * The reason was the configuration file updatetasks.cfg. It contained a list of tasks to execute. And this marker interface should indicate
 * tasks that should be executed although they are not listed in the updatetasks.cfg configuration file. But that configuration file has
 * been removed and it was replaced by a configurable exclude list of update tasks that does not have the necessity to indicate tasks added
 * by some third party.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface ExternalUpdateTask extends UpdateTaskV2 {
    // Only a marker interface.
}
