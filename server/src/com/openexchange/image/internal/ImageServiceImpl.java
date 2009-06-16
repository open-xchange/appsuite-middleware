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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.image.internal;

import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.image.ImageService;
import com.openexchange.session.Session;

/**
 * {@link ImageServiceImpl} - Implementation of {@link ImageService} using {@link ImageRegistry}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageServiceImpl implements ImageService {

    /**
     * Initializes a new {@link ImageServiceImpl}
     */
    public ImageServiceImpl() {
        super();
    }

    public ImageData addImageData(final Session session, final DataSource imageSource, final DataArguments imageArguments, final int timeToLive) {
        return ImageRegistry.getInstance().addImageData(session, imageSource, imageArguments, timeToLive);
    }

    public ImageData addImageData(final int contextId, final DataSource imageSource, final DataArguments imageArguments, final int timeToLive) {
        return ImageRegistry.getInstance().addImageData(contextId, imageSource, imageArguments, timeToLive);
    }

    public ImageData addImageData(final Session session, final DataSource imageSource, final DataArguments imageArguments) {
        return ImageRegistry.getInstance().addImageData(session, imageSource, imageArguments);
    }

    public ImageData addImageData(final int contextId, final DataSource imageSource, final DataArguments imageArguments) {
        return ImageRegistry.getInstance().addImageData(contextId, imageSource, imageArguments);
    }

    public void addImageData(final int contextId, final ImageData imageData) {
        ImageRegistry.getInstance().addImageData(contextId, imageData);
    }

    public void addImageData(final Session session, final ImageData imageData) {
        ImageRegistry.getInstance().addImageData(session, imageData);
    }

    public void clearRegistry() {
        ImageRegistry.getInstance().clearRegistry();
    }

    public boolean containsImageData(final Session session, final String uniqueId) {
        return ImageRegistry.getInstance().containsImageData(session, uniqueId);
    }

    public boolean containsImageData(final int contextId, final String uniqueId) {
        return ImageRegistry.getInstance().containsImageData(contextId, uniqueId);
    }

    public ImageData[] getImageData(final Session session) {
        return ImageRegistry.getInstance().getImageData(session);
    }

    public ImageData[] getImageData(final int contextId) {
        return ImageRegistry.getInstance().getImageData(contextId);
    }

    public ImageData getImageData(final Session session, final String uniqueId) {
        return ImageRegistry.getInstance().getImageData(session, uniqueId);
    }

    public ImageData getImageData(final int contextId, final String uniqueId) {
        return ImageRegistry.getInstance().getImageData(contextId, uniqueId);
    }

    public void removeImageData(final Session session) {
        ImageRegistry.getInstance().removeImageData(session);
    }

    public void removeImageData(final int contextId) {
        ImageRegistry.getInstance().removeImageData(contextId);
    }

    public boolean removeImageData(final int contextId, final String uniqueId) {
        return ImageRegistry.getInstance().removeImageData(contextId, uniqueId);
    }

}
