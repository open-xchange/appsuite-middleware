
package javax.imageio.spi.osgi;

import javax.imageio.spi.IIORegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends HousekeepingActivator {

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
        registry.registerServiceProvider(new com.sun.media.imageioimpl.stream.ChannelImageInputStreamSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.stream.ChannelImageOutputStreamSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.png.CLibPNGImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.wbmp.WBMPImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.bmp.BMPImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.pnm.PNMImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLibSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.wbmp.WBMPImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.bmp.BMPImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.pnm.PNMImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.raw.RawImageWriterSpi());
        registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());
        // registry.registerServiceProvider(new com.sun.media.jai.imageioimpl.ImageReadWriteSpi());
    }

}
