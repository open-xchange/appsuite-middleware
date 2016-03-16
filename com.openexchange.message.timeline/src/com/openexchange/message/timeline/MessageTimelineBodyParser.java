/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
