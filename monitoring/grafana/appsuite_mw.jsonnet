local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local gauge = grafana.gauge;
local row = grafana.row;
local prometheus = grafana.prometheus;
local table = grafana.tablePanel;

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
    'appsuite_sessiond_TotalCount_sessions{client="all", instance=~"$instance"}',
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
    'appsuite_sessiond_ActiveCount_sessions{client="all", instance=~"$instance"}',
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
    'appsuite_sessiond_ShortTermCount_sessions{client="all", instance=~"$instance"}',
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
    'appsuite_sessiond_LongTermCount_sessions{client="all", instance=~"$instance"}',
    legendFormat='Long Term Sessions'
  )
);

local threadPoolTasks = graphPanel.new(
  title='ThreadPool Tasks',
  datasource='Prometheus',
  description='',
  decimals=0,
  nullPointMode='null as zero',
  labelY1='Tasks/s',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0'
).addTarget(
  prometheus.target(
    'executor_queue_remaining_tasks{name="ox.executor.service",instance=~"$instance"}',
    legendFormat='Remaining'
  )
).addTarget(
  prometheus.target(
    'executor_queued_tasks{name="ox.executor.service",instance=~"$instance"}',
    legendFormat='Queued'
  )
).addTarget(
  prometheus.target(
    'rate(executor_completed_tasks_total{name="ox.executor.service",instance=~"$instance"}[$interval])',
    legendFormat='Completed'
  )
);

local threadPool = graphPanel.new(
  title='ThreadPool',
  datasource='Prometheus',
  description='',
  labelY1='Threads',
  nullPointMode='null as zero',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0'
).addTarget(
  prometheus.target(
    'executor_active_threads{name="ox.executor.service",instance=~"$instance"}',
    legendFormat='ActiveCount'
  )
).addTarget(
  prometheus.target(
    'executor_pool_size_threads{name="ox.executor.service",instance=~"$instance"}',
    legendFormat='PoolSize'
  )
);

local httpApiRequestsSeconds = graphPanel.new(
  title='Latencies: Percentiles, Avg & Max',
  description='HTTP API request times.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.5, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
    legendFormat='p50'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.75, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
    legendFormat='p75'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.9, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
    legendFormat='p90'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.95, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
    legendFormat='p95'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, instance))',
    legendFormat='p99'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_httpapi_requests_seconds_max{instance=~"$instance"})',
    legendFormat='max'
  )
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_httpapi_requests_seconds_sum{instance=~"$instance"}[$interval]))/sum(rate(appsuite_httpapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
    legendFormat='avg'
  )
);

local httpApiRequestsPercentilesByAction = graphPanel.new(
  title='99th percentile (group by action)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_hideZero=true,
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, action, instance))',
    legendFormat='{{action}}'
  )
);

local httpApiRequestsPercentilesByModule = graphPanel.new(
  title='99th percentile (group by module)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_hideZero=true,
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance=~"$instance"}[$interval])) by (le, module, instance))',
    legendFormat='{{module}}'
  )
);

local httpApiRequestsOK = singlestat.new(
  title='OKs',
  description='Successful HTTP API Requests.',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{status="OK", instance=~"$instance"}[$interval]))',
    legendFormat='OKs'
  )
);

local httpApiRequestsKO = singlestat.new(
  title='KOs',
  description='Failed HTTP API Requests.',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{status!="OK", instance=~"$instance"}[$interval]))',
    legendFormat='KOs'
  )
);

local httpApiRequestsTotal = singlestat.new(
  title='Total',
  description='The total number of HTTP API Requests.',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{instance=~"$instance"}[$interval]))',
    legendFormat='Total'
  )
);

local restApiRequestsByMethod = graphPanel.new(
  title='Average Response Time (group by method)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_restapi_requests_timer_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_restapi_requests_timer_seconds_count{instance=~"$instance"}[$interval])) by (method)',
    legendFormat='{{method}}'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_restapi_requests_timer_seconds_max{instance=~"$instance"})',
    legendFormat='max'
  )
);

local restApiRequestsByStatus = graphPanel.new(
  title='Average Response Time (group by status)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_restapi_requests_timer_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_restapi_requests_timer_seconds_count{instance=~"$instance"}[$interval])) by (status)',
    legendFormat='{{status}}'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_restapi_requests_timer_seconds_max{instance=~"$instance"})',
    legendFormat='max'
  )
);

local circuitBreakerDenials = graphPanel.new(
  title='Average Denials Rate (per-second)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  labelY1='denials/s'
).addTarget(
  prometheus.target(
    'rate(appsuite_circuit_breakers_denialsMeter_total{instance=~"$instance", protocol="imap"}[$interval])',
    legendFormat='{{protocol}}'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_circuit_breakers_denialsMeter_total{instance=~"$instance", protocol="mailfilter"}[$interval])',
    legendFormat='{{protocol}}'
  )
);

local circuitBreakerRequestRate = graphPanel.new(
  title='Average Error & Request Rate (per-second)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  labelY1='event/s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_imap_requestRate_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_imap_requestRate_seconds_count{instance=~"$instance"}[$interval]))',
    legendFormat='Requests'
  )
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_imap_errorRate_total{instance=~"$instance"}[$interval]))',
    legendFormat='Errors'
  )
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
    'count(appsuite_circuit_breakers_status{protocol="imap"}) by (account, status)',
    format='table',
    instant=true
  )
);

local soapApiRequestsByOperation = graphPanel.new(
  title='Average Response Time (group by operation)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_soapapi_requests_timer_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_soapapi_requests_timer_seconds_count{instance=~"$instance"}[$interval])) by (operation)',
    legendFormat='{{operation}}'
  )
);

local soapApiRequestsByService = graphPanel.new(
  title='Average Response Time (group by service)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_soapapi_requests_timer_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_soapapi_requests_timer_seconds_count{instance=~"$instance"}[$interval])) by (service)',
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
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'sum(rate(appsuite_webdav_requests_seconds_sum{instance=~"$instance"}[$interval]) / rate(appsuite_webdav_requests_seconds_count{instance=~"$instance"}[$interval])) by (method)',
    legendFormat='{{method}}'
  )
);

local configDBReadConnections = graphPanel.new(
  title='ConfigDB Read Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  decimals=0,
  nullPointMode='null as zero',
  min='0'
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_total_Connections{class="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Pooled'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_active_Connections{class="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Active'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_idle_Connections{class="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Idle'
  )
);

local configDBWriteConnections = graphPanel.new(
  title='ConfigDB Write Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  decimals=0,
  nullPointMode='null as zero',
  min='0'
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_total_Connections{class="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Pooled'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_active_Connections{class="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Active'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_idle_Connections{class="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Idle'
  )
);

local userDBReadConnections = graphPanel.new(
  title='UserDB Read Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  decimals=0,
  nullPointMode='null as zero',
  min='0'
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_total_Connections{class!="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Pooled'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_active_Connections{class!="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Active'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_idle_Connections{class!="ConfigDB",type="read",instance=~"$instance"}',
    legendFormat='Idle'
  )
);

local userDBWriteConnections = graphPanel.new(
  title='UserDB Write Connections',
  datasource='Prometheus',
  description='The total number of pooled and active connections of this db pool.',
  decimals=0,
  nullPointMode='null as zero',
  min='0'
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_total_Connections{class!="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Pooled'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_active_Connections{class!="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Active'
  )
).addTarget(
  prometheus.target(
    'appsuite_mysql_connections_idle_Connections{class!="ConfigDB",type="write",instance=~"$instance"}',
    legendFormat='Idle'
  )
);

local configDBReadTimes = graphPanel.new(
  title='ConfigDB Read Times',
  datasource='Prometheus',
  description='',
  decimals=0,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_acquire_seconds_sum{class="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='acquire'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_usage_seconds_sum{class="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='usage'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_create_seconds_sum{class="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='create'
  )
);

local configDBWriteTimes = graphPanel.new(
  title='ConfigDB Read Times',
  datasource='Prometheus',
  description='',
  decimals=0,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_acquire_seconds_sum{class="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='acquire'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_usage_seconds_sum{class="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='usage'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_create_seconds_sum{class="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='create'
  )
);

local userDBReadTimes = graphPanel.new(
  title='UserDB Read Times',
  datasource='Prometheus',
  description='',
  decimals=0,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_acquire_seconds_sum{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='acquire'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_usage_seconds_sum{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='usage'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_create_seconds_sum{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class!="ConfigDB",type="read",instance=~"$instance"}[$interval])',
    legendFormat='create'
  )
);

local userDBWriteTimes = graphPanel.new(
  title='UserDB Write Times',
  datasource='Prometheus',
  description='',
  decimals=0,
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_acquire_seconds_sum{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_acquire_seconds_count{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='acquire'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_usage_seconds_sum{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_usage_seconds_count{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='usage'
  )
).addTarget(
  prometheus.target(
    'rate(appsuite_mysql_connections_create_seconds_sum{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])/rate(appsuite_mysql_connections_create_seconds_count{class!="ConfigDB",type="write",instance=~"$instance"}[$interval])',
    legendFormat='create'
  )
);

grafana.newDashboard(
  title='AppSuite',
  tags=['java', 'appsuite-mw'],
  metric='jvm_info'
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
    configDBReadConnections { gridPos: { h: 8, w: 6, x: 0, y: 17 } },
    configDBWriteConnections { gridPos: { h: 8, w: 6, x: 6, y: 17 } },
    userDBReadConnections { gridPos: { h: 8, w: 6, x: 12, y: 17 } },
    userDBWriteConnections { gridPos: { h: 8, w: 6, x: 18, y: 17 } },
    //
    configDBReadTimes { gridPos: { h: 8, w: 6, x: 0, y: 25 } },
    configDBWriteTimes { gridPos: { h: 8, w: 6, x: 6, y: 25 } },
    userDBReadTimes { gridPos: { h: 8, w: 6, x: 12, y: 25 } },
    userDBWriteTimes { gridPos: { h: 8, w: 6, x: 18, y: 25 } },
  ] + [
    row.new(
      title='HTTP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 33 } },
    httpApiRequestsOK { gridPos: { h: 6, w: 4, x: 0, y: 34 } },
    httpApiRequestsKO { gridPos: { h: 6, w: 4, x: 4, y: 34 } },
    httpApiRequestsTotal { gridPos: { h: 6, w: 4, x: 8, y: 34 } },
    //
    httpApiRequestsSeconds { gridPos: { h: 8, w: 24, x: 0, y: 40 } },
    //
    httpApiRequestsPercentilesByAction { gridPos: { h: 8, w: 12, x: 0, y: 48 } },
    httpApiRequestsPercentilesByModule { gridPos: { h: 8, w: 12, x: 12, y: 48 } },
  ] + [
    row.new(
      title='REST API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 56 } },
    restApiRequestsByMethod { gridPos: { h: 8, w: 12, x: 0, y: 57 } },
    restApiRequestsByStatus { gridPos: { h: 8, w: 12, x: 12, y: 57 } },
  ] + [
    row.new(
      title='WebDAV API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 65 } },
    webdavApiRequestsOK { gridPos: { h: 6, w: 4, x: 0, y: 66 } },
    webdavApiRequestsKO { gridPos: { h: 6, w: 4, x: 4, y: 66 } },
    webdavApiRequestsTotal { gridPos: { h: 6, w: 4, x: 12, y: 66 } },
    //
    webdavApiRequestsByInterface { gridPos: { h: 8, w: 12, x: 0, y: 72 } },
    webdavApiRequestsByMethod { gridPos: { h: 8, w: 12, x: 12, y: 72 } },
  ] + [
    row.new(
      title='SOAP API'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 80 } },
    soapApiRequestsByOperation { gridPos: { h: 8, w: 12, x: 0, y: 81 } },
    soapApiRequestsByService { gridPos: { h: 8, w: 12, x: 12, y: 81 } },
  ] + [
    row.new(
      title='Circuit Breaker'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 89 } },
    circuitBreakerDenials { gridPos: { h: 8, w: 12, x: 0, y: 90 } },
    circuitBreakerRequestRate { gridPos: { h: 8, w: 12, x: 12, y: 90 } },
    circuitBreakerIMAPStatus { gridPos: { h: 8, w: 24, x: 0, y: 98 } },
  ]
)
