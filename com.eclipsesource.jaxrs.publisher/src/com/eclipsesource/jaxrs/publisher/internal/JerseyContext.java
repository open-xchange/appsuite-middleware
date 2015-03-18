/*******************************************************************************
 * Copyright (c) 2012,2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Dragos Dascalita  - disbaled autodiscovery
 *    Lars Pfannenschmidt  - made WADL generation configurable
 *    Ivan Iliev - added ServletConfiguration handling
 ******************************************************************************/
package com.eclipsesource.jaxrs.publisher.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ServerProperties;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.eclipsesource.jaxrs.publisher.ApplicationConfiguration;
import com.eclipsesource.jaxrs.publisher.ServletConfiguration;


@SuppressWarnings( "rawtypes" )
public class JerseyContext {

  private final RootApplication application;
  private final HttpService httpService;
  private final String rootPath;
  private final ServletContainerBridge servletContainerBridge;
  private final ServletConfiguration servletConfigurationService;
  private final ApplicationConfiguration applicationConfiguration;
  private final ResourcePublisher resourcePublisher;
  private boolean isApplicationRegistered;
  
  public JerseyContext( HttpService httpService, String rootPath, long publishDelay ) {
    this( httpService, rootPath, publishDelay, null, null ); 
  }

  public JerseyContext( HttpService httpService,
                        String rootPath,
                        long publishDelay,
                        ApplicationConfiguration applicationConfiguration,
                        ServletConfiguration servletConfigurationService )
  {
    this.httpService = httpService;
    this.applicationConfiguration = applicationConfiguration;
    this.rootPath = rootPath == null ? "/services" : rootPath;
    this.application = new RootApplication();
    this.servletContainerBridge = new ServletContainerBridge( application );
    this.servletConfigurationService = servletConfigurationService;
    this.resourcePublisher = new ResourcePublisher( servletContainerBridge, publishDelay );
  }

  private void disableAutoDiscovery() {
    // don't look for implementations described by META-INF/services/*
    this.application.addProperty(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, false );
    // disable auto discovery on server, as it's handled via OSGI
    this.application.addProperty(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true );
  }

  public void addResource( Object resource ) {
    getRootApplication().addResource( resource );
    registerServletWhenNotAlreadyRegistered();
    resourcePublisher.schedulePublishing();
  }

  void registerServletWhenNotAlreadyRegistered() {
    if( !isApplicationRegistered ) {
      isApplicationRegistered = true;
      applicationConfiguration.configure(application);
      registerApplication();
    }
  }

  private void registerApplication() {
    ClassLoader loader = getContextClassloader();
    setContextClassloader();
    try {
      registerServlet();
    } catch( ServletException shouldNotHappen ) {
      throw new IllegalStateException( shouldNotHappen );
    } catch( NamespaceException shouldNotHappen ) {
      throw new IllegalStateException( shouldNotHappen );
    } finally {
      resetContextClassloader( loader );
    }
  }

  private ClassLoader getContextClassloader() {
    return Thread.currentThread().getContextClassLoader();
  }

  private void setContextClassloader() {
    Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
  }

  private void registerServlet() throws ServletException, NamespaceException {
    ClassLoader original = getContextClassloader();
    try {
      Thread.currentThread().setContextClassLoader( Application.class.getClassLoader() );
      httpService.registerServlet( rootPath, 
                                   servletContainerBridge.getServletContainer(), 
                                   getInitParams(), 
                                   getHttpContext() );
    } finally {
      resetContextClassloader( original );
    }
  }

  private Dictionary getInitParams() {
    if( servletConfigurationService != null ) {
      return servletConfigurationService.getInitParams( httpService, rootPath );
    }
    return null;
  }

  private HttpContext getHttpContext() {
    if( servletConfigurationService != null ) {
      return servletConfigurationService.getHttpContext( httpService, rootPath );
    }
    return null;
  }

  private void resetContextClassloader( ClassLoader loader ) {
    Thread.currentThread().setContextClassLoader( loader );
  }
  
  public void removeResource( Object resource ) {
    getRootApplication().removeResource( resource );
    unregisterServletWhenNoresourcePresents();
    resourcePublisher.schedulePublishing();
  }

  private void unregisterServletWhenNoresourcePresents() {
    if( !getRootApplication().hasResources() && isApplicationRegistered ) {
      // unregistering while jersey context is being reloaded can lead to many exceptions
      synchronized( servletContainerBridge ) {
        httpService.unregister( rootPath );
        servletContainerBridge.reset();
        resourcePublisher.cancelPublishing();
        isApplicationRegistered = false;
      }
    }
  }

  public List<Object> eliminate() {
    if( isApplicationRegistered ) {
      // unregistering while jersey context is being reloaded can lead to many exceptions
      synchronized( servletContainerBridge ) {
        try {
          // this should call destroy on our servlet container
          httpService.unregister( rootPath );
        } catch( Exception jerseyShutdownException ) {
          jerseyShutdownException.printStackTrace();
          // do nothing because jersey sometimes throws an exception during shutdown
        }
      }
      resourcePublisher.cancelPublishing();
    }
    return new ArrayList<Object>( getRootApplication().getResources() );
  }

  // For testing purpose
  RootApplication getRootApplication() {
    return application;
  }

}
