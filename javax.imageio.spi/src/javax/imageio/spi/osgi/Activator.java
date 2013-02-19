
package javax.imageio.spi.osgi;

import javax.imageio.spi.IIORegistry;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(Activator.class));

    private IIORegistry registry;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        registerIIO();
    }

    private void registerIIO() {
        registry = IIORegistry.getDefaultInstance();
        registerIIO("com.sun.media.imageioimpl.stream.ChannelImageInputStreamSpi");
        registerIIO("com.sun.media.imageioimpl.stream.ChannelImageOutputStreamSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.png.CLibPNGImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.wbmp.WBMPImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.bmp.BMPImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.pnm.PNMImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLibSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.wbmp.WBMPImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.bmp.BMPImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.pnm.PNMImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.raw.RawImageWriterSpi");
        registerIIO("com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi");
        registerIIO("com.sun.media.jai.imageioimpl.ImageReadWriteSpi");
    }

    private void registerIIO(String provider) {
        try {
            registry.registerServiceProvider(Class.forName(provider).newInstance());
        } catch (Throwable t) {
            LOG.error("Unable to register provider: " + provider, t);
        }
    }

}
