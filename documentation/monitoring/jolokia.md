---
title: Jolokia
---

# How to interact with Jolokia for Open-

Open-Xchange does support Jolokia as a remote JMX-Bridge over HTTP.

By Version 7.4.0 ongoing, it is located inside Open-Xchange Bundle and configured by `jolokia.properties`.

Additional information can be found [here](http://www.jolokia.org/).

## jolokia.properties

The jolokia properties are documented [here](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=features&feature=Jolokia).

Keep in mind that Jolokia will not start unless you set `com.openexchange.jolokia.start = true` , `com.openexchange.jolokia.user = yourUser` and to `com.openexchange.jolokia.password = yourPassword`.

When using Munin-Scripts with Jolokia, this user and password also need to be changed.

## Running Jolokia

As Jolokia represents a JMX-Interface it is highly recommended not to forward it to the internet!

This is by default set through the use of `com.openexchange.jolokia.restrict.to.localhost = true` and can be changed by either setting it to false or providing a `jolokia-access.xml` inside `/opt/open-xchange/etc/`

For further information how to setup this file, http://www.jolokia.org/reference/html/security.html is a good start as all those settings are usable.

### Jolokia with Grizzly

When using Grizzly and munin scripts on the same machine, you can connect to jolokia directly with the servers address, e.g.: `http://localhost:8009/monitoring/jolokia`. When connecting through another machine, a best practise is to use the same forwarding as described below.

## Example

A more detailed example can be found [here](login_counter_howto.html)