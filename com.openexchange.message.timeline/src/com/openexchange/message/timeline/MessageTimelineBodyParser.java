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

package com.openexchange.message.timeline;

import java.io.IOException;
import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.DefaultBodyParser;
import com.openexchange.exception.OXException;
import com.openexchange.message.timeline.util.LimitExceededIOException;
import com.openexchange.message.timeline.util.LimitReader;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link MessageTimelineBodyParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class MessageTimelineBodyParser extends DefaultBodyParser {

    private static final int LIMIT = 2048;

    private static final String MODULE = MessageTimelineActionFactory.MODULE;

    // ---------------------------------------------------------------------------------------- //

    private final MessageTimelineActionFactory actionFactory;

    /**
     * Initializes a new {@link MessageTimelineBodyParser}.
     */
    public MessageTimelineBodyParser(final MessageTimelineActionFactory actionFactory) {
        super();
        this.actionFactory = actionFactory;
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public boolean accepts(final AJAXRequestData requestData) {
        return MODULE.equals(requestData.getModule()) && actionFactory.contains(requestData.getAction());
    }

    @Override
    protected void hookHandleIOException(final IOException ioe) throws OXException {
        if (ioe instanceof LimitExceededIOException) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        super.hookHandleIOException(ioe);
    }

    @Override
    protected Reader hookGetReaderFor(final HttpServletRequest req) throws IOException {
        return new LimitReader(super.hookGetReaderFor(req), LIMIT);
    }

}
