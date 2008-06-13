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
package com.openexchange.admin.soap;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author choeger
 *
 */
public abstract class OXSOAPRMIMapper {

    /*
     * RMI connection attempts should only be done once at a time, because
     * else every Instance of OXSOAPRMIMapper does a (re)connect at the same
     * time which multiplies the time to wait for the remote end
     */
    protected static final Lock LOCK = new ReentrantLock(true);
    
    private final String MAX_RMI_CONNECT_ATTEMPTS_PROP = "MAX_RMI_CONNECT_ATTEMPTS";
    public int MAX_RMI_CONNECT_ATTEMPTS    = 5;

    /**
     * time in seconds to wait between connect attempts
     */
    private final String CONNECT_ATTEMPTS_DELAY_TIME_PROP = "CONNECT_ATTEMPTS_DELAY_TIME";
    public int CONNECT_ATTEMPTS_DELAY_TIME = 5;

    private final String RMI_HOSTNAME_PROP = "RMI_HOSTNAME";
    public String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    protected Object rmistub = null;
    

    private Class<?> clazz = null;
    
    protected static final Log log = LogFactory.getLog(OXSOAPRMIMapper.class);    

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

            Properties props = new Properties();
            props.load(new FileInputStream(configFile));
            if( props.containsKey(MAX_RMI_CONNECT_ATTEMPTS_PROP) ) {
                MAX_RMI_CONNECT_ATTEMPTS = Integer.parseInt((String)props.get(MAX_RMI_CONNECT_ATTEMPTS_PROP));
            }
            if( props.containsKey(CONNECT_ATTEMPTS_DELAY_TIME_PROP) ) {
                CONNECT_ATTEMPTS_DELAY_TIME = Integer.parseInt((String)props.get(CONNECT_ATTEMPTS_DELAY_TIME_PROP));
            }
            if( props.containsKey(RMI_HOSTNAME_PROP) ) {
                RMI_HOSTNAME = (String)props.get(RMI_HOSTNAME_PROP);
            }
            if( log.isDebugEnabled() ) {
                log.debug("OXSOAPRMIMapper settings:");
                log.debug("MAX_RMI_CONNECT_ATTEMPTS: " + MAX_RMI_CONNECT_ATTEMPTS);
                log.debug("CONNECT_ATTEMPTS_DELAY_TIME: " + CONNECT_ATTEMPTS_DELAY_TIME);
                log.debug("RMI_HOSTNAME: " + RMI_HOSTNAME);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @param force
     * @throws RemoteException
     */
    protected void reconnect(final boolean force) throws RemoteException {

        if( ! LOCK.tryLock() ) {
            throw new RemoteException("failed to reconnect to RMI port of admin daemon");
        }

        try {
            if( rmistub == null || force ) {
                final String rmihost = RMI_HOSTNAME + clazz.getDeclaredField("RMI_NAME").get(this);
                log.info("reconnecting to " + rmihost);
                boolean doloop = true;
                int count = MAX_RMI_CONNECT_ATTEMPTS;
                int delay = CONNECT_ATTEMPTS_DELAY_TIME*1000;
                boolean failed = false;
                while(doloop) {
                    try {
                        rmistub = Naming.lookup(rmihost);
                        doloop = false;
                    } catch (java.rmi.ConnectException e) {
                        rmistub = null;
                        log.info("OXSOAPRMIMapper.reconnect: Connection problem");
                        log.info("waiting " + CONNECT_ATTEMPTS_DELAY_TIME + " seconds and try again");
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e1) {
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
                    throw new RemoteException("failed to reconnect to RMI port of admin daemon");
                }
                //clazz.cast(rmistub);
            }
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (NoSuchFieldException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (NotBoundException e) {
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
