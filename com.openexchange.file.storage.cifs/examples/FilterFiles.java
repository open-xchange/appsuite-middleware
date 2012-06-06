import jcifs.smb.DosFileFilter;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFilenameFilter;

public class FilterFiles {

    static class ShortFilenameFilter implements SmbFilenameFilter {
        @Override
        public boolean accept( SmbFile dir, String name ) throws SmbException {
            return name.length() < 14;
        }
    }
    static class BigFileFilter implements SmbFileFilter {
        @Override
        public boolean accept( SmbFile file ) throws SmbException {
            return file.length() > 0x1FFFFL;
        }
    }

    public static void main( String[] argv ) throws Exception {

        SmbFile file = new SmbFile( argv[0] );
        BigFileFilter filter = new BigFileFilter();
        ShortFilenameFilter sfilter = new ShortFilenameFilter();
        DosFileFilter everything = new DosFileFilter( "*", 0xFFFF );

        long t1 = System.currentTimeMillis();
        SmbFile[] files = file.listFiles( everything );
        long t2 = System.currentTimeMillis() - t1;

        for( int i = 0; i < files.length; i++ ) {
            System.out.print( " " + files[i].getName() );
        }
        System.out.println();
        System.out.println( files.length + " files in " + t2 + "ms" );
    }
}

