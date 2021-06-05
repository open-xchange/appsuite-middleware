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

package com.openexchange.templating;

import java.io.Writer;
import com.openexchange.exception.OXException;


/**
 * {@link OXTemplate}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface OXTemplate {

    public enum TemplateLevel {
        SERVER, USER;
    }

    /**
     * Render the template
     * @param rootObject
     * @param writer
     * @throws OXException
     */
    public void process(Object rootObject, Writer writer) throws OXException;

    /**
     * On which level is this template defined?
     * @return
     */
    public TemplateLevel getLevel();
    
    
    /**
     * Is this template considered trusted?
     */
    public boolean isTrusted();
    
    public String getProperty(String name);
    
    public <T> T getProperty(String name, Class<T> klass);
    
    public String getProperty(String name, String defaultValue);
    
    public <T> T getProperty(String name, Class<T> klass, T defaultValue);
    
}
