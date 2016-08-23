---
title: Cross-folder fulltext search with Dovecot
---

The mail search in OX App Suite can utilize fulltext and cross-folder search capabilities, as they are provided by Dovecot. This article aims to be a short walkthrough for setting up Dovecot and the OX App Suite middleware accordingly. I assume that you already have a working Dovecot installation that is used as the primary mail backend for your (again already existing and basically configured) OX App Suite installation. I further assume that you are running Dovecot in version 2.2.9 and OX App Suite 7.6.0 on Debian Wheezy. We will use [Solr](https://lucene.apache.org/solr/ "wikilink") as mail index.

# Dovecot

Both search features are realized via Dovecot plugins. Fulltext search relies on [FTS](http://wiki2.dovecot.org/Plugins/FTS "wikilink") and [FTS Solr](http://wiki2.dovecot.org/Plugins/FTS/Solr "wikilink"). Cross-folder search is realized via a virtual folder, that claims to contain all mails from all other folders. Debian Wheezy comes with Dovecot 2.1.7. As this version is quite old, we take the packages from the backports repository, which contains version 2.2.9. Nevertheless this should all work with 2.1.7 also. After adding the backports entries my `/etc/apt/sources.list` looks like this:

```
deb http://ftp2.de.debian.org/debian/ wheezy main contrib non-free
deb-src http://ftp2.de.debian.org/debian/ wheezy main contrib non-free

deb http://ftp2.de.debian.org/debian/ wheezy-updates main contrib non-free
deb-src http://ftp2.de.debian.org/debian/ wheezy-updates main contrib non-free

deb http://ftp2.de.debian.org/debian/ wheezy-backports main contrib non-free
deb-src http://ftp2.de.debian.org/debian/ wheezy-backports main contrib non-free
```

## Installing and configuring the fulltext index

We start with installing Solr. I use solr-jetty here because it's easy to configure and more leightweight than a full tomcat installation. Later we will use the Solr admin panel to see if everything works. The admin panel uses Java Server Pages (JSP), therefore we also need a JDK. We install both via

```
$ aptitude install solr-jetty openjdk-6-jdk
```

We also need Dovecots FTS Solr plugin. When installing Dovecot packages, we have to explicitly choose the backports repository or we get the original 2.1.7 packages. Install the plugin via

```
$ aptitude -t wheezy-backports install dovecot-solr
```

Before we can start Jetty, which in turn starts Solr as a web app, we have to configure it. The Solr admin panel refers to a shared jQuery library, that is linked into the web app. Therefore we have to allow the delivery of symlinks by Jetty. Open `/etc/jetty/webdefault.xml` and add the following to the `<servlet>` section:

```
<init-param>
    <param-name>aliases</param-name>
    <param-value>true</param-value>
</init-param>
```

Then open `/etc/default/jetty` and change `NO_START` to `0`. To access Solrs admin panel Jetty must accept connections from the outside. So you have to adjust `JETTY_HOST` accordingly. Further there is a problem with the softlink that links the solr webapp into Jetty. `/var/lib/jetty/webapps/solr` points to `/usr/share/solr/webapp`, what is wrong. The correct path is `/usr/share/solr/web`. We fix this with

```
$ rm /var/lib/jetty/webapps/solr
$ ln -s /usr/share/solr/web /var/lib/jetty/webapps/solr
```

You may also check the permissions for the jquery library. By default, it's a symlink at `/usr/share/solr/web/admin/jquery-1.4.3.min.js` which throws a 404 when visiting the solr admin panel. Examinate `/var/log/jetty/*stdout.log` for any Jetty related issues.

The productive use has shown to change these values in the `/etc/jetty/jetty.xml` to get jetty work with big mailboxes:

```
<Set name="maxIdleTime">100000</Set>
<Set name="headerBufferSize">65536</Set>
```

Now you can start Jetty with

```
$ service jetty start
```

If everything was done correctly, you should reach Solrs admin panel at `http://<ip or host>:8080/solr/admin`.

Now its time to activate the FTS Solr plugin in Dovecot and to make use of our freshly set up indexing server. Open `/etc/dovecot/conf.d/10-mail.conf` and change

```
#mail_plugins
```

to

```
mail_plugins = fts fts_solr
```

To configure the plugins open `/etc/dovecot/conf.d/90-plugin.conf` and change the plugin-section to

```
plugin {
 fts = solr
 fts_autoindex = yes
 fts_solr = url=http://localhost:8080/solr/
}
```

`fts_autoindex = yes` will enable automatic indexing of new mails. Dovecot 2.2.9 or newer is required by this feature.

Solr uses a so called schema that defines the structure of the index. A schema is defined in an XML file. The schema for Dovecot is provided by the dovecot-solr package and can be found at `/usr/share/doc/dovecot-core/dovecot/solr-schema.xml`. We have to replace Solrs default schema with this one:

```
$ cp /usr/share/doc/dovecot-core/dovecot/solr-schema.xml /etc/solr/conf/schema.xml
```

Afterwards we restart Jetty and Dovecot to apply the changes.

```
$ service jetty restart
$ service dovecot restart
```

Now we can start indexing some mails, to see if everything works. According to [the FTS-Solr manual](http://wiki2.dovecot.org/Plugins/FTS/Solr "wikilink") we can index a mailbox with

```
$ doveadm fts rescan -u <user>
$ doveadm index -u <user> '*'
```

The first call instructs Dovecot to remember that a rebuild of all indexes for the given user is necessary. The second one then forces the rebuild-operation and blocks until all mails are indexed. We use * as a wildcard for all mailboxes of the given user here. `http://<ip or host>:8080/solr/admin/schema.jsp` should now show the `numDocs` value as equal to the number of mails contained in the mailbox.

Congratulations, searching within mail bodies now utilizes Solr and is blazing fast!

## Configuring the all-messages folder

To enable cross-folder search we configure Dovecot to add a special folder to every mailbox. From the outside the folder looks like it contains all mails from all other folders. Some more information can be found [here](http://wiki2.dovecot.org/Plugins/Virtual "wikilink").
Add the following to your Dovecot configuration:

```
mail_plugins = $mail_plugins virtual
namespace Virtual {
  prefix = Virtual/
  separator = /
  hidden = yes
  list = no
  subscriptions = no
  location = virtual:/etc/dovecot/virtual:INDEX=/var/vmail/%u/virtual
}
```

This makes use of the dovecot virtual folders plugin. A new hidden namespace `Virtual` is created, which will not be contained in IMAP `LIST` responses and not accept subscriptions. However, folders below that namespace can be selected and examined. In our case we define a global configuration for virtual folders below `/etc/dovecot/virtual`, which makes configured folders appear in every users account. However, indexes for such folders need to be created per-mailbox of course, which we expect to be located under `/var/vmail/`.

To create a virtual folder, a file system folder carrying the target name needs to be created below the denoted path. In our case we create a directory `/etc/dovecot/virtual/All`. Folder owner of the `virtual`and `virtual/All` folders needs to be the system user running the `dovecot` process. In our case it's `vmail`.

```
mkdir -p /etc/dovecot/virtual/All
chown -R vmail:vmail /etc/dovecot/virtual
```

Now we need to create the virtual folders configuration. Create a new file `/etc/dovecot/virtual/All/dovecot-virtual` and open it in your favorite editor. E.g. you might decide to include all mails from all folders, but Trash and Spam.

```
*
-INBOX/Trash
-INBOX/Trash/*
-INBOX/Spam
-INBOX/Spam/*
  all
```

The file can be owned by `root` but must be readable by the user running the `dovecot` process.

As a result every mail account will contain a selectable mailbox `Virtual/All` which pretends to contain all messages from all other mailboxes but Trash and Spam (given that these are named like this and located below the `INBOX` anmespace with `/` as separator).

For App Suite to be able to display the original folders within the search results Dovecot needs to announce an additional capability which needs to be added via

`imap_capability = +XDOVECOT`


### Excursion: Make the all-messages folder visible

If you want to make the All-folder available within the folder tree, don't hide the `Virtual` namespace and enable listing. You should also think about more user-friendly names for the namespace and folder then. Furthermore you would probably want to mark the folder with the special-use flag `\All`. This can be done via adding the following to `/etc/dovecot/conf.d/15-mailboxes.conf`:

```
namespace Virtual {
 mailbox All {
   special_use = \All
 }
}
```


# OX App Suite

Open `/opt/open-xchange/etc/findbasic.properties` and configure

```
com.openexchange.find.basic.mail.allMessagesFolder = Virtual/All
```

Restart the server with

```
$ /etc/init.d/open-xchange restart
```

OX App Suite uses that folder now when starting a search from within the inbox:

![Cross-folder search](all_folder_search.png)
