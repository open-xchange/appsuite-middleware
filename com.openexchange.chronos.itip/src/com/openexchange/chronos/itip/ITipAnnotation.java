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

package com.openexchange.chronos.itip;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import com.openexchange.chronos.Event;
import com.openexchange.i18n.tools.StringHelper;

/**
 * 
 * {@link ITipAnnotation}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipAnnotation {

    private String message;
    private List<Object> args;
    private Event additional;

    public ITipAnnotation(String message, Locale locale, Object... args) {
        if (locale != null) {
            message = StringHelper.valueOf(locale).getString(message);
        }
        this.message = message;
        this.args = Arrays.asList(args);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public void setArgs(Object... args) {
        this.args = Arrays.asList(args);
    }

    public Event getEvent() {
        return additional;
    }

    public void setEvent(Event additional) {
        this.additional = additional;
    }

}
