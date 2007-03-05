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

package com.openexchange.admin.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.exceptions.ContextException;
import com.openexchange.admin.exceptions.ContextExceptionFactory;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.jobs.I_AdminProgressEnabledJob;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXContext;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;

/**
 *
 * @author <a href="mailto:sebastian.kotyrba@open-xchange.com">Koty</a>
 *
 */
@OXExceptionSource(
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXCONTEXT,
        component=Component.ADMIN_CONTEXT
        )
public class FilestoreDataMover implements Callable<Vector>, I_AdminProgressEnabledJob {
    
    
    static ContextExceptionFactory CONTEXT_EXCEPTIONS = new ContextExceptionFactory(OXContext.class);
    
    private Log log = LogFactory.getLog( this.getClass() );
    private Process proc;
    private String src = null;
    private String dst = null;
    private int context_id = -1;
    private int dstStore_id = -1;
    
    /**
     *
     */
    public FilestoreDataMover(String src, String dst, int context_id, int dstStore_id) {
        this.src= src;
        this.dst= dst;
        this.context_id= context_id;
        this.dstStore_id= dstStore_id;
    }
    
    /**
     *
     * get Size as long (bytes) from the source dir
     *
     * @param source
     * @return
     */
    public long getSize( String source ) {
        String[] cmd = { "/opt/open-xchange/libexec/getSize.sh", source };
        Vector v = execute( cmd );
        long alles_size = 0;
        if ( v.size() > 1 && v.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
            Vector vs = (Vector)v.get( 1 );
            String s = vs.get( 0 ).toString();
            alles_size = Long.valueOf( s );
        } else {
            alles_size = -1;
        }
        return alles_size;
    }
    
    
    
    /**
     *
     * get the list of files to copy from the source dir
     *
     * @param source
     * @return
     */
    public Vector<String> getFileList( String source ) {
        String[] cmd = { "/opt/open-xchange/libexec/getFileList.sh", source };
        Vector v = execute( cmd );
        Vector<String> f_list = new Vector<String>();
        if ( v.size() > 1 && v.get( 0 ).toString().equalsIgnoreCase( "OK" ) ) {
            Vector vs = (Vector)v.get( 1 );
            for ( int i = 0; i < vs.size(); i++ ) {
                String s = vs.get( i ).toString();
                f_list.add( parseFileOut( s ) );
            }
        }
        return f_list;
    }
    
    
    
    private String parseFileOut( String logLine ) {
        // 1.) remove starting '.'
        if ( logLine.startsWith( "." ) ) {
            logLine = logLine.substring( 1 );
        }
        
        // 2.) parse filename
        String f_name = logLine.substring( 0, logLine.indexOf( "[" ) );
        f_name = f_name.trim();
        
        return f_name;
    }
    
    
    
//    private long parseSizeOut( String logLine, String source ) {
//        // 1.) remove starting '.'
//        if ( logLine.startsWith( "." ) ) {
//            logLine = logLine.substring( 1 );
//        }
//
//        // 2.) parse filename
//        String f_name = logLine.substring( 0, logLine.indexOf( "[" ) );
//        f_name = f_name.trim();
//
//        // 3.) parse size
//        long l_size = Long.valueOf( logLine.substring( logLine.indexOf( "[" ) + 1, logLine.indexOf( "]" ) ) );
//        File f = new File( source + f_name );
//        /*
//        if ( !f.isDirectory() ) {
//            ll = ll + l_size;
//        } else {
//            alles_size = alles_size - l_size;
//        }
//        */
//        return l_size;
//    }
    
    
    
    //    private long parseSizeOut( String logLine, String source ) {
    //        // 1.) remove starting '.'
    //        if ( logLine.startsWith( "." ) ) {
    //            logLine = logLine.substring( 1 );
    //        }
    //
    //        // 2.) parse filename
    //        String f_name = logLine.substring( 0, logLine.indexOf( "[" ) );
    //        f_name = f_name.trim();
    //
    //        // 3.) parse size
    //        long l_size = Long.valueOf( logLine.substring( logLine.indexOf( "[" ) + 1, logLine.indexOf( "]" ) ) );
    //        File f = new File( source + f_name );
    //        /*
    //        if ( !f.isDirectory() ) {
    //            ll = ll + l_size;
    //        } else {
    //            alles_size = alles_size - l_size;
    //        }
    //        */
    //        return l_size;
    //    }
        
        
        
        /**
         *
         * start the copy (rsync)
         *
         * @param source
         * @param dest
         * @param log_file
         */
        @OXThrowsMultiple(
        category={Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR},
                desc={" "," "},
                exceptionId={0,1},
                msg={"Unable to change Storage data in configdb: %s","Unable to enable Context %s"}
        )
        public void copynew(String source, String dest, String log_file) {
            // TODO: d7 exceptions has to be thrown when something went's wrong
            File srcFile = new File(source);
            if (srcFile.exists()) {
                // if context store does not yet exist, which might be possible,
                // just change the configdb
                String[] cmd = { "/opt/open-xchange/libexec/sync.sh", source, dest, log_file };
                executenew(cmd);
            }
            try {
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                final Filestore filestore = new Filestore(dstStore_id);
                final Context ctx = new Context(context_id);
                
                try {
                    oxcox.changeStorageData(ctx, filestore);
                } catch (StorageException e) {
                    throw CONTEXT_EXCEPTIONS.create(0, context_id);
                }
                try {
                    oxcox.enable(ctx);
                } catch (StorageException e) {
                    throw CONTEXT_EXCEPTIONS.create(1, context_id);
                }
                if (srcFile.exists()) {
                    AdminDaemonTools.deleteDirectory(source);
                }
//            } catch (RemoteException e) {
//                log.error("Error copying filestore", e);
//                ret.add("ERROR");
//                ret.add("" + e.getMessage());
            } catch (ContextException e) {
                log.error("Error copying filestore", e);
            } catch (StorageException e) {
                log.error("Error copying filestore", e);
            }
        }

    /**
     *
     * start the copy (rsync)
     *
     * @param source
     * @param dest
     * @param log_file
     */
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR},
            desc={" "," "},
            exceptionId={0,1},
            msg={"Unable to change Storage data in configdb: %s","Unable to enable Context %s"}
    )
    public Vector copy(String source, String dest) {
        Vector<Object> ret = new Vector<Object>();
        File srcFile = new File(source);
        if (srcFile.exists()) {
            // if context store does not yet exist, which might be possible,
            // just change the configdb
            String[] cmd = { "/opt/open-xchange/libexec/sync.sh", source, dest };
            ret = execute(cmd);
        } else {
            ret.add("OK");
        }
        if (ret.get(0).equals("OK")) {
            try {
                // FIXME: Use new context implementation
//                com.openexchange.admin.dataSource.impl.OXContext oxcox = new com.openexchange.admin.dataSource.impl.OXContext();
                Hashtable<String, String> fh = new Hashtable<String, String>();
//                fh.put(I_OXContext.CONTEXT_FILESTORE_ID, "" + dstStore_id);
//                ret = oxcox.changeStorageData(context_id, fh);
                if (!ret.get(0).equals("OK")) {
                    throw CONTEXT_EXCEPTIONS.create(0, context_id);
                }
//                ret = oxcox.enableContext(context_id);
                if (!ret.get(0).equals("OK")) {
                    throw CONTEXT_EXCEPTIONS.create(1, context_id);
                }
                if (srcFile.exists()) {
                    AdminDaemonTools.deleteDirectory(source);
                }
                ret.clear();
                ret.add("OK");
                ret.add("Successfully moved");
//            } catch (RemoteException e) {
//                log.error("Error copying filestore", e);
//                ret.add("ERROR");
//                ret.add("" + e.getMessage());
            } catch (ContextException e) {
                log.error("Error copying filestore", e);
                ret.add("ERROR");
                ret.add("" + e.getMessage());
            }
        }

        return ret;
    }
    
    
    
    /**
     * 
     * @param command
     * @return
     */
    private Vector<Object> execute( String command[] ) {
        Vector<Object> retval = new Vector<Object>();
        log.debug( "Load execute : " + command );
        BufferedReader buf = null;
        
        InputStream is = null;
        try {
            
            proc = Runtime.getRuntime().exec( command );
            String readBuffer = null;
            
            is = proc.getInputStream();
            int c = is.read();
            if ( c != -1 ) {
                retval.add( "OK" );
                buf = new BufferedReader( new InputStreamReader( is ) );
                while ( ( readBuffer = buf.readLine() ) != null ) {
                    retval.add( readBuffer );
                }
            } else {
                proc.waitFor();
                if ( proc.exitValue() != 0 ) {
                    retval.add( "ERROR" );
                    is = proc.getErrorStream();
                    c = is.read();
                    if ( c != -1 ) {
                        buf = new BufferedReader( new InputStreamReader( is ) );
                        while ( ( readBuffer = buf.readLine() ) != null ) {
                            if ( retval.size() <= 1 ) {
                                readBuffer = (char)c + readBuffer;
                            }
                            retval.add( readBuffer );
                        }
                    }
                } else {
                    retval.add( "OK" );
                }
            }
        } catch ( Exception e ) {
            Vector<Object> errorval = new Vector<Object>();
            errorval.add( "ERROR" );
            errorval.add( e.toString() );
            errorval.addAll( retval );
            retval.clear();
            log.fatal( "[FilestoreDataMover.execute]", e );
            retval = errorval;
        } finally {
            
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    is = null;
                }
            }
            
            if ( buf != null ) {
                try {
                    buf.close();
                } catch ( IOException e ) {
                    buf = null;
                }
            }
            
            if ( proc != null ) {
                proc.destroy();
            }
            log.debug( "SCRIPT_EXECUTE_COMMAND: " + Arrays.asList( command ) );
            log.debug( "SCRIPT_EXECUTE_RETURN: " + retval );
        }
        
        return retval;
    }

    /**
     * 
     * @param command
     * @return
     */
    private void executenew( String command[] ) {
        // TODO: d7 rewrite
        log.debug( "Load execute : " + command );
        BufferedReader buf = null;
        
        InputStream is = null;
        try {
            proc = Runtime.getRuntime().exec( command );
            
            proc.waitFor();
            if ( proc.exitValue() != 0 ) {
                is = proc.getErrorStream();
            }
        } catch ( IllegalThreadStateException e ) {
            log.fatal( "[FilestoreDataMover.execute]", e );
        } catch (IOException e) {
            log.fatal( "[FilestoreDataMover.execute]", e );
        } catch (InterruptedException e) {
            log.fatal( "[FilestoreDataMover.execute]", e );
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    is = null;
                }
            }
            
            if ( buf != null ) {
                try {
                    buf.close();
                } catch ( IOException e ) {
                    buf = null;
                }
            }
            
            if ( proc != null ) {
                proc.destroy();
            }
            log.debug( "SCRIPT_EXECUTE_COMMAND: " + Arrays.asList( command ) );
        }
    }
    
    
    
    /**
     *
     * starting the thread
     *
     */
    public Vector call() {
        return copy(src, dst);
    }
    
    /**
     * from I_AdminProgressEnabledJob
     */
    public int getPercentDone() {
        // TODO Auto-generated method stub
        return 0;
    }
}