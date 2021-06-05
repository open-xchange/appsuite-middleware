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

package com.openexchange.templating.impl;

import com.openexchange.groupware.infostore.DocumentMetadata;


/**
 * {@link DocumentMetadataMatcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DocumentMetadataMatcher {
    private DocumentMetadata bestMatch;
    private final String name;
    private int score;

    public DocumentMetadataMatcher(final String name) {
        this.name = name;
        this.score = 0;

    }

    public boolean hasPerfectMatch() {
        return score > 10;
    }

    public DocumentMetadata getBestMatch() {
        return bestMatch;
    }

    public void propose(final DocumentMetadata document) {
        int newScore = 0;
        final String fileName = document.getFileName();
        if (fileName == null) {
            return;
        }
        if (fileName.equals(name)) {
            newScore = 100;
        }
        if (fileName.indexOf('.') >= 0) {
            final String filenameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            if (filenameWithoutExtension.equals(name)) {
                newScore = 5;
            }
        }

        if (document.getTitle() != null && document.getTitle().equals(name)) {
            newScore = 7;
        }


        if (newScore > score) {
            score = newScore;
            bestMatch = document;
        }
    }
}

