import jcifs.smb.ACE;
import jcifs.smb.SmbFile;

public class GetShareSecurity {

    public static void main( String[] argv ) throws Exception {
        SmbFile file = new SmbFile( argv[0] );
        ACE[] security;

        security = file.getShareSecurity(true);
        System.out.println("Share Permissions:");
        for (int ai = 0; ai < security.length; ai++) {
            System.out.println(security[ai].toString());
        }
        System.out.println("Security:");
        security = file.getSecurity(true);
        for (int ai = 0; ai < security.length; ai++) {
            System.out.println(security[ai].toString());
        }
    }
}
