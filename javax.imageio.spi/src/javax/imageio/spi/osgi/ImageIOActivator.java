
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
