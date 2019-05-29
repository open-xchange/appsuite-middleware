---
title: Jolokia
icon: fas fa-pepper-hot
tags: Monitoring, Configuration, Administration
---

<!-- Once on FontAwesome 5.x change to 'fa-pepper-hot' icon -->

# Install Open-Xchange

Open-Xchange does support Jolokia as a remote JMX-Bridge over HTTP. See the [quick install guide](http://oxpedia.org/wiki/index.php?title=AppSuite:Main_Page_AppSuite#quickinstall) if you don't have Open-Xchange installed yet. Jolokia is part of the base product, no extra packages are needed. Additional information can be found [here](http://www.jolokia.org/).

# Enable Jolokia

The jolokia properties are documented [here](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=features&feature=Jolokia).

In `etc/jolokia.properties`, enable Jolokia by setting the following properties:

```properties
com.openexchange.jolokia.start = true
com.openexchange.jolokia.user = youruser
com.openexchange.jolokia.password = yourpassword
```
Note that Jolokia will not be enabled when no user/password is set.

When using munin scripts with Jolokia, this user and password also need to be changed.

You can optionally adjust this setting:

```properties
com.openexchange.jolokia.servlet.name = /monitoring/jolokia
```
If you do, you need to adjust the examples below as well.

# Allow access from other hosts

This is an optional step in case you want to access the Jolokia interface from other hosts other than `localhost`. This may be very helpful during the development phase of a project. Please be aware that this interface exposes lots of "interesting" data, so if you remove the restriction to localhost, you need to ensure by other means (network setup, firewalls, web server configuration, ...) that no unauthorised access is possible on production systems.

In `etc/jolokia.properties`, set:

```properties
com.openexchange.jolokia.restrict.to.localhost = false
```
In your web server configuration, enable access to the Jolokia servlet. For Apache this is possible by adding a `ProxyPass` directive for each OX host in the cluster:

```apache
 ProxyPass /monitoring/ox1/jolokia http://ox1-ip:8009/monitoring/jolokia
 ProxyPass /monitoring/ox2/jolokia http://ox2-ip:8009/monitoring/jolokia
 ...
```
On a default installation as described by our installation guides, this would be in `proxy_http.conf`.

Reload your apache config and restart the `open-xchange` service for the changes to take effect.

## Running Jolokia

As Jolokia represents a JMX-Interface it is highly recommended not to forward it to the internet!

This is by default set through the use of `com.openexchange.jolokia.restrict.to.localhost = true` and can be changed by either setting it to false or providing a `jolokia-access.xml` inside `/opt/open-xchange/etc/`

For further information how to setup this file, http://www.jolokia.org/reference/html/security.html is a good start as all those settings are usable.

## Jolokia with Grizzly

When using Grizzly and munin scripts on the same machine, you can connect to jolokia directly with the servers address, e.g.: `http://localhost:8009/monitoring/jolokia`. When connecting through another machine, a best practise is to use the same forwarding as described below.

## Jolokia with Munin Scripts

The package `open-xchange-munin-scripts`, which is part of the `backend` software repository, must be installed.

Munin-scripts for Jolokia need an additional perl modules json and lwp, which are set inside the depenecies. Those should be installed automaticly.

If not, please install the following modules:

Debian:

* libwww-perl
* libjson-perl

rpm:

* perl-JSON
* perl-libwww-perl

### Supplementary notes for distributed munin clients

#### Munin node

The default munin node configuration only allows connections from localhost, which means that munin master has to run on the same host. If there already is a munin master running, that master IP has to be added to the file /etc/munin/munin-node.conf on each node:

```
\# A list of addresses that are allowed to connect.  This must be a
\# regular expression, since Net::Server does not understand CIDR-style
\# network notation unless the perl module Net::CIDR is installed.  You
\# may repeat the allow line as many times as you'd like

allow ^127\.0\.0\.1$
```

#### Munin master

Make sure the munin and apache packages are installed. If only localhost is going to be monitored, the default configuration is sufficient. Other munin nodes can be added in the ``/etc/munin/munin.conf`` file:

```
\# a simple host tree
[localhost.localdomain]
    address 127.0.0.1
    use_node_name yes
By default, the munin monitoring web page is only reached from localhost, other hosts or networks can be added in /etc/apache2/conf.d/munin:

Allow from localhost 127.0.0.0/8 ::1    # the default setting
Allow from 10.99.0.0/8                  # added network
```

The munin webpage is located at [http://yourhost_where_munin_is_running.org/munin](http://yourhost_where_munin_is_running.org/munin).

## Configuring for OX Documents / Documentconverter

OX Documents and Documentconverter monitoring can used by installing the additional packages ``open-xchange-documents-monitoring``. The Documentconverter uses a different port to access monitoring data. The corresponding ``oxJolokiaURL`` is has to be configured with an entry in the settings.

```
[ox_documentconverter*]
env.oxJolokiaUrl http://localhost:8008/monitoring/jolokia
```

See this [article](http://oxpedia.org/wiki/index.php?title=AppSuite:DocumentsMonitoring) for more details.

## Example

This section describes how to access information exposed through JMX by Open-Xchange with the Jolokia JMX-to-HTTP bridge, using "Login Counter" as an example.

On localhost, call:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/list > ox.json
```
If you enabled access from other hosts, you can also use a standard web browser. For example open the following URL in a browser:

```
http://<yourserver>/monitoring/ox1/jolokia/list
```
You'll be asked for user name and password through a standard HTTP authentication window.

## Access specific information

The `ox.json` file you created in the last step gives you a complete list on what data is available through this interface.

As an example, the "Login Counter" interface (which is also used by the 
`logincounter` command line tool) is described like this:

```json
"com.openexchange.reporting": {
  "name=Login Counter": {
    "desc": "Information on the management interface of the MBean",
    "op": {
      "getLastLoginTimeStamp": {
        "ret": "java.util.List",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "java.lang.String"
          }
        ]
      },
      "getNumberOfLogins": {
        "ret": "java.util.Map",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "boolean"
          },
          {
            "desc": "",
            "name": "p4",
            "type": "java.lang.String"
          }
        ]
      }
    }
  }
}
```

A more detailed documentation on how to use the Jolokia interface can be found [here](http://www.jolokia.org/reference/html/protocol.html). Furthermore, [here](http://www.jolokia.org/reference/html/protocol.html#serialization) you can find documentation of the list of datatypes that can be passed as arguments and received in return values.

More information about the parameters `p1`, `p2`, etc. can be found at the [source code](http://oxpedia.org/wiki/index.php?title=SourceCodeAccess).

In the example above, to get the number of logins in a specific timeframe and with a specific client, we need to call the method `getNumberOfLogins` with the parameters `startDate`, `endDate`, `aggregate` and `clientstring`. They correspond to the command line parameters of the `logincounter` command line tool as described [here](http://oxpedia.org/wiki/index.php?title=AppSuite:Logincounter).

In `curl` this call would look like this:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/
```
If you have enabled access to the Jolokia interface from other hosts, the same information can be viewed in any web browser:

```bash
http://<yourserver>/monitoring/ox1/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/