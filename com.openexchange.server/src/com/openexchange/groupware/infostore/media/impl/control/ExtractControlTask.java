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

package com.openexchange.groupware.infostore.media.impl.control;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link ExtractControlTask} - Responsible for interrupting expired threads currently extracting media metadata.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ExtractControlTask implements Runnable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtractControlTask.class);
    }

    /**
     * Initializes a new {@link ExtractControlTask}.
     */
    public ExtractControlTask() {
        super();
    }

    @Override
    public void run() {
        try {
            Thread runner = Thread.currentThread();
            ExtractControl control = ExtractControl.getInstance();
            while (!runner.isInterrupted()) {
                List<ExtractAndApplyMediaMetadataTask> expired = control.awaitExpired();
                boolean poisoned = expired.remove(ExtractAndApplyMediaMetadataTask.POISON);
                for (ExtractAndApplyMediaMetadataTask task : expired) {
                    // Extracting for too long
                    task.interrupt();
                    DocumentMetadata document = task.getDocument();
                    int contextId = task.getContextId();
                    if (contextId > 0) {
                        LoggerHolder.LOG.warn("Interrupted exceeded extraction of media metadata from document {} ({}) with version {} in context {}", I(document.getId()), document.getFileName(), I(document.getVersion()), I(contextId));
                    } else {
                        LoggerHolder.LOG.warn("Interrupted exceeded extraction of media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));
                    }
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
