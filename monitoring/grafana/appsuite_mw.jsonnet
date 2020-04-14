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

local restApiRequestsPerSecond = graphPanel.new(
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
      'sum(rate(appsuite_restapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total'
    ),
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_count{status=~"([45][0-9][0-9])",instance=~"$instance"}[$interval]))',
      legendFormat='OK'
    ),
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_count{status!~"([45][0-9][0-9])",instance=~"$instance"}[$interval]))',
      legendFormat='KO'
    ),
  ]
);

local restApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  description='REST API response time percentiles.',
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
      'histogram_quantile(0.5, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p50',
    ),
    prometheus.target(
      'histogram_quantile(0.75, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p75',
    ),
    prometheus.target(
      'histogram_quantile(0.9, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p90',
    ),
    prometheus.target(
      'histogram_quantile(0.95, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p95',
    ),
    prometheus.target(
      'histogram_quantile(0.99, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p99',
    ),
    prometheus.target(
      'histogram_quantile(0.999, sum(rate(appsuite_restapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p999',
    ),
    prometheus.target(
      'sum(rate(appsuite_restapi_requests_seconds_sum{instance=~"$instance"}[$interval]))/sum(rate(appsuite_restapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='avg',
    ),
    prometheus.target(
      'sum(appsuite_restapi_requests_seconds_max{instance=~"$instance"})',
      legendFormat='max',
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

local soapApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles',
  datasource='Prometheus',
  description='',
  aliasColors={
    max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  min='0',
  format='s',
  labelY1='Response Time',
).addTargets(
  [
    prometheus.target(
      'histogram_quantile(0.5, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p50',
    ),
    prometheus.target(
      'histogram_quantile(0.75, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p75',
    ),
    prometheus.target(
      'histogram_quantile(0.9, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p90',
    ),
    prometheus.target(
      'histogram_quantile(0.95, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p95',
    ),
    prometheus.target(
      'histogram_quantile(0.99, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p99',
    ),
    prometheus.target(
      'histogram_quantile(0.999, sum(rate(appsuite_soapapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
      legendFormat='p999',
    ),
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_sum{instance=~"$instance"}[$interval]))/sum(rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='avg',
    ),
    prometheus.target(
      'sum(appsuite_soapapi_requests_seconds_max{instance=~"$instance"})',
      legendFormat='max',
    ),
  ]
);

local soapApiRequestsPerSecond = graphPanel.new(
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
      'sum(rate(appsuite_soapapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total'
    ),
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_count{status=~"([45][0-9][0-9]|OK|0)",instance=~"$instance"}[$interval]))',
      legendFormat='OK'
    ),
    prometheus.target(
      'sum(rate(appsuite_soapapi_requests_seconds_count{status!~"([45][0-9][0-9]|OK|0)",instance=~"$instance"}[$interval]))',
      legendFormat='KO'
    ),
  ]
);

local webdavApiResponsePercentiles = graphPanel.new(
  title='Response Time Percentiles $davinterface',
  datasource='Prometheus',
  description='',
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
  [
    prometheus.target(
      'histogram_quantile(0.5, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p50',
    ),
    prometheus.target(
      'histogram_quantile(0.75, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p75',
    ),
    prometheus.target(
      'histogram_quantile(0.9, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p90',
    ),
    prometheus.target(
      'histogram_quantile(0.95, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p95',
    ),
    prometheus.target(
      'histogram_quantile(0.99, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p99',
    ),
    prometheus.target(
      'histogram_quantile(0.999, sum(rate(appsuite_webdav_requests_seconds_bucket{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval])) by (le, interface, instance))',
      legendFormat='p999',
    ),
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_sum{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval]))/sum(rate(appsuite_webdav_requests_seconds_count{instance=~"$instance",interface="$' + davInterfaceName + '"}[$interval]))',
      legendFormat='avg',
    ),
    prometheus.target(
      'sum(appsuite_webdav_requests_seconds_max{instance=~"$instance",interface="$' + davInterfaceName + '"})',
      legendFormat='max',
    ),
  ]
);

local webdavApiRequestsPerSecond = graphPanel.new(
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
      'sum(rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval]))',
      legendFormat='Total'
    ),
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_count{status=~"([45][0-9][0-9]|OK|0)",instance=~"$instance"}[$interval]))',
      legendFormat='OK'
    ),
    prometheus.target(
      'sum(rate(appsuite_webdav_requests_seconds_count{status!~"([45][0-9][0-9]|OK|0)",instance=~"$instance"}[$interval]))',
      legendFormat='KO'
    ),
  ]
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
      'appsuite_mysql_connections_total_Connections{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}',
      legendFormat='Pooled {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_active_Connections{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}',
      legendFormat='Active {{type}}'
    ),
    prometheus.target(
      'appsuite_mysql_connections_idle_Connections{class!="configdb",instance=~"$instance",pool="$' + dbPoolName + '"}',
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
).addTemplate(
  template.new(
    name=davInterfaceName,
    label='DAV Interface',
    hide='variable',
    includeAll=true,
    datasource='Prometheus',
    query='label_values(appsuite_webdav_requests_seconds_count,interface)',
    refresh='load',
    sort=1,
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
    restApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 65 } },
    restApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 65 } },
  ] + [
    row.new(
      title='WebDAV API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 73 } },
    webdavApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 74 } },
    webdavApiResponsePercentiles { gridPos: { h: 8, w: 6, x: 0, y: 82 } },
  ] + [
    row.new(
      title='SOAP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 88 } },
    soapApiRequestsPerSecond { gridPos: { h: 8, w: 12, x: 0, y: 89 } },
    soapApiResponsePercentiles { gridPos: { h: 8, w: 12, x: 12, y: 89 } },
  ] + [
    row.new(
      title='Circuit Breaker'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 97 } },
    circuitBreakerDenials { gridPos: { h: 8, w: 12, x: 0, y: 98 } },
    circuitBreakerRequestRate { gridPos: { h: 8, w: 12, x: 12, y: 98 } },
    circuitBreakerIMAPStatus { gridPos: { h: 8, w: 24, x: 0, y: 106 } },
  ]
)
