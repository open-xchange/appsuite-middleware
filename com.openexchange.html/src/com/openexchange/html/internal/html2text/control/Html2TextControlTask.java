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

package com.openexchange.html.internal.html2text.control;

import java.util.List;

/**
 * {@link Html2TextControlTask} - Responsible for interrupting expired threads currently performing HTML-to-text conversion.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class Html2TextControlTask implements Runnable {

    /**
     * Initializes a new {@link Html2TextControlTask}.
     */
    public Html2TextControlTask() {
        super();
    }

    @Override
    public void run() {
        try {
            Thread runner = Thread.currentThread();
            Html2TextControl control = Html2TextControl.getInstance();
            while (!runner.isInterrupted()) {
                List<Html2TextTask> expired = control.awaitExpired();
                boolean poisoned = expired.remove(Html2TextTask.POISON);
                for (Html2TextTask task : expired) {
                    // Parsing for too long
                    task.interrupt();
                }
                if (poisoned) {
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

}
