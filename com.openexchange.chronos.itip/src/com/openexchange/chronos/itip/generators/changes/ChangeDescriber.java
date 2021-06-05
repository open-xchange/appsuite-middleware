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

package com.openexchange.chronos.itip.generators.changes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.itip.generators.changes.generators.ChangeDescriptionProvider;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link ChangeDescriber}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChangeDescriber {

    private final ChangeDescriptionGenerator[] generators;

    /**
     * 
     * Initializes a new {@link ChangeDescriber} with all available {@link ChangeDescriptionGenerator}.
     *
     */
    public ChangeDescriber() {
        this(ChangeDescriptionProvider.getGenerators());
    }

    public ChangeDescriber(ChangeDescriptionGenerator... generators) {
        this.generators = generators;
    }

    public List<String> getChanges(Context ctx, Event original, Event update, ITipEventUpdate diff, TypeWrapper wrapper, Locale locale, TimeZone timezone, int recipientUserId) throws OXException {
        if (diff == null) {
            return Collections.emptyList();
        }

        List<String> changeDescriptions = new ArrayList<String>(generators.length);
        for (ChangeDescriptionGenerator generator : generators) {
            if (diff.containsAnyChangeOf(generator.getFields())) {
                List<Sentence> descriptions = generator.getDescriptions(ctx, original, update, diff, locale, timezone, recipientUserId);
                if (null != descriptions) {
                    for (Sentence changeDescription : descriptions) {
                        changeDescriptions.add(changeDescription.getMessage(wrapper, locale));
                    }
                }
            }
        }
        return changeDescriptions;
    }
}
