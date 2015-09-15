Do not just replace this patched version of ical4j with a newly release version until
https://sourceforge.net/p/ical4j/bugs/164/ is fixed. The patch can be found in file
ical4j-cachingFix.diff.

Marcus Klein

Another one that needs to be incorporated when upgrading the ical4j library is https://sourceforge.net/p/ical4j/bugs/165/ . 
This needs the additional tz.alias file in the resource folder.

* For https://sourceforge.net/p/ical4j/bugs/172/ (OX bug #39098), see ical4j-#172.patch
