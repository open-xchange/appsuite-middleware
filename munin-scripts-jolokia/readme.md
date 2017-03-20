#Installation guide for local setup

##Setup perl libraries

To run the munin scripts you have to install the following packages

```
sudo apt-get install libwww-perl
sudo apt-get install libjson-perl
```

##Set environment variables

You also have to set environment variables to your system. These need to be consistent to "jolokia.properties"

```
oxJolokiaUrl=http://localhost:8009/monitoring/jolokia
export oxJolokiaUrl

oxJolokiaUser=admin
export oxJolokiaUser

oxJolokiaPassword=secret
export oxJolokiaPassword
```

Now you should be able to run the munin scripts