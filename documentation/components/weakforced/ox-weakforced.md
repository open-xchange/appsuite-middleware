---
title: Open-Xchange Weakforced Connector
---

# How it works
The Open-Xchange Weakforced Connector (OXWF) utilizes the login listener framework. Registered login listeners receive various call-backs during log-in process for a user:

 - Before authentication takes place

And then either of:

 - After successful authentication
 - After failed authentication
 - After redirected authentication (neither failed nor succeeded, but authentication is supposed to happen at another service). Not used by OXWF.

The OXWF itself registers such a login listener, which triggers the HTTP calls to Weakforced service. Currently used:

 - `allow`
 - `report`

## Before authentication
The OXWF login listener executes the `allow` call before authentication takes place to let Weakforced service check whether that login attempt is allowed or not, which is determined by providing a hash of login, password, remote address (either host name or IP address) and client identifier. The `allow` call responds with an integer result:

 - <code>0</code> (OK )
 - <code>-1</code> (BLOCKED)
 - A positive integer

 For an OK response, the login attempt is allowed to proceed as usual. The BLOCKED integer value leads to aborting the login attempt through throwing a LGI-0026 (LOGIN DENIED) error code. A positive integer indicates the number of seconds the client is supposed to wait until login attempt is allowed to procee. Hence, the executing thread gets halted for that amount of seconds.

## After successful authentication
A successful authentication attempt is `report`ed back to Weakforced service by OXWF login listener while providing a hash of login, password, remote address (either host name or IP address) and client identifier. That call-back may also be used to pass more attributes to Weakforced service in order to be validated; e.g. the login result may now contain certain information from LDAP (or any other authentication authority). If so a `allow` call is executed prior to the `report` request, while the status result gets examined as explained above (OK, BLOCKED, or positive integer).

## After failed authentication
A failed authentication attempt is `report`ed back to Weakforced service by providing a hash of login, password, remote address (either host name or IP address) and client identifier in order to be tracked for subsequent validations of login attempts from the same client/user.

# Installation
Deploying the OXWF simply requires to install the `open-xchange-weakforced` package. Once istalled the associated login listener gets registered and receives the mentioned call-backs during login requests.

# Configuration
After the package is deployed, an administrator is able to configure the OXWF through `/opt/open-xchange/etc/weakforced.properties` file.

## End-points
The OXWF accepts a listing of Weakforced HTTP end-points, whose entries are used in a round-robin fashion while tracking outages and re-availability of individual end-points. It is possible to specify generic end-points, which are used to fire `allow` and `report` calls. But it is also possible to specify end-points, which are used for either of those calls.

Each end-point listing also supports specifying the total number of connections to use, max. connections per route as well as read and connect timeout. If not configured default values are used:

- Total number of connections is 100
- Max. connection per route defaults to total number of connections divided by number of end-points in listing
- Read timeout defaults to 2500 milliseconds
- Connect timeout defaults to 1500 milliseconds

### Example

     com.openexchange.weakforced.endpoints=http://weakforced1.host.invalid:8081, http://weakforced2.host.invalid:8081
     com.openexchange.weakforced.endpoints.totalConnections=100
     com.openexchange.weakforced.endpoints.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)
     com.openexchange.weakforced.endpoints.readTimeout=2500
     com.openexchange.weakforced.endpoints.connectTimeout=1500
     ------------- For "report" call
     com.openexchange.weakforced.endpoints.report=http://weakforced1.reporthost.invalid:8081, http://weakforced2.reporthost.invalid:8081
     com.openexchange.weakforced.endpoints.report.totalConnections=100
     com.openexchange.weakforced.endpoints.report.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)
     com.openexchange.weakforced.endpoints.report.readTimeout=2500
     com.openexchange.weakforced.endpoints.report.connectTimeout=1500

This examples specifies two generic end-points and two end-points to use for `report` calls exclusively. Hence, the OXWF routes all `report` calls to either `http://weakforced1.reporthost.invalid:8081` or `http://weakforced2.reporthost.invalid:8081`. Remaining calls (only `allow` left) will use `http://weakforced1.host.invalid:8081` or`http://weakforced2.host.invalid:8081`.

## Secret
The `com.openexchange.weakforced.secret` option specifies the secret to use when calculating the hash of login, password, remote address (either host name or IP address) and client identifier acting as some sort of salt.

## Basic authentication
The `com.openexchange.weakforced.basic-auth.login` and `com.openexchange.weakforced.basic-auth.password` allow setting the user-name and password to use to perform HTTP basic authentication against Weakforced end-points. All end-points are expected to have the same HTTP basic authentication.

## Basic authentication
The `com.openexchange.weakforced.attributes` specifies a comma-separated list of arbitrary attributes that are supposed to read from session on successful authentication. Those attributes are then communicated to Weakforced using post-auth `allow` hook.

If no attributes specified or no single attribute available from session, no post-auth 'allow' takes place.
