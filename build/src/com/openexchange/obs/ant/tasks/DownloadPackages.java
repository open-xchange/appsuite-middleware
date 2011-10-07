/**
 * 
 */
package com.openexchange.obs.ant.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HttpException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.openexchange.obs.api.BuildServiceClient;
import com.openexchange.obs.api.BuildServiceException;

/**
 * @author choeger
 *
 */
public class DownloadPackages extends Task {

    private String bsprjname;
    
    private String bsreponame;

    private String bsuser;
    
    private String bspass;
    
    private String bsurl;
    
    private String bsarch;
    
    private String bspackage;
    
    private String bspkgpath;
    
    /**
     * @return the bspkgpath
     */
    public final String getBspkgpath() {
        return bspkgpath;
    }

    /**
     * @param bspkgpath the bspkgpath to set
     */
    public final void setBspkgpath(String bspkgpath) {
        this.bspkgpath = bspkgpath;
    }

    /**
     * @return the bsarch
     */
    public final String getBsarch() {
        return bsarch;
    }

    /**
     * @param bsarch the bsarch to set
     */
    public final void setBsarch(String bsarch) {
        this.bsarch = bsarch;
    }

    /**
     * @return the bspackage
     */
    public final String getBspackage() {
        return bspackage;
    }

    /**
     * @param bspackage the bspackage to set
     */
    public final void setBspackage(String bspackage) {
        this.bspackage = bspackage;
    }

    /**
     * @return the bsprjname
     */
    public final String getBsprjname() {
        return bsprjname;
    }

    /**
     * @param bsprjname the bsprjname to set
     */
    public final void setBsprjname(String bsprjname) {
        this.bsprjname = bsprjname;
    }

    /**
     * @return the bsreponame
     */
    public final String getBsreponame() {
        return bsreponame;
    }

    /**
     * @param bsreponame the bsreponame to set
     */
    public final void setBsreponame(String bsreponame) {
        this.bsreponame = bsreponame;
    }

    /**
     * @return the bsuser
     */
    public final String getBsuser() {
        return bsuser;
    }

    /**
     * @param bsuser the bsuser to set
     */
    public final void setBsuser(String bsuser) {
        this.bsuser = bsuser;
    }

    /**
     * @return the bspass
     */
    public final String getBspass() {
        return bspass;
    }

    /**
     * @param bspass the bspass to set
     */
    public final void setBspass(String bspass) {
        this.bspass = bspass;
    }

    /**
     * @return the bsurl
     */
    public final String getBsurl() {
        return bsurl;
    }

    /**
     * @param bsurl the bsurl to set
     */
    public final void setBsurl(String bsurl) {
        this.bsurl = bsurl;
    }


    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        
        // bsarch has a default
        if( this.bsarch == null || this.bsarch.length() == 0 ) {
            this.bsarch = "i586";
        }
        // bspkgpath has a default
        if( this.bspkgpath == null || this.bspkgpath.length() == 0 ) {
            this.bspkgpath = "./";
        }

        // check ant task arguments
        
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final String tname = f.getName();
                if( tname.startsWith("bs") ) {
                    final Object ob = f.get(this);
                    if( ob instanceof String ) {
                        if (ob == null || ((String)ob).length() == 0 ) {
                            throw new BuildException(tname + " is not set");
                        }
                    }
                }
            } catch (final IllegalArgumentException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            } catch (final IllegalAccessException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            }
        }
        
        BuildServiceClient bsc = new BuildServiceClient(bsuser, bspass, bsurl);
            try {
                Iterator<String> it = bsc.getProjectBinaryPackageNames(bsprjname, bsreponame, bspackage).iterator();
                while( it.hasNext() ) {
                    final String pkg = it.next();
                    System.out.println(pkg);
                    final File file = new File(bspkgpath+pkg);
                    log("downloading to " + file.getAbsolutePath(),Project.MSG_INFO);
                    if(file.exists()) {
                        file.delete();
                    }
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    final InputStream is = bsc.getProjectBinaryPackageByName(bsprjname, bsreponame, bspackage, pkg);
                    byte[] buf = new byte[512];
                    int length;
                    while ((length = is.read(buf)) != -1) {
                        bos.write(buf, 0, length);
                    }
                }
            } catch (XPathExpressionException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            } catch (HttpException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            } catch (IOException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            } catch (BuildServiceException e) {
                log(e.getMessage(), Project.MSG_ERR);
                throw new BuildException(e.getMessage(),e);
            }
    }
    
}
