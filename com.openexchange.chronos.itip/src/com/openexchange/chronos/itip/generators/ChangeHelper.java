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

package com.openexchange.chronos.itip.generators;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriber;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link ChangeHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChangeHelper {

    private final ChangeDescriber describer;

    private final ITipEventUpdate diff;
    private final Event update;
    private final Event original;
    private final TypeWrapper wrapper;

    private final Context ctx;

    private final Locale locale;

    private final TimeZone timezone;

    private int recipientUserId;

    public ChangeHelper(final Context ctx, final Event original, final Event update, final ITipEventUpdate diff, final Locale locale, final TimeZone tz, final TypeWrapper wrapper, int userId) {
        super();
        this.original = original;
        this.update = update;
        this.diff = diff;
        this.locale = locale;
        this.timezone = tz;
        this.wrapper = wrapper;
        this.ctx = ctx;
        this.recipientUserId = userId;
        describer = new ChangeDescriber();

    }

    public List<String> getChanges() throws OXException {
        return describer.getChanges(ctx, original, update, diff, wrapper, locale, timezone, recipientUserId);
    }

}
