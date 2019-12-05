---
title: Custom Region Settings
icon: fa-language
tags: Locale
---

Up to version 7.10.2 it was not possible to use regional settings which are different from the used locale. For example a user couldn't use the 24h time format and also use en_US as the locale. This is now possible. The following settings can be overwritten:

|Field name| type   | description        | Example|
|:---------|:-------|:-------------------|:-------|
| time     | string | The time format    | HH:mm  |
| date     | string | The date format    | DD.MM.YYYY |
| number   | string | The number format  | 1.234,56 |
| firstDayOfWeek | string | The first day of the week | monday |
| firstDayOfYear | number | Minimal days required in the first week of the year | 4 |

These custom settings are accessible via the config tree under the path "localeData". Please note that only those settings are stored, which differ from the defaults of the currently selected locale. So for example it is possible to choose a specific time format which will be appplied independent of the selected locale. 

For example if a user configured the 24h time format the format would be used regardles of the configured locale. All other settings though would still depend on the selected locale.