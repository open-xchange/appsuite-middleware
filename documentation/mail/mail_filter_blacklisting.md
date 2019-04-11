---
title: Mail filter blacklisting
---

Typically sieve rules consists of three types of elements: tests, comparators and actions. By default the appsuite middleware provides the hole stack of available and implemented elements.
But this is not always the desired behaviour. Sometimes a provider wants to limit the amount of elements presented to the user. For example by limiting the available comparators for a given test to the most useful ones. This can potentionally lead to a reduced amount of service needed to help users with this feature (e.g. because the users regulary didn't understand the meaning of a particular test or comparator before). 

To support this scenario two new properties has been introduced which allows a detailed configuration of what is availble. Even in caser these properties are used the middleware is still able to parse blacklisted rules. E.g. because they are created before the properties are changed or in case they are created with another sieve client. 

Please note that this blacklist is only applicable for the new v2 mailfilter api and that invalid elements are ignored silently. So in case your configuration doesn't work please check the spelling first.


# Properties

**com.openexchange.mail.filter.blacklist.[base]**

Defines a comma separated list of elements. Each elements is either a test name, an action name or a comparison depending on the [base].

[base] must be one of:  
* tests
* actions
* comparisons

e.g.:
```
com.openexchange.mail.filter.blacklist.actions = keep, discard
```
With this configuration the actions `keep` and `discard` will not be shown to clients.


**com.openexchange.mail.filter.blacklist.[base].[element].[field]**

Defines a comma separated list of strings. Each string represents a value which should be blacklisted from the corresponding field of the element.

[base] must be one of:  
* tests
* actions

Currently only tests contains sub fields.

[element] must be a valid element of the base group. E.g. `address` for `tests` or `keep` for `actions`.

[field] must be a valid field of the element. E.g. *comparisons*

e.g.:
```
com.openexchange.mail.filter.blacklist.tests.from.comparisons = is
```

This will blacklist the `is` comparator from the `from` action.
