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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tools.images.transformations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.ExpectedExceptionFactory;
import com.openexchange.tools.images.scheduler.Scheduler;

/**
 * {@link ImageTransformationsTask} - A task for an {@link ImageTransformationsImpl} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class ImageTransformationsTask extends ImageTransformationsImpl {

    /** The exception factory constant */
    private static final ExpectedExceptionFactory<IOException> EXCEPTION_FACTORY = new ExpectedExceptionFactory<IOException>() {

        @Override
        public Class<IOException> getType() {
            return IOException.class;
        }

        @Override
        public IOException newUnexpectedError(final Throwable t) {
            return new IOException(t);
        }
    };

    // --------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param sourceImage The source image
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsTask(final BufferedImage sourceImage, final Object optSource) {
        super(sourceImage, optSource);
    }

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param sourceImageStream The image input stream
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsTask(final InputStream sourceImageStream, final Object optSource) {
        super(sourceImageStream, optSource);
    }

    @Override
    protected BufferedImage getImage(final String formatName) throws IOException {
        final FutureTask<BufferedImage> ft = new FutureTask<BufferedImage>(new CallableImpl(formatName));
        // Pass appropriate key object to accumulate tasks for the same caller/session/whatever
        final boolean success = Scheduler.getInstance().execute(optSource, ft);
        if (!success) {
            throw new IOException("Image transformation rejected");
        }
        return ThreadPools.getFrom(ft, EXCEPTION_FACTORY);
    }

    /**
     * Gets the resulting image after applying all transformations.
     *
     * @param formatName the image format to use, or <code>null</code> if not relevant
     * @return The transformed image
     * @throws IOException if an I/O error occurs
     */
    protected BufferedImage doGetImage(final String formatName) throws IOException {
        return super.getImage(formatName);
    }

    // --------------------------------------------------------------------------------------------------------- //

    private final class CallableImpl implements Callable<BufferedImage> {

        private final String formatName;

        CallableImpl(final String formatName) {
            this.formatName = formatName;
        }

        @Override
        public BufferedImage call() throws Exception {
            return doGetImage(formatName);
        }
    }

}
