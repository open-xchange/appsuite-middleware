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

import org.apache.commons.text.StringEscapeUtils;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.ShownAsTransparency;

/**
 * {@link HTMLWrapper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HTMLWrapper extends PassthroughWrapper {

    @Override
    public String original(Object argument) {
        return wrap("original", argument);
    }

    @Override
    public String participant(Object argument) {
        return wrap("person", argument);
    }

    @Override
    public String state(Object argument, ParticipationStatus status) {
        return wrap("status " + getName(status), argument);
    }

    private String getName(ParticipationStatus status) {
        if (ParticipationStatus.ACCEPTED.matches(status)) {
            return "accepted";
        } else if (ParticipationStatus.DECLINED.matches(status)) {
            return "declined";
        } else if (ParticipationStatus.TENTATIVE.matches(status)) {
            return "tentative";
        } else {
            return "none";
        }
    }

    @Override
    public String updated(Object argument) {
        return wrap("updated", argument);
    }

    @Override
    public String emphasiszed(Object argument) {
        if (argument == null) {
            return "";
        }
        return "<em>" + escapeHtml(argument.toString()) + "</em>";
    }

    @Override
    public String shownAs(Object argument, ShownAsTransparency shownAs) {
        return wrap("shown_as_label " + shownAsCssClass(shownAs), argument);
    }

    private String shownAsCssClass(ShownAsTransparency shownAs) {
        if (null == shownAs) {
            return "unknown";
        }
        switch (shownAs) {
            case RESERVED:
                return "reserved";
            case TEMPORARY:
                return "temporary";
            case ABSENT:
                return "absent";
            case FREE:
                return "free";
            default:
                return "unknown";
        }
    }

    private String wrap(String string, Object argument) {
        if (argument == null) {
            return "";
        }
        return "<span class='" + string + "'>" + escapeHtml(argument.toString()) + "</span>";
    }

    @Override
    public String reference(Object argument) {
        if (argument == null) {
            return "";
        }
        String string = escapeHtml(argument.toString());
        return "<a href=\"" + string + "\">" + string + "</a>";
    }

    private String escapeHtml(String string) {
        return StringEscapeUtils.escapeHtml4(string);
    }

    @Override
    public String italic(Object argument) {
        if (argument == null) {
            return "";
        }
        return "<i>" + escapeHtml(argument.toString()) + "</i>";
    }

    @Override
    public String getFormat() {
        return "html";
    }

}
