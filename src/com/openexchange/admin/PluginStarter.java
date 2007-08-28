
package com.openexchange.admin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.exceptions.ConfigException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchPluginException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.PropertyHelper;
import com.openexchange.admin.tools.monitoring.MonitorAgent;

public class PluginStarter {
    private ClassLoader loader = null;
    private static Registry registry = null;
    private static Log log = LogFactory.getLog(PluginStarter.class);

    private static com.openexchange.admin.rmi.impl.OXContext oxctx_v2 = null;
    private static com.openexchange.admin.rmi.impl.OXUtil oxutil_v2 = null;

    private static MonitorAgent moni = null;

    public PluginStarter(final ClassLoader loader) {
        this.loader = loader;
    }

    public void start(final BundleContext context) throws RemoteException, AlreadyBoundException {
        try {
            Thread.currentThread().setContextClassLoader(loader);

            if (null == System.getSecurityManager()) {
                System.setSecurityManager(new SecurityManager() {
                    public void checkPermission(Permission perm) {
                    }

                    public void checkPermission(Permission perm, Object context) {
                    }
                });
            }
            registry = AdminDaemon.getRegistry();

            // Create all OLD Objects and bind export them
            oxctx_v2 = new com.openexchange.admin.rmi.impl.OXContext();
            OXContextInterface oxctx_stub_v2 = (OXContextInterface) UnicastRemoteObject.exportObject(oxctx_v2, 0);

            oxutil_v2 = new com.openexchange.admin.rmi.impl.OXUtil();
            OXUtilInterface oxutil_stub_v2 = (OXUtilInterface) UnicastRemoteObject.exportObject(oxutil_v2, 0);

            // bind all NEW Objects to registry
            registry.bind(OXContextInterface.RMI_NAME, oxctx_stub_v2);
            registry.bind(OXUtilInterface.RMI_NAME, oxutil_stub_v2);
            
            ClientAdminThread.cache.getProperties().registerPluginProperty(PropertyHelper.PLUGIN_NAME, "hosting.properties");

            startJMX();
            
            if (log.isDebugEnabled()) {
                log.debug("Loading context implementation: " + PropertyHelper.getString(PropertyHelper.CONTEXT_STORAGE, null));
                log.debug("Loading util implementation: " + PropertyHelper.getString(PropertyHelper.UTIL_STORAGE, null));
            }            
        } catch (final RemoteException e) {
            log.fatal(e.getMessage(), e);
            AdminDaemon.shutdown();
        } catch (final AlreadyBoundException e) {
            log.fatal(e.getMessage(), e);
            AdminDaemon.shutdown();
        } catch (final StorageException e) {
            log.fatal("Error while creating one instance for RMI interface", e);
            AdminDaemon.shutdown();
        } catch (final NoSuchPluginException e) {
            log.fatal(e.getMessage(), e);
            AdminDaemon.shutdown();
        } catch (final InvalidDataException e) {
            log.fatal("Error while reading an essential property from config file", e);
            AdminDaemon.shutdown();
        } catch (final ConfigException e) {
            log.fatal("Error while reading config file", e);
            AdminDaemon.shutdown();
        }
    }

    public void stop() throws AccessException, RemoteException, NotBoundException {
        try {
            stopJMX();
            if (null != registry) {
                registry.unbind(OXContextInterface.RMI_NAME);
                registry.unbind(OXUtilInterface.RMI_NAME);
            }
            ClientAdminThread.cache.getProperties().unregisterPluginProperty(PropertyHelper.PLUGIN_NAME);
        } catch (final AccessException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NotBoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }
    
    private void startJMX() throws NoSuchPluginException, InvalidDataException {
        int jmx_port = PropertyHelper.getInt(PropertyHelper.JMX_PORT, 9998);
        String addr = PropertyHelper.getString(PropertyHelper.JMX_BIND_ADDRESS,"localhost");
        InetAddress iaddr = null;
        if(!addr.trim().equals("0")){
            // bind only on specified interfaces
            try {
                iaddr = InetAddress.getByName(addr);
                
                moni = new MonitorAgent(jmx_port,iaddr);
                moni.start();
            } catch (UnknownHostException e) {
               log.error("Could NOT start start JMX monitor ",e);
            }
        }
        
        String servername = ClientAdminThread.cache.getProperties().getString(PropertyHandler.PropertyFiles.ADMIN, AdminProperties.Prop.SERVER_NAME, "local");
        log.info("Admindaemon Name: " + servername);
    }
    
    private void stopJMX() {
        moni.stop();
    }
}
