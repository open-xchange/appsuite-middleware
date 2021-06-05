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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.BodyParser;
import com.openexchange.ajax.requesthandler.DefaultBodyParser;
import com.openexchange.java.ConcurrentList;


/**
 * {@link BodyParserRegistry} - A registry for {@link BodyParser} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class BodyParserRegistry extends ServiceTracker<BodyParser, BodyParser> {

    private final ConcurrentList<BodyParser> parsers;

    /**
     * Initializes a new {@link BodyParserRegistry}.
     *
     * @param context The bundle context
     */
    public BodyParserRegistry(BundleContext context) {
        super(context, BodyParser.class, null);
        parsers = new ConcurrentList<BodyParser>(Collections.<BodyParser> singletonList(DefaultBodyParser.getInstance()));
    }

    @Override
    public BodyParser addingService(ServiceReference<BodyParser> reference) {
        final BodyParser bodyParser = context.getService(reference);
        if (parsers.addIfAbsent(bodyParser)) {
            return bodyParser;
        }
        return null;
    }

    @Override
    public void removedService(ServiceReference<BodyParser> reference, BodyParser bodyParser) {
        parsers.remove(bodyParser);
        context.ungetService(reference);
    }

    /**
     * Gets the appropriate body parser for given request data.
     *
     * @param requestData The AJAX request data
     * @return The body parser or <code>null</code>
     */
    public BodyParser getParserFor(final AJAXRequestData requestData) {
        BodyParser candidate = null;
        for (final BodyParser parser : parsers) {
            if (parser.accepts(requestData) && (null == candidate || candidate.getRanking() < parser.getRanking())) {
                candidate = parser;
            }
        }
        return candidate;
    }

    /**
     * Gets the available parsers
     *
     * @return The parsers
     */
    public List<BodyParser> getParsers() {
        return Collections.unmodifiableList(parsers);
    }

}
