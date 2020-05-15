local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local gauge = grafana.gauge;
local row = grafana.row;
local prometheus = grafana.prometheus;
local table = grafana.tablePanel;
local template = grafana.template;

local dbPoolName = 'dbpool';
local davInterfaceName = 'davinterface';
local httpClient = 'httpclient';
local serviceName = 'service';

local templates = [
  {
    name: serviceName,
    label: 'Service',
    query: 'label_values(' + serviceName + ')',
    sort: 1,
  },
  {
    name: 'instance',
    label: 'Instance',
    query: 'label_values(up{job=~"$job",service=~"$' + serviceName + '"}, instance)',
    sort: 1,
  },
  {
    name: dbPoolName,
    label: 'DB Pool',
    hide: 'variable',
    includeAll: true,
    query: 'label_values(appsuite_mysql_connections_total,pool)',
    regex: '([0-9]+)',
    sort: 3,
  },
  {
    name: davInterfaceName,
    label: 'DAV Interface',
    hide: 'variable',
    includeAll: true,
    query: 'label_values(appsuite_webdav_requests_seconds_count,interface)',
    sort: 1,
  },
  {
    name: httpClient,
    label: 'HTTP Client',
    query: 'label_values(appsuite_httpclient_requests_seconds_count,client)',
    sort: 1,
  },
];

local sessions = [
  {
    title: 'Total',
    description: 'The number of total sessions.',
    target: {
      expr: 'appsuite_sessions_total{client="all", instance=~"$instance"}',
      legendFormat: 'Total',
    },
    gridPos: { h: 4, w: 3, x: 0, y: 1 },
  },
  {
    title: 'Active',
    description: 'The number of active sessions or in other words the number of sessions within the first two short term containers.',
    target: {
      expr: 'appsuite_sessions_active_total{client="all", instance=~"$instance"}',
      legendFormat: 'Active',
    },
    gridPos: { h: 4, w: 3, x: 3, y: 1 },
  },
  {
    title: 'Short Term',
    description: 'The number of sessions in the short term containers.',
    target: {
      expr: 'appsuite_sessions_short_term_total{client="all", instance=~"$instance"}',
      legendFormat: 'Short Term',
    },
    gridPos: { h: 4, w: 3, x: 0, y: 5 },
  },
  {
    title: 'Long Term',
    description: 'The number of sessions in the long term containers.',
    target: {
      expr: 'appsuite_sessions_long_term_total{client="all", instance=~"$instance"}',
      legendFormat: 'Long Term',
    },
    gridPos: { h: 4, w: 3, x: 3, y: 5 },
  },
];

local cacheRatio = [
  {
    title: 'Hit Ratio',
    expr: 'sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) / (sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) + sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])))',
    gridPos: { h: 4, w: 3, x: 0, y: 17 },
  },
  {
    title: 'Miss Ratio',
    expr: 'sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])) / (sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) + sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])))',
    gridPos: { h: 4, w: 3, x: 0, y: 21 },
  },
];

local genPercentileTargets(metric, labels=[], groupBy=[]) = [
  prometheus.target(
    'histogram_quantile(0.' + p + ', sum(rate(appsuite_' + metric + '_seconds_bucket{' + std.join(',', ['instance=~"$instance"'] + labels) + '}[$interval])) by (' + std.join(',', ['le', 'instance'] + groupBy) + '))',
    legendFormat='p' + p,
  )
  for p in [50, 75, 90, 95, 99, 999]
] + [
  prometheus.target(
    'sum(rate(appsuite_%s_seconds_sum{%s}[$interval]))/sum(rate(appsuite_%s_seconds_count{%s}[$interval]))' % [metric, std.join(',', ['instance=~"$instance"'] + labels), metric, std.join(',', ['instance=~"$instance"'] + labels)],
    legendFormat='avg',
  ),
  prometheus.target(
    'sum(appsuite_%s_seconds_max{%s})' % [metric, std.join(',', ['instance=~"$instance"'] + labels)],
    legendFormat='max',
  ),
];

local httpClientRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_httpclient_requests_seconds_count{instance=~"$instance",client="$httpclient"}[$interval])) by (client,instance)',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_httpclient_requests_seconds_count{instance=~"$instance",client="$httpclient",status!~"[45][0-9]{2}"}[$interval])) by (client,instance)',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_httpclient_requests_seconds_count{instance=~"$instance",client="$httpclient",status=~"[45][0-9]{2}"}[$interval])) by (client,instance)',
      legendFormat='KO',
    ),
  ]
);

local httpClientConnections = graphPanel.new(
  title='Connections',
  datasource=grafana.default.datasource,
  decimals=0,
  aliasColors={
    Max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_httpclient_connections_pending{instance=~"$instance",client=~"$httpclient"}',
      legendFormat='Pending',
    ),
    prometheus.target(
      'appsuite_httpclient_connections_available{instance=~"$instance",client=~"$httpclient"}',
      legendFormat='Available',
    ),
    prometheus.target(
      'appsuite_httpclient_connections_leased{instance=~"$instance",client=~"$httpclient"}',
      legendFormat='Leased',
    ),
    prometheus.target(
      'appsuite_httpclient_connections_max{instance=~"$instance",client=~"$httpclient"}',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Max',
    fill: 0,
  },
);

local threadPoolTasks = graphPanel.new(
  title='ThreadPool Tasks',
  datasource=grafana.default.datasource,
  fill=2,
  linewidth=2,
  decimals=0,
  labelY1='Tasks/s',
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_executor_queue_remaining_tasks{name="main",instance=~"$instance"}',
      legendFormat='Remaining',
    ),
    prometheus.target(
      'appsuite_executor_queued_tasks{name="main",instance=~"$instance"}',
      legendFormat='Queued',
    ),
    prometheus.target(
      'rate(appsuite_executor_completed_tasks_total{name="main",instance=~"$instance"}[$interval])',
      legendFormat='Completed',
    ),
  ]
);

local threadPool = graphPanel.new(
  title='ThreadPool',
  datasource=grafana.default.datasource,
  decimals=0,
  fill=2,
  linewidth=2,
  labelY1='Threads',
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_executor_active_threads{name="main",instance=~"$instance"}',
      legendFormat='ActiveCount',
    ),
    prometheus.target(
      'appsuite_executor_pool_size_threads{name="main",instance=~"$instance"}',
      legendFormat='PoolSize',
    ),
  ]
);

local httpApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  description='HTTP API response time percentiles.',
  datasource=grafana.default.datasource,
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  labelY1='Response Time',
  min='0',
  format='s',
).addTargets(
  genPercentileTargets('httpapi_requests')
).addSeriesOverride(
  {
    alias: 'max',
    fill: 0,
  },
);

local httpApiRequestsPercentilesByRequest = graphPanel.new(
  title='Top 5 Requests (99th percentile)',
  datasource=grafana.default.datasource,
  fill=2,
  linewidth=2,
  legend_hideZero=true,
  min='0',
  format='s',
  sort='decreasing',
  labelY1='Response Time',
).addTarget(
  prometheus.target(
    'topk(5,histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[${__range_s}s])) by (le, module, action, instance)))',
    legendFormat='{{module}}/{{action}}',
  )
);

local httpApiRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=0,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{status=~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{status!~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='KO',
    ),
  ]
);

local restApiRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=0,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_count{status!~"[45][0-9]{2}",instance=~"$instance"}[$interval]))',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_count{status=~"[45][0-9]{2}",instance=~"$instance"}[$interval]))',
      legendFormat='KO',
    ),
  ]
);

local restApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  description='REST API response time percentiles.',
  datasource=grafana.default.datasource,
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  labelY1='Response Time',
  min='0',
  format='s',
).addTargets(
  genPercentileTargets('restapi_requests')
).addSeriesOverride(
  {
    alias: 'max',
    fill: 0,
  },
);

local circuitBreakerDenials = graphPanel.new(
  title='Average Denials Rate (per-second)',
  datasource=grafana.default.datasource,
  fill=2,
  linewidth=2,
  min='0',
  labelY1='denials/s',
).addTargets(
  [
    prometheus.target(
      'rate(appsuite_circuitbreaker_denials_total{instance=~"$instance", name!="mailfilter"}[$interval])',
      legendFormat='{{name}}',
    ),
    prometheus.target(
      'rate(appsuite_circuitbreaker_denials_total{instance=~"$instance", name="mailfilter"}[$interval])',
      legendFormat='{{name}}',
    ),
  ]
);

local imapRequestRate = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=0,
  fill=2,
  linewidth=2,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  min='0',
  format='cps',
  labelY1='Commands/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_imap_commands_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_imap_commands_seconds_count{status=~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_imap_commands_seconds_count{status!~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='KO',
    ),
  ]
);

local circuitBreakerIMAPStatus = table.new(
  title='IMAP Status',
  styles=[
    {
      dateFormat: 'YYYY-MM-DD HH:mm:ss',
      pattern: 'Value',
      type: 'hidden',
    },
    {
      dateFormat: 'YYYY-MM-DD HH:mm:ss',
      pattern: 'Time',
      type: 'date',
    },
  ]
).addTarget(
  prometheus.target(
    'count(appsuite_circuitbreaker_state{instance=~"$instance",name!="mailfilter"}>0) by (host,state)',
    format='table',
    instant=true,
  )
);

local soapApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  datasource=grafana.default.datasource,
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='s',
  labelY1='Response Time',
).addTargets(
  genPercentileTargets('soapapi_requests')
);

local soapApiRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=0,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_count{status=~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_count{status!~"OK",instance=~"$instance"}[$interval]))',
      legendFormat='KO',
    ),
  ]
);

local webdavApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles $davinterface',
  datasource=grafana.default.datasource,
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='s',
  labelY1='Response Time',
  repeat=davInterfaceName,
  repeatDirection='h',
).addTargets(
  genPercentileTargets('webdav_requests', labels=[std.format('interface="$%s"', davInterfaceName)], groupBy=['interface'])
);

local webdavApiRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=0,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total',
    ),
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_count{status=~"OK|^0",instance=~"$instance"}[$interval]))',
      legendFormat='OK',
    ),
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_count{status!~"OK|^0",instance=~"$instance"}[$interval]))',
      legendFormat='KO',
    ),
  ]
);

local dbTimeoutRate = singlestat.new(
  title='Timeout Error Rate',
  datasource=grafana.default.datasource,
  decimals=0,
  timeFrom='1h',
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true,
).addTarget(
  prometheus.target(
    expr='sum(rate(appsuite_mysql_connections_timeout_total{instance=~"$instance"}[1h])) by (instance)',
  )
);

local configDBConnections = graphPanel.new(
  title='ConfigDB Connections',
  datasource=grafana.default.datasource,
  description='The total number of pooled and active connections of this db pool.',
  fill=2,
  linewidth=2,
  decimals=0,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total{class="configdb",instance=~"$instance"}',
      legendFormat='Pooled {{type}}',
    ),
    prometheus.target(
      'appsuite_mysql_connections_active{class="configdb",instance=~"$instance"}',
      legendFormat='Active {{type}}',
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle{class="configdb",instance=~"$instance"}',
      legendFormat='Idle {{type}}',
    ),
  ]
);

local configDBReadConnections = graphPanel.new(
  title='ConfigDB Read Connections',
  datasource=grafana.default.datasource,
  description='The total number of pooled and active connections of this db pool.',
  aliasColors={
    Max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  decimals=0,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total{class="configdb",instance=~"$instance",type="read"}',
      legendFormat='Pooled',
    ),
    prometheus.target(
      'appsuite_mysql_connections_active{class="configdb",instance=~"$instance",type="read"}',
      legendFormat='Active',
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle{class="configdb",instance=~"$instance",type="read"}',
      legendFormat='Idle',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_max{class="configdb",instance=~"$instance",type="read"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride({ alias: 'Max', fill: 0 },);

local configDBWriteConnections = graphPanel.new(
  title='ConfigDB Write Connections',
  datasource=grafana.default.datasource,
  description='The total number of pooled and active connections of this db pool.',
  aliasColors={
    Max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  decimals=0,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total{class="configdb",instance=~"$instance",type="write"}',
      legendFormat='Pooled',
    ),
    prometheus.target(
      'appsuite_mysql_connections_active{class="configdb",instance=~"$instance",type="write"}',
      legendFormat='Active',
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle{class="configdb",instance=~"$instance",type="write"}',
      legendFormat='Idle',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_max{class="configdb",instance=~"$instance",type="write"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride({ alias: 'Max', fill: 0 },);

local configDBTimes = graphPanel.new(
  title='ConfigDB Times',
  datasource=grafana.default.datasource,
  fill=2,
  linewidth=2,
  decimals=0,
  min='0',
  format='s',
).addTargets(
  [
    prometheus.target(
      'rate(appsuite_mysql_connections_acquire_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='acquire {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_usage_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='usage {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_create_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='create {{type}}',
    ),
  ]
);

local userDBWriteConnections = graphPanel.new(
  title='UserDB Write Connections',
  datasource=grafana.default.datasource,
  description='The total number of pooled and active connections of this db pool.',
  aliasColors={
    Max: 'dark-red',
  },
  decimals=0,
  fill=2,
  linewidth=2,
  min='0',
).addTargets(
  [
    prometheus.target(
      'sum(appsuite_mysql_connections_total{class="userdb",instance=~"$instance",type="write"})',
      legendFormat='Pooled',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_active{class="userdb",instance=~"$instance",type="write"})',
      legendFormat='Active',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_idle{class="userdb",instance=~"$instance",type="write"})',
      legendFormat='Idle',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_max{class="userdb",instance=~"$instance",type="write"}) by (pool)',
      legendFormat='Max (Pool {{pool}})',
    ),
  ]
).addSeriesOverride({ alias: '/Max.*/', fill: 0 },);

local userDBReadConnections = graphPanel.new(
  title='UserDB Read Connections',
  datasource=grafana.default.datasource,
  description='The total number of pooled and active connections of this db pool.',
  aliasColors={
    Max: 'dark-red',
  },
  decimals=0,
  fill=2,
  linewidth=2,
  min='0',
).addTargets(
  [
    prometheus.target(
      'sum(appsuite_mysql_connections_total{class="userdb",instance=~"$instance",type="read"})',
      legendFormat='Pooled',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_active{class="userdb",instance=~"$instance",type="read"})',
      legendFormat='Active',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_idle{class="userdb",instance=~"$instance",type="read"})',
      legendFormat='Idle',
    ),
    prometheus.target(
      'sum(appsuite_mysql_connections_max{class="userdb",instance=~"$instance",type="read"}) by (pool)',
      legendFormat='Max (Pool {{pool}})',
    ),
  ]
).addSeriesOverride({ alias: '/Max.*/', fill: 0 },);

local userDBTimes = graphPanel.new(
  title='UserDB Pool $dbpool Times',
  datasource=grafana.default.datasource,
  decimals=0,
  fill=2,
  linewidth=2,
  min='0',
  format='s',
  repeat=dbPoolName,
  repeatDirection='h',
).addTargets(
  [
    prometheus.target(
      'rate(appsuite_mysql_connections_acquire_seconds_sum{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])',
      legendFormat='Acquire {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_usage_seconds_sum{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])',
      legendFormat='Usage {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_create_seconds_sum{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}[$interval])',
      legendFormat='Create {{type}}',
    ),
  ]
);

grafana.newDashboard(
  title='App Suite', tags=['Java', 'AppSuite'], metric='jvm_info',
).addTemplates(
  [
    template.new(
      name=obj.name,
      label=obj.label,
      query=obj.query,
      datasource=grafana.default.datasource,
      hide=if std.objectHas(obj, 'hide') then obj.hide else '',
      includeAll=if std.objectHas(obj, 'includeAll') then obj.includeAll else false,
      sort=if std.objectHas(obj, 'sort') then obj.sort else 0,
      regex=if std.objectHas(obj, 'regex') then obj.regex else '',
      refresh='load',
    )
    for obj in templates
  ]
).addPanels(
  [
    //------------------------------------------------------------------------------
    row.new(title='Sessions') + { gridPos: { h: 1, w: 24, x: 0, y: 0 } },
  ] + [
    singlestat.new(
      title=obj.title,
      description=obj.description,
      datasource=grafana.default.datasource,
      decimals=0,
      valueName='avg',
      valueMaps=[
        {
          op: '=',
          text: '0',
          value: 'null',
        },
      ],
      sparklineShow=true,
    ).addTarget(
      prometheus.target(
        expr=obj.target.expr,
        legendFormat=obj.target.legendFormat,
      )
    ) + { gridPos: obj.gridPos }
    for obj in sessions
  ] + [
    graphPanel.new(
      title='Overview',
      datasource=grafana.default.datasource,
      aliasColors={
        Max: 'dark-red',
      },
      fill=2,
      linewidth=2,
      labelY1='Sessions',
      min='0',
    ).addTargets(
      [
        prometheus.target(
          expr=obj.target.expr,
          legendFormat=obj.target.legendFormat,
        )
        for obj in sessions
      ] + [
        prometheus.target(
          expr='appsuite_sessions_max{client="all", instance=~"$instance"}',
          legendFormat='Max',
        ),
      ]
    ).addSeriesOverride(
      {
        alias: 'Max',
        fill: 0,
      },
    ) { gridPos: { h: 8, w: 12, x: 6, y: 1 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='ThreadPool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    threadPool { gridPos: { h: 8, w: 12, x: 0, y: 8 } },
    threadPoolTasks { gridPos: { h: 8, w: 12, x: 12, y: 8 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='Cache'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 16 } },
  ] + [
    singlestat.new(
      title=obj.title,
      datasource=grafana.default.datasource,
      decimals=1,
      format='percentunit',
      valueMaps=[
        {
          op: '=',
          text: '0',
          value: 'null',
        },
      ],
      sparklineShow=true,
    ).addTarget(
      prometheus.target(expr=obj.expr,)
    ) { gridPos: obj.gridPos }
    for obj in cacheRatio
  ] + [
    graphPanel.new(
      title='Cache Hit/Miss Ratio',
      datasource=grafana.default.datasource,
      format='percentunit',
      decimals=2,
      fill=2,
      linewidth=2,
      max='1',
    ).addTargets(
      [
        prometheus.target(
          expr='sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) / (sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) + sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])))',
          legendFormat='Hit Ratio',
        ),
        prometheus.target(
          expr='sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])) / (sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) + sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])))',
          legendFormat='Miss Ratio',
        ),
      ]
    ) { gridPos: { h: 8, w: 9, x: 3, y: 17 } },
    graphPanel.new(
      title='Cache Operations',
      datasource=grafana.default.datasource,
      fill=2,
      linewidth=2,
      min='0',
    ).addTargets(
      [
        prometheus.target(
          expr='sum(rate(appsuite_jcs_cache_puts_total{instance=~"$instance"}[$interval]))',
          legendFormat='Puts',
        ),
        prometheus.target(
          expr='sum(rate(appsuite_jcs_cache_removals_total{instance=~"$instance"}[$interval]))',
          legendFormat='Removals',
        ),
      ]
    ) { gridPos: { h: 8, w: 12, x: 12, y: 17 } },
    graphPanel.new(
      title='Top 5 Cache Regions',
      datasource=grafana.default.datasource,
      fill=2,
      linewidth=2,
      sort='decreasing',
      min='0',
      legend_alignAsTable=true,
      legend_rightSide=true,
      legend_sort='avg',
      legend_sortDesc=true,
      legend_max=true,
      legend_min=true,
      legend_avg=true,
      legend_values=true,
    ).addTarget(
      prometheus.target(
        expr='topk(5,avg_over_time(appsuite_jcs_cache_elements_total{instance=~"$instance"}[${__range_s}s]))',
        legendFormat='{{region}}',
      )
    ) { gridPos: { h: 8, w: 24, x: 0, y: 25 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='DB Pool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 33 } },
    dbTimeoutRate { gridPos: { h: 8, w: 6, x: 0, y: 34 } },
    configDBReadConnections { gridPos: { h: 8, w: 9, x: 6, y: 34 } },
    configDBWriteConnections { gridPos: { h: 8, w: 9, x: 15, y: 34 } },
    userDBReadConnections { gridPos: { h: 8, w: 12, x: 0, y: 50 } },
    userDBWriteConnections { gridPos: { h: 8, w: 12, x: 12, y: 50 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='HTTP Client "$' + httpClient + '"',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 66 } },
    httpClientRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 67 } },
    httpClientConnections { gridPos: { h: 8, w: 12, x: 12, y: 67 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='HTTP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 75 } },
    httpApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 76 } },
    httpApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 76 } },
    //
    httpApiRequestsPercentilesByRequest { gridPos: { h: 8, w: 24, x: 0, y: 84 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='REST API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 92 } },
    restApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 93 } },
    restApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 93 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='WebDAV API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 101 } },
    webdavApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 102 } },
    webdavApiResponsePercentiles { gridPos: { h: 8, w: 6, x: 0, y: 110 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='SOAP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 118 } },
    soapApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 119 } },
    soapApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 119 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='Circuit Breaker'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 127 } },
    circuitBreakerDenials { gridPos: { h: 8, w: 12, x: 0, y: 128 } },
    imapRequestRate { gridPos: { h: 8, w: 12, x: 12, y: 128 } },
    circuitBreakerIMAPStatus { gridPos: { h: 8, w: 24, x: 0, y: 136 } },
  ]
)
