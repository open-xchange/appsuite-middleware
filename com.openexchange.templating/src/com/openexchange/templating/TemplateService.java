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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
@SingletonService
public interface TemplateService {

    /**
     * Loads the denoted template from default template path w/o an exception handler.
     *
     * @param templateName The template name
     * @return The loaded template
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName) throws OXException;

    /**
     * Loads the denoted template from default template path using given optional exception handler.
     *
     * @param templateName The template name
     * @param exceptionHandler The exception handler or <code>null</code>
     * @return The loaded template
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, OXTemplateExceptionHandler exceptionHandler) throws OXException;

    /**
     * Loads the denoted template w/o an exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The name of default template
     * @param session The associated session
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session) throws OXException;

    /**
     * Loads the denoted template using optional exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The name of default template
     * @param session The associated session
     * @param exceptionHandler The exception handler or <code>null</code>
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session, OXTemplateExceptionHandler exceptionHandler) throws OXException;

    /**
     * Loads the denoted template w/o an exception handler.
     *
     * @param templateName The template name
     * @param defaultTemplateName The template path
     * @param session The associated session
     * @param createCopy <code>true</code> to return a copy; otherwise <code>false</code> for a reference
     * @return The loaded template or <code>defaultTemplateName</code> in case <code>templateName</code> is empty or user has no such permission
     * @throws OXException If loading template fails
     */
    OXTemplate loadTemplate(String templateName, String defaultTemplateName, Session session, boolean createCopy) throws OXException;

    /**
     * Gets the names of basic templates that match given filters
     *
     * @param filter The optional filter tags
     * @return The matching names
     * @throws OXException If operation fails
     */
    List<String> getBasicTemplateNames(String... filter) throws OXException;

    /**
     * Gets the names of the user templates that match given filters
     *
     * @param session The associated session
     * @param filter The optional filter tags
     * @return The matching names
     * @throws OXException If operation fails
     */
    List<String> getTemplateNames(Session session, String... filter) throws OXException;

    /**
     * Creates a templating helper.
     *
     * @param rootObject The root object
     * @param session The associated session
     * @param createCopy Whether to create a copy
     * @return The templating helper
     */
    TemplatingHelper createHelper(Object rootObject, Session session, boolean createCopy);
    
    /**
     * Loads an image from the configured template path, encodes the image to base64 and returns a {@link Pair} consisting of 
     * (contentType, base64Representation) of the image for use within data-urls in templates
     * 
     * @param imageName The name of the image within the templates path
     * @return a {@link Pair} consisting of (contentType, base64Representation) of the image at footerImagePath
     * @throws OXException if loading/encoding fails
     */
    public Pair<String, String> encodeTemplateImage(String imageName) throws OXException;

}
