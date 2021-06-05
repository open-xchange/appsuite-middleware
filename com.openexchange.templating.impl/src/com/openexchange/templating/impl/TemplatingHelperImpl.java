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

import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplatingHelper;


/**
 * {@link TemplatingHelperImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TemplatingHelperImpl implements TemplatingHelper {

    private final Object rootObject;
    private final Session session;
    private final TemplateServiceImpl templateService;
    private final boolean createCopy;

    public TemplatingHelperImpl(Object rootObject, Session session, TemplateServiceImpl templateServiceImpl, boolean createCopy) {
        super();
        this.rootObject = rootObject;
        this.session = session;
        this.templateService = templateServiceImpl;
        this.createCopy = createCopy;
    }

    @Override
    public String include(String templateName) throws OXException {
        OXTemplate template = null;
        if (session != null) {
            template = templateService.loadTemplate(templateName, templateName, session, createCopy);
        } else {
            template = templateService.loadTemplate(templateName);
        }

        AllocatingStringWriter writer = new AllocatingStringWriter();
        template.process(rootObject, writer);

        return writer.toString();
    }

}
