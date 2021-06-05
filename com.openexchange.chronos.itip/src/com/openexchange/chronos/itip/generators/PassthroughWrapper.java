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

import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.ShownAsTransparency;


/**
 * {@link PassthroughWrapper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PassthroughWrapper implements TypeWrapper {

    @Override
    public String none(final Object argument) {
        if (argument != null) {
            return argument.toString();
        }
        return "";
    }

    @Override
    public String original(final Object argument) {
        return none(argument);
    }

    @Override
    public String participant(final Object argument) {
        return none(argument);
    }

    @Override
    public String state(final Object argument, final ParticipationStatus status) {
        return none(argument);
    }

    @Override
    public String updated(final Object argument) {
        return none(argument);
    }

	@Override
    public String emphasiszed(final Object argument) {
		return none(argument);
	}

	@Override
    public String reference(final Object argument) {
		return none(argument);
	}

	@Override
	public String shownAs(final Object argument, final ShownAsTransparency shownAs) {
		return none(argument);
	}

    @Override
    public String italic(Object argument) {
        return none(argument);
    }
    
    @Override
    public String getFormat() {
        return "text";
    }

}
