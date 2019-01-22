---
title: Good to know
---

# Introduction
There are some peculiarities with regards to login and sessions that will be listed on this page.

## Exception code LGI-0016
There is a contract between server and client (currently web UI and devices) that when the server responds with exception code LGI-0016 the client will (and should) handle it like a HTTP 302. The error parameter will contain the URL the client should redirect to.

A possible response might look like the following JSON snippet:

```json
{
   "error":"https://mySuperSecureSiteThatDoesNotExist.com",
   "error_params":[
      "https://mySuperSecureSiteThatDoesNotExist.com"
   ],
   "categories":"USER_INPUT",
   "category":1,
   "code":"LGI-0016",
   "error_id":"1634303709-5",
   "error_desc":"https://mySuperSecureSiteThatDoesNotExist.com",
   "error_stack":[
      "https://mySuperSecureSiteThatDoesNotExist.com",
      "com.openexchange.exception.OXExceptionFactory.create(OXExceptionFactory.java:175)",
      "com.openexchange.exception.OXExceptionFactory.create(OXExceptionFactory.java:165)",
      "com.openexchange.exception.OXExceptionFactory.create(OXExceptionFactory.java:138)",
      "com.openexchange.authentication.LoginExceptionCodes.create(LoginExceptionCodes.java:256)",
      "com.openexchange.login.internal.MyLoginListener.onBeforeAuthentication(MyLoginListener.java:73)",
      "com.openexchange.login.internal.LoginPerformer.doLogin(LoginPerformer.java:202)",
      "com.openexchange.login.internal.LoginPerformer.doLogin(LoginPerformer.java:157)",
      "com.openexchange.login.internal.LoginPerformer.doLogin(LoginPerformer.java:145)",
      "com.openexchange.ajax.login.Login$1.doLogin(Login.java:109)",
      "com.openexchange.ajax.login.AbstractLoginRequestHandler.loginOperation(AbstractLoginRequestHandler.java:226)",
      "com.openexchange.ajax.login.AbstractLoginRequestHandler.loginOperation(AbstractLoginRequestHandler.java:184)",
      "com.openexchange.ajax.login.Login.doLogin(Login.java:97)",
      "com.openexchange.ajax.login.Login.handleRequest(Login.java:90)",
      "com.openexchange.ajax.LoginServlet.doJSONAuth(LoginServlet.java:798)",
      "com.openexchange.ajax.LoginServlet.doGet(LoginServlet.java:763)",
      "com.openexchange.ajax.LoginServlet.doPost(LoginServlet.java:883)",
      "javax.servlet.http.HttpServlet.service(HttpServlet.java:706)",
      "com.openexchange.ajax.AJAXServlet.doService(AJAXServlet.java:566)",
      "com.openexchange.ajax.LoginServlet.service(LoginServlet.java:743)",
      "javax.servlet.http.HttpServlet.service(HttpServlet.java:791)",
      "org.glassfish.grizzly.servlet.FilterChainImpl.doFilter(FilterChainImpl.java:148)",
      "com.openexchange.http.grizzly.servletfilter.RequestReportingFilter.doFilter(RequestReportingFilter.java:138)",
      "org.glassfish.grizzly.servlet.FilterChainImpl.doFilter(FilterChainImpl.java:138)",
      "com.openexchange.http.grizzly.servletfilter.WrappingFilter.doFilter(WrappingFilter.java:222)",
      "org.glassfish.grizzly.servlet.FilterChainImpl.doFilter(FilterChainImpl.java:138)",
      "com.openexchange.http.grizzly.service.http.OSGiAuthFilter.doFilter(OSGiAuthFilter.java:139)",
      "org.glassfish.grizzly.servlet.FilterChainImpl.doFilter(FilterChainImpl.java:138)",
      "org.glassfish.grizzly.servlet.FilterChainImpl.invokeFilterChain(FilterChainImpl.java:107)",
      "org.glassfish.grizzly.servlet.ServletHandler.doServletService(ServletHandler.java:226)",
      "org.glassfish.grizzly.servlet.ServletHandler.service(ServletHandler.java:178)",
      "com.openexchange.http.grizzly.service.http.OSGiMainHandler.service(OSGiMainHandler.java:301)",
      "org.glassfish.grizzly.http.server.HttpHandler$1.run(HttpHandler.java:224)",
      "com.openexchange.threadpool.internal.CustomThreadPoolExecutor$MDCProvidingRunnable.run(CustomThreadPoolExecutor.java:2575)",
      "com.openexchange.threadpool.internal.CustomThreadPoolExecutor$Worker.runTask(CustomThreadPoolExecutor.java:841)",
      "com.openexchange.threadpool.internal.CustomThreadPoolExecutor$Worker.run(CustomThreadPoolExecutor.java:868)",
      "java.lang.Thread.run(Thread.java:745)"
   ]
}
```

The exception LGI-0016 itself is only used as a workaround for a redirection, and is no real error!