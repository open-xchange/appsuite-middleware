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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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


package javax.imageio.spi.osgi;

import javax.imageio.spi.IIORegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ImageIOActivator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ImageIOActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        registerIIO();
    }

    private void registerIIO() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registerIIO("com.sun.media.imageioimpl.stream.ChannelImageInputStreamSpi", registry);
        registerIIO("com.sun.media.imageioimpl.stream.ChannelImageOutputStreamSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.png.CLibPNGImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.wbmp.WBMPImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.bmp.BMPImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.pnm.PNMImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLibSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.wbmp.WBMPImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.bmp.BMPImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.pnm.PNMImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.raw.RawImageWriterSpi", registry);
        registerIIO("com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi", registry);
        registerIIO("com.sun.media.jai.imageioimpl.ImageReadWriteSpi", registry);
    }

    private void registerIIO(String provider, IIORegistry registry) {
        try {
            registry.registerServiceProvider(this.getClass().getClassLoader().loadClass(provider).newInstance());
        } catch (Throwable t) {
            com.openexchange.log.Log.loggerFor(ImageIOActivator.class).error("Unable to register provider: " + provider, t);
        }
    }

}
