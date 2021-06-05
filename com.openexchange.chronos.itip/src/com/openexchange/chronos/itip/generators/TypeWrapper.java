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

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.ShownAsTransparency;

/**
 * {@link TypeWrapper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface TypeWrapper {

    static final Map<String, TypeWrapper> WRAPPER = ImmutableMap.of("text", new PassthroughWrapper(), "html", new HTMLWrapper());

    String participant(Object argument);

    String original(Object argument);

    String updated(Object argument);

    String state(Object argument, ParticipationStatus confirmStatus);

    String none(Object argument);

    String emphasiszed(Object argument);

    String reference(Object argument);

    String shownAs(Object argument, ShownAsTransparency shownAs);

    String italic(Object argument);
    
    String getFormat();
}
