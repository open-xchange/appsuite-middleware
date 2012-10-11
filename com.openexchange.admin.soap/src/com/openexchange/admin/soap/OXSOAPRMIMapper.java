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
package com.openexchange.admin.soap;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.commons.logging.Log;

/**
 * @author choeger
 *
 */
public abstract class OXSOAPRMIMapper {

    private static final String THREAD_INTERRUPTED_ERROR = "Thread was interrupted while trying to get lock";

    private static final String CRLF = "\r\n";
    
    /*
     * RMI connection attempts should only be done once at a time, because
     * else every Instance of OXSOAPRMIMapper does a (re)connect at the same
     * time which multiplies the time to wait for the remote end
     */
    protected static final Lock LOCK = new ReentrantLock(true);
    
    protected final String RMI_CONNECT_ERROR = "failed to reconnect to RMI port of admin daemon";
    
    private final String MAX_RMI_CONNECT_ATTEMPTS_PROP = "MAX_RMI_CONNECT_ATTEMPTS";
    public int MAX_RMI_CONNECT_ATTEMPTS    = 2;

    /**
     * time in seconds to wait between connect attempts
     */
    private final String CONNECT_ATTEMPTS_DELAY_TIME_PROP = "CONNECT_ATTEMPTS_DELAY_TIME";
    public int CONNECT_ATTEMPTS_DELAY_TIME = 1;

    /**
     * time in seconds to wait for a lock 
     */
    private final String LOCK_WAIT_TIME_PROP = "LOCK_WAIT_TIME";
    public int LOCK_WAIT_TIME = 10;
    
    private final String RMI_HOSTNAME_PROP = "RMI_HOSTNAME";
    public String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    protected Object rmistub = null;
    

    private Class<?> clazz = null;
    
    protected static final Log log = org.apache.commons.logging.LogFactory.getLog(OXSOAPRMIMapper.class);    

    /**
     * @throws RemoteException 
     * 
     */
    public OXSOAPRMIMapper(final Class<?> clazz) throws RemoteException{
        this.clazz = clazz;
        
        final String classContainer = this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        final URL manifestUrl;
        try {
            manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            final Manifest manifest = new Manifest(manifestUrl.openStream());
            final Attributes attrs = manifest.getMainAttributes();
            final String configFile = attrs.getValue("Config-File");

            final Properties props = new Properties();
            props.load(new FileInputStream(configFile));
            if( props.containsKey(MAX_RMI_CONNECT_ATTEMPTS_PROP) ) {
                MAX_RMI_CONNECT_ATTEMPTS = Integer.parseInt((String)props.get(MAX_RMI_CONNECT_ATTEMPTS_PROP));
            }
            if( props.containsKey(CONNECT_ATTEMPTS_DELAY_TIME_PROP) ) {
                CONNECT_ATTEMPTS_DELAY_TIME = Integer.parseInt((String)props.get(CONNECT_ATTEMPTS_DELAY_TIME_PROP));
            }
            if( props.containsKey(LOCK_WAIT_TIME_PROP) && null != props.get(LOCK_WAIT_TIME_PROP)) {
                LOCK_WAIT_TIME = Integer.parseInt((String)props.get(LOCK_WAIT_TIME_PROP));
            } else {
                LOCK_WAIT_TIME = 10;
            }
            if( props.containsKey(RMI_HOSTNAME_PROP) ) {
                RMI_HOSTNAME = (String)props.get(RMI_HOSTNAME_PROP);
            }
            if( log.isDebugEnabled() ) {
                final StringBuilder sb = new StringBuilder();
                sb.append("OXSOAPRMIMapper settings:");
                sb.append(CRLF);
                sb.append("MAX_RMI_CONNECT_ATTEMPTS: ");
                sb.append(MAX_RMI_CONNECT_ATTEMPTS);
                sb.append(CRLF);
                sb.append("CONNECT_ATTEMPTS_DELAY_TIME: ");
                sb.append(CONNECT_ATTEMPTS_DELAY_TIME);
                sb.append(CRLF);
                sb.append("LOCK WAIT TIME: ");
                sb.append(LOCK_WAIT_TIME);
                sb.append(CRLF);
                sb.append("RMI_HOSTNAME: ");
                sb.append(RMI_HOSTNAME);
                log.debug(sb.toString());
            }
        } catch (final MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @param force
     * @throws RemoteException
     */
    protected void reconnect(final boolean force) throws RemoteException {
        
        try {
            if( ! LOCK.tryLock(LOCK_WAIT_TIME, TimeUnit.SECONDS) ) {
                throw new RemoteException("Could get lock within " + LOCK_WAIT_TIME + " seconds");
            }
        } catch (final InterruptedException e2) {
            throw new RemoteException(THREAD_INTERRUPTED_ERROR);
        }

        try {
            if( rmistub == null || force ) {
                final String rmihost = RMI_HOSTNAME + clazz.getDeclaredField("RMI_NAME").get(this);
                log.info("reconnecting to " + rmihost);
                boolean doloop = true;
                int count = MAX_RMI_CONNECT_ATTEMPTS;
                final int delay = CONNECT_ATTEMPTS_DELAY_TIME*1000;
                boolean failed = false;
                while(doloop) {
                    try {
                        rmistub = Naming.lookup(rmihost);
                        doloop = false;
                    } catch (final java.rmi.ConnectException e) {
                        rmistub = null;
                        log.info("OXSOAPRMIMapper.reconnect: Connection problem");
                        log.info("waiting " + CONNECT_ATTEMPTS_DELAY_TIME + " seconds and try again");
                        try {
                            Thread.sleep(delay);
                        } catch (final InterruptedException e1) {
                            log.error(e1.getMessage(),e1);
                        }
                        count--;
                        if( count == 0 ) {
                            doloop = false;
                            failed = true;
                        }
                    }
                }
                if( failed ) {
                    throw new RemoteException(RMI_CONNECT_ERROR);
                }
                //clazz.cast(rmistub);
            }
        } catch (final SecurityException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final NoSuchFieldException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (final NotBoundException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } finally {
            LOCK.unlock();
        }
    }
    
    /**
     * @throws RemoteException
     */
    protected void reconnect() throws RemoteException {
        reconnect(false);
    }
}
