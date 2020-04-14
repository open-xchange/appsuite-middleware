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

local overviewTotalSessions = singlestat.new(
  title='Total Sessions',
  description='The number of total sessions.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessions_total_count{client="all", instance=~"$instance"}',
    legendFormat='Total Sessions'
  )
);

local overviewActiveSessions = singlestat.new(
  title='Active Sessions',
  description='The number of active sessions or in other words the number of sessions within the first two short term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessions_active_count{client="all", instance=~"$instance"}',
    legendFormat='Active Sessions'
  )
);

local overviewShortTermSessions = singlestat.new(
  title='Short Term Sessions',
  description='The number of sessions in the short term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessions_short_term_count{client="all", instance=~"$instance"}',
    legendFormat='Short Term Sessions'
  )
);

local overviewLongTermSessions = singlestat.new(
  title='Long Term Sessions',
  description='The number of sessions in the long term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessions_long_term_count{client="all", instance=~"$instance"}',
    legendFormat='Long Term Sessions'
  )
);

local threadPoolTasks = graphPanel.new(
  title='ThreadPool Tasks',
  datasource='Prometheus',
  description='',
  fill=2,
  linewidth=2,
  decimals=0,
  nullPointMode='null as zero',
  labelY1='Tasks/s',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0'
).addTargets(
  [
    prometheus.target(
      'appsuite_executor_queue_remaining_tasks{name="main",instance=~"$instance"}',
      legendFormat='Remaining'
    ),
    prometheus.target(
      'appsuite_executor_queued_tasks{name="main",instance=~"$instance"}',
      legendFormat='Queued'
    ),
    prometheus.target(
      'rate(appsuite_executor_completed_tasks_total{name="main",instance=~"$instance"}[$interval])',
      legendFormat='Completed'
    ),
  ]
);

local threadPool = graphPanel.new(
  title='ThreadPool',
  datasource='Prometheus',
  description='',
  decimals=0,
  fill=2,
  linewidth=2,
  labelY1='Threads',
  nullPointMode='null as zero',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0'
).addTargets(
  [
    prometheus.target(
      'appsuite_executor_active_threads{name="main",instance=~"$instance"}',
      legendFormat='ActiveCount'
    ),
    prometheus.target(
      'appsuite_executor_pool_size_threads{name="main",instance=~"$instance"}',
      legendFormat='PoolSize'
    ),
  ]
);

local httpApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  description='HTTP API response time percentiles.',
  datasource='Prometheus',
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  labelY1='Response Time',
  min='0',
  format='s',
).addTargets(
  [
    prometheus.target(
      'histogram_quantile(0.5, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p50',
    ),
    prometheus.target(
      'histogram_quantile(0.75, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p75',
    ),
    prometheus.target(
      'histogram_quantile(0.9, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p90',
    ),
    prometheus.target(
      'histogram_quantile(0.95, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p95',
    ),
    prometheus.target(
      'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p99',
    ),
    prometheus.target(
      'histogram_quantile(0.999, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p999',
    ),
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_sum{instance=~"$instance"}[$interval]))/sum(rate(appsuite_httpapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='avg',
    ),
    prometheus.target(
      'sum(appsuite_httpapi_requests_seconds_max{instance=~"$instance"})',
      legendFormat='max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'max',
    fill: 0,
  },
);

local httpApiRequestsPercentilesByRequest = graphPanel.new(
  title='Top Requests (99th percentile)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  legend_hideZero=true,
  min='0',
  format='s',
  sort='decreasing',
  labelY1='Response Time',
).addTarget(
  prometheus.target(
    'topk(10,histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, module, action, instance)))',
    legendFormat='{{module}}/{{action}}',
  )
);

local httpApiRequestsPerSecond = graphPanel.new(
  title='Requests (per-second)',
  description='',
  datasource='Prometheus',
  //decimals=0,
  aliasColors={
    KO: 'dark-red',
    OK: 'dark-green',
    Total: 'dark-yellow',
  },
  nullPointMode='null as zero',
  fill=2,
  linewidth=2,
  min='0',
  format='cps',
  labelY1='Counts/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total'
    ),
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{status="OK",instance=~"$instance"}[$interval]))',
      legendFormat='OK'
    ),
    prometheus.target(
      'sum(rate(appsuite_httpapi_requests_seconds_count{status!="OK",instance=~"$instance"}[$interval]))',
      legendFormat='KO'
    ),
  ]
);

local restApiRequestsByMethod = graphPanel.new(
  title='Average Response Time (group by method)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s',
  aliasColors={
    max: 'dark-red',
  },
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_restapi_requests_seconds_count{instance=~"$instance"}[$interval])) by (method)',
      legendFormat='{{method}}'
    ),
    prometheus.target(
      'sum(appsuite_restapi_requests_seconds_max{instance=~"$instance"})',
      legendFormat='max'
    ),
  ]
).addSeriesOverride(
  {
    alias: 'max',
    fill: 0,
  },
);

local restApiRequestsByStatus = graphPanel.new(
  title='Average Response Time (group by status)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s',
  aliasColors={
    max: 'dark-red',
  },
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_restapi_requests_seconds_count{instance=~"$instance"}[$interval])) by (status)',
      legendFormat='{{status}}'
    ),
    prometheus.target(
      'sum(appsuite_restapi_requests_seconds_max{instance=~"$instance"})',
      legendFormat='max'
    ),
  ]
).addSeriesOverride(
  {
    alias: 'max',
    fill: 0,
  },
);

local circuitBreakerDenials = graphPanel.new(
  title='Average Denials Rate (per-second)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  labelY1='denials/s',
).addTargets(
  [
    prometheus.target(
      'rate(appsuite_circuit_breaker_denials_total{instance=~"$instance", protocol="imap"}[$interval])',
      legendFormat='{{protocol}}'
    ),
    prometheus.target(
      'rate(appsuite_circuit_breaker_denials_total{instance=~"$instance", protocol="mailfilter"}[$interval])',
      legendFormat='{{protocol}}'
    ),
  ]
);

local circuitBreakerRequestRate = graphPanel.new(
  title='Average Error & Request Rate (per-second)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  labelY1='event/s',
).addTargets(
  [
    prometheus.target(
      'sum(rate(appsuite_imap_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_imap_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Requests'
    ),
    prometheus.target(
      'sum(rate(appsuite_imap_errors_total{instance=~"$instance"}[$interval]))',
      legendFormat='Errors'
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
    'count(appsuite_circuit_breaker_status{protocol="imap"}) by (account, status)',
    format='table',
    instant=true
  )
);

local soapApiRequestsByOperation = graphPanel.new(
  title='Average Response Time (group by operation)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_soapapi_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_soapapi_requests_seconds_count{instance=~"$instance"}[$interval])) by (operation)',
    legendFormat='{{operation}}'
  )
);

local soapApiRequestsByService = graphPanel.new(
  title='Average Response Time (group by service)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_soapapi_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_soapapi_requests_seconds_count{instance=~"$instance"}[$interval])) by (service)',
    legendFormat='{{service}}'
  )
);

local webdavApiRequestsOK = singlestat.new(
  title='OKs',
  description='Successful WebDAV API Requests.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  colorValue=true,
  sparklineShow=true
).addTarget(
  prometheus.target(
    'sum(increase(appsuite_webdav_requests_seconds_count{status=~"OK|0", instance=~"$instance"}[$interval]))',
    legendFormat='OKs'
  )
);

local webdavApiRequestsKO = singlestat.new(
  title='KOs',
  description='Failed WebDAV API Requests.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  colorValue=true,
  colors=[
    '#d44a3a',
    'rgba(237, 129, 40, 0.89)',
    '#299c46',
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'sum(increase(appsuite_webdav_requests_seconds_count{status!~"OK|0", instance=~"$instance"}[$interval]))',
    legendFormat='KOs'
  )
);

local webdavApiRequestsTotal = singlestat.new(
  title='Total',
  description='The total number of WebDAV API Requests.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  valueMaps=[
    {
      op: '=',
      text: '0',
      value: 'null',
    },
  ],
  sparklineShow=true
).addTarget(
  prometheus.target(
    'sum(increase(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval]))',
    legendFormat='Total'
  )
);


local webdavApiRequestsByInterface = graphPanel.new(
  title='Average Response Time (group by interface)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_webdav_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval])) by (interface)',
    legendFormat='{{interface}}'
  )
);

local webdavApiRequestsByMethod = graphPanel.new(
  title='Average Response Time (group by method)',
  description='',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_webdav_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval])) by (method)',
    legendFormat='{{method}}'
  )
);

local configDBConnections = graphPanel.new(
  title='ConfigDB Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  fill=2,
  linewidth=2,
  decimals=0,
  min='0'
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total{class="configdb",instance=~"$instance"}',
      legendFormat='Pooled {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_active{class="configdb",instance=~"$instance"}',
      legendFormat='Active {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle{class="configdb",instance=~"$instance"}',
      legendFormat='Idle {{type}}'
    ),
  ]
);

local configDBTimes = graphPanel.new(
  title='ConfigDB Times',
  datasource='Prometheus',
  fill=2,
  linewidth=2,
  description='',
  decimals=0,
  min='0',
  format='s',
).addTargets(
  [
    prometheus.target(
      'rate(appsuite_mysql_connections_acquire_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='acquire {{type}}'
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_usage_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='usage {{type}}'
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_create_seconds_sum{class="configdb",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class="configdb",instance=~"$instance"}[$interval])',
      legendFormat='create {{type}}'
    ),
  ]
);

local userDBConnections = graphPanel.new(
  title='UserDB Pool $dbpool Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  decimals=0,
  fill=2,
  linewidth=2,
  min='0',
  repeat=dbPoolName,
  repeatDirection='h',
).addTargets(
  [
    prometheus.target(
      'appsuite_mysql_connections_total_Connections{class!="configdb",instance=~"$instance",pool="$dbpool"}',
      legendFormat='Pooled {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_active_Connections{class!="configdb",instance=~"$instance",pool="$dbpool"}',
      legendFormat='Active {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle_Connections{class!="configdb",instance=~"$instance",pool="$dbpool"}',
      legendFormat='Idle {{type}}'
    ),
  ]
);

local userDBTimes = graphPanel.new(
  title='UserDB Pool $dbpool Times',
  datasource='Prometheus',
  description='',
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
      'rate(appsuite_mysql_connections_acquire_seconds_sum{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])',
      legendFormat='Acquire {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_usage_seconds_sum{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])',
      legendFormat='Usage {{type}}',
    ),
    prometheus.target(
      'rate(appsuite_mysql_connections_create_seconds_sum{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class!="configdb",instance=~"$instance",pool="$dbpool"}[$interval])',
      legendFormat='Create {{type}}',
    ),
  ]
);

grafana.newDashboard(
  title='AppSuite',
  tags=['Java', 'AppSuite-MW'],
  metric='jvm_info'
).addTemplate(
  template.new(
    name=dbPoolName,
    label='DB Pool',
    hide='variable',
    includeAll=true,
    datasource='Prometheus',
    query='label_values(appsuite_mysql_connections_total,pool)',
    refresh='load',
    regex='([0-9]+)',
    sort=3,
  )
).addPanels(
  [
    row.new(
      title='Overview'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 1 } },
    overviewTotalSessions { gridPos: { h: 6, w: 4, x: 0, y: 1 } },
    overviewActiveSessions { gridPos: { h: 6, w: 4, x: 4, y: 1 } },
    overviewShortTermSessions { gridPos: { h: 6, w: 4, x: 8, y: 1 } },
    overviewLongTermSessions { gridPos: { h: 6, w: 4, x: 12, y: 1 } },
  ] + [
    row.new(
      title='ThreadPool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    threadPool { gridPos: { h: 8, w: 12, x: 0, y: 8 } },
    threadPoolTasks { gridPos: { h: 8, w: 12, x: 12, y: 8 } },
  ] + [
    row.new(
      title='DB'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 16 } },
    configDBConnections { gridPos: { h: 8, w: 12, x: 0, y: 17 } },
    configDBTimes { gridPos: { h: 8, w: 12, x: 12, y: 17 } },
    //
    userDBConnections { gridPos: { h: 8, w: 6, x: 0, y: 25 } },
    //
    userDBTimes { gridPos: { h: 8, w: 6, x: 0, y: 33 } },
  ] + [
    row.new(
      title='HTTP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 41 } },
    httpApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 42 } },
    httpApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 42 } },
    //
    httpApiRequestsPercentilesByRequest { gridPos: { h: 8, w: 12, x: 0, y: 50 } },
  ] + [
    row.new(
      title='REST API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 64 } },
    restApiRequestsByMethod { gridPos: { h: 8, w: 12, x: 0, y: 65 } },
    restApiRequestsByStatus { gridPos: { h: 8, w: 12, x: 12, y: 65 } },
  ] + [
    row.new(
      title='WebDAV API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 73 } },
    webdavApiRequestsOK { gridPos: { h: 6, w: 4, x: 0, y: 74 } },
    webdavApiRequestsKO { gridPos: { h: 6, w: 4, x: 4, y: 74 } },
    webdavApiRequestsTotal { gridPos: { h: 6, w: 4, x: 12, y: 74 } },
    //
    webdavApiRequestsByInterface { gridPos: { h: 8, w: 12, x: 0, y: 80 } },
    webdavApiRequestsByMethod { gridPos: { h: 8, w: 12, x: 12, y: 80 } },
  ] + [
    row.new(
      title='SOAP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 88 } },
    soapApiRequestsByOperation { gridPos: { h: 8, w: 12, x: 0, y: 89 } },
    soapApiRequestsByService { gridPos: { h: 8, w: 12, x: 12, y: 89 } },
  ] + [
    row.new(
      title='Circuit Breaker'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 97 } },
    circuitBreakerDenials { gridPos: { h: 8, w: 12, x: 0, y: 98 } },
    circuitBreakerRequestRate { gridPos: { h: 8, w: 12, x: 12, y: 98 } },
    circuitBreakerIMAPStatus { gridPos: { h: 8, w: 24, x: 0, y: 106 } },
  ]
)
