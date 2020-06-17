local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local gauge = grafana.gauge;
local row = grafana.row;
local prometheus = grafana.prometheus;
local table = grafana.tablePanel;
local template = grafana.template;
local link = grafana.link;

local dbPoolName = 'dbpool';
local davInterfaceName = 'davinterface';
local httpClient = 'httpclient';
local serviceName = 'service';
local circuitBreakerName = 'circuitbreaker_name';

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
    includeAll: true,
    customAllValue: '.*',
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
  {
    name: circuitBreakerName,
    label: 'CircuitBreaker Name',
    query: 'label_values(appsuite_circuitbreaker_state, name)',
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
    gridPos: { h: 4, w: 3, x: 0, y: 25 },
  },
  {
    title: 'Miss Ratio',
    expr: 'sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])) / (sum(rate(appsuite_jcs_cache_hits_total{instance=~"$instance"}[$interval])) + sum(rate(appsuite_jcs_cache_misses_total{instance=~"$instance"}[$interval])))',
    gridPos: { h: 4, w: 3, x: 0, y: 25 },
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

local imapFailureRatio = singlestat.new(
  title='Failure Ratio',
  datasource=grafana.default.datasource,
  decimals=1,
  format='percentunit',
  colorValue=true,
  gaugeShow=true,
  gaugeThresholdLabels=true,
  thresholds='25,50',
  valueName='current',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  timeFrom='1m',
).addTarget(
  prometheus.target(
    expr='sum without (instance, status, host) (rate(appsuite_imap_commands_seconds_count{status!~"OK"}[1m])) / sum without (instance, status, host) (rate(appsuite_imap_commands_seconds_count[1m]))',
  )
);

local imapRequestRate = graphPanel.new(
  title='Requests (per-second)',
  datasource=grafana.default.datasource,
  decimals=1,
  fill=2,
  linewidth=2,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  legend_values=true,
  legend_rightSide=true,
  legend_alignAsTable=true,
  legend_current=true,
  min='0',
  format='reqps',
  labelY1='Requests/s',
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

local circuitBreakerDenialRate = graphPanel.new(
  title='Denial Rate: $' + circuitBreakerName,
  datasource=grafana.default.datasource,
  description='The number of times an execution was denied because the circuit was open.',
  fill=2,
  linewidth=2,
  legend_show=false,
  min='0',
  decimals=0,
).addTarget(
  prometheus.target(
    expr='rate(appsuite_circuitbreaker_denials_total{instance=~"$instance", name=~"$' + circuitBreakerName + '"}[$interval])',
    legendFormat='{{name}}',
  )
);

local circuitBreakerOpenRate = graphPanel.new(
  title='Open Rate: $' + circuitBreakerName,
  datasource=grafana.default.datasource,
  description='The number of times the circuit was opened.',
  fill=2,
  linewidth=2,
  legend_show=false,
  min='0',
  decimals=0,
).addTarget(
  prometheus.target(
    expr='rate(appsuite_circuitbreaker_opens_total{instance=~"$instance", name=~"$' + circuitBreakerName + '"}[$interval])',
    legendFormat='{{name}}',
  )
);

local circuitBreakerStates = table.new(
  title='States',
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
    expr='count(appsuite_circuitbreaker_state{instance=~"$instance"} > 0) by (host, name, state)',
    format='table',
    instant=true,
  )
);

local circuitBreakerClosed = singlestat.new(
  title='CLOSED',
  description='Number of closed CircuitBreaker',
  datasource=grafana.default.datasource,
  decimals=0,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    expr='sum(appsuite_circuitbreaker_state{state="CLOSED"})',
  )
);

local circuitBreakerOpen = singlestat.new(
  title='OPEN',
  description='Number of open CircuitBreaker',
  datasource=grafana.default.datasource,
  decimals=0,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    expr='sum(appsuite_circuitbreaker_state{state="OPEN"})',
  )
);

local circuitBreakerHalfOpen = singlestat.new(
  title='HALF_OPEN',
  description='Number of half-open CircuitBreaker',
  datasource=grafana.default.datasource,
  decimals=0,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    expr='sum(appsuite_circuitbreaker_state{state="HALF_OPEN"})',
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

local dbTimeoutRatio = singlestat.new(
  title='Timeout Error Ratio',
  datasource=grafana.default.datasource,
  decimals=1,
  format='percentunit',
  colorValue=true,
  gaugeShow=true,
  gaugeThresholdLabels=true,
  thresholds='25,50',
  valueName='current',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  timeFrom='1m',
).addTarget(
  prometheus.target(
    expr='sum without (instance, type, class, pool, service) (rate(appsuite_mysql_connections_timeout_total[1m])) / sum without (instance, type, class, pool, service) (rate(appsuite_mysql_connections_total[1m]))',
  )
);

local configdbActive = singlestat.new(
  title='Active ConfigDB',
  datasource=grafana.default.datasource,
  description='**Active ConfigDB Connections**',
  decimals=0,
  valueName='current',
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
    expr='sum(appsuite_mysql_connections_active{class=~"configdb"})',
  )
);

local userdbActive = singlestat.new(
  title='Active UserDB',
  datasource=grafana.default.datasource,
  description='**Active UserDB Connections**',
  decimals=0,
  valueName='current',
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
    expr='sum(appsuite_mysql_connections_active{class=~"userdb"})',
  )
);

local dbPoolTable = table.new(
  title='DB Pools',
  sort={
    col: 2,
    desc: true,
  },
  styles=[
    {
      pattern: '/Value.*/',
      type: 'hidden',
    },
    {
      pattern: 'Time',
      type: 'hidden',
    },
  ]
).addTarget(
  prometheus.target(
    expr='count(appsuite_mysql_connections_total{pool=~".*[0-9]+"}) by (pool, class, type)',
    format='table',
    instant=true,
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

local userdbConnections = graphPanel.new(
  title='UserDB $' + dbPoolName + ' Connections',
  datasource=grafana.default.datasource,
  decimals=0,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=true,
  legend_values=true,
  legend_avg=true,
  legend_current=true,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total{class=~"userdb",instance=~"$instance",pool=~"$' + dbPoolName + '"}',
      legendFormat='Pooled (Type: {{type}}, Pool: {{pool}})',
    ),
    prometheus.target(
      'appsuite_mysql_connections_active{class=~"userdb",instance=~"$instance",pool=~"$' + dbPoolName + '"}',
      legendFormat='Active (Type: {{type}}, Pool: {{pool}})',
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle{class=~"userdb",instance=~"$instance",pool=~"$' + dbPoolName + '"}',
      legendFormat='Idle (Type: {{type}}, Pool: {{pool}})',
    ),
    prometheus.target(
      'appsuite_mysql_connections_max{class=~"userdb",instance=~"$instance",pool=~"$' + dbPoolName + '"}',
      legendFormat='Max (Type: {{type}}, Pool: {{pool}})',
    ),
  ]
).addSeriesOverride({ alias: '/Max.*/', fill: 0, color: '#C4162A' },);

local sessionStorageBytes = graphPanel.new(
  title='Consumed memory',
  datasource=grafana.default.datasource,
  description='Consumed memory of stored sessions held by this node in.',
  fill=2,
  linewidth=2,
  decimals=0,
  format='decbytes',
  min='0',
).addTarget(
  prometheus.target(
    'appsuite_sessionstorage_memory_bytes{instance=~"$instance"}',
    legendFormat='{{type}}',
  )
);

local sessionStorageTotal = graphPanel.new(
  title='Stored sessions',
  datasource=grafana.default.datasource,
  description='Number of stored sessions held by this node.',
  aliasColors={
    global: 'dark-red',
  },
  fill=2,
  linewidth=2,
  decimals=0,
  min='0',
).addTargets(
  [
    prometheus.target(
      'appsuite_sessionstorage_sessions_total{instance=~"$instance"}',
      legendFormat='{{type}}',
    ),
    prometheus.target(
      'sum(appsuite_sessionstorage_sessions_total{type="owned"})',
      legendFormat='global',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'global',
    fill: 0,
  },
);

grafana.newDashboard(
  title='App Suite', tags=['Java', 'AppSuite'], metric='jvm_info',
).addLink(
  link.dashboards(
    title='Documentation',
    tags=[],
    url='https://documentation.open-xchange.com/latest/middleware/monitoring/02_micrometer_and_prometheus.html#visualization',
    targetBlank=true,
    icon='info',
    type='link',
    asDropdown=false,
  )
).addTemplates(
  [
    template.new(
      name=obj.name,
      label=obj.label,
      query=obj.query,
      datasource=grafana.default.datasource,
      hide=if std.objectHas(obj, 'hide') then obj.hide else '',
      includeAll=if std.objectHas(obj, 'includeAll') then obj.includeAll else false,
      allValues=if std.objectHas(obj, 'customAllValue') then obj.customAllValue else null,
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
    row.new(
      title='SessionStorage',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    sessionStorageBytes { gridPos: { h: 8, w: 12, x: 0, y: 8 } },
    sessionStorageTotal { gridPos: { h: 8, w: 12, x: 12, y: 8 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='ThreadPool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 16 } },
    threadPool { gridPos: { h: 8, w: 12, x: 0, y: 17 } },
    threadPoolTasks { gridPos: { h: 8, w: 12, x: 12, y: 17 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='Cache'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 25 } },
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
    ) { gridPos: { h: 8, w: 9, x: 3, y: 26 } },
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
    ) { gridPos: { h: 8, w: 12, x: 12, y: 26 } },
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
    ) { gridPos: { h: 8, w: 24, x: 0, y: 33 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='DB Pool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 41 } },
    dbTimeoutRatio { gridPos: { h: 8, w: 6, x: 0, y: 42 } },
    configdbActive { gridPos: { h: 4, w: 3, x: 6, y: 42 } },
    userdbActive { gridPos: { h: 4, w: 3, x: 6, y: 45 } },
    dbPoolTable { gridPos: { h: 8, w: 15, x: 9, y: 42 } },
    configDBReadConnections { gridPos: { h: 8, w: 12, x: 0, y: 50 } },
    configDBWriteConnections { gridPos: { h: 8, w: 12, x: 12, y: 50 } },
    userdbConnections { gridPos: { h: 8, w: 24, x: 0, y: 58 } },
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
      title='IMAP'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 127 } },
    imapFailureRatio { gridPos: { h: 8, w: 6, x: 0, y: 128 } },
    imapRequestRate { gridPos: { h: 8, w: 18, x: 6, y: 128 } },
  ] + [
    //------------------------------------------------------------------------------
    row.new(
      title='CircuitBreaker'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 136 } },
    circuitBreakerClosed { gridPos: { h: 4, w: 3, x: 0, y: 137 } },
    circuitBreakerHalfOpen { gridPos: { h: 4, w: 3, x: 3, y: 137 } },
    circuitBreakerOpen { gridPos: { h: 4, w: 3, x: 0, y: 141 } },
    circuitBreakerDenialRate { gridPos: { h: 8, w: 9, x: 6, y: 137 } },
    circuitBreakerOpenRate { gridPos: { h: 8, w: 9, x: 15, y: 137 } },
    circuitBreakerStates { gridPos: { h: 8, w: 24, x: 0, y: 153 } },
  ]
)
