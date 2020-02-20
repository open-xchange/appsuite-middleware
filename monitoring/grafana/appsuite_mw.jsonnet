local grafana = import 'grafonnet/grafana.libsonnet';
local dashboard = grafana.dashboard;
local template = grafana.template;
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local gauge = grafana.gauge;
local row = grafana.row;
local prometheus = grafana.prometheus;
local table = grafana.tablePanel;

local overviewRow = row.new(
  title='Overview'
);

local httpApiRow = row.new(
  title='HTTP API'
);

local restApiRow = row.new(
  title='REST API'
);

local circuitBreakerRow = row.new(
  title='Circuit Breaker'
);

local overviewTotalSessions = singlestat.new(
  title='Total Sessions',
  description='The number of total sessions.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessiond_TotalCount_sessions{client="all", instance="$instance"}',
    legendFormat='Total Sessions'
  )
);

local overviewActiveSessions = singlestat.new(
  title='Active Sessions',
  description='The number of active sessions or in other words the number of sessions within the first two short term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessiond_ActiveCount_sessions{client="all", instance="$instance"}',
    legendFormat='Active Sessions'
  )
);

local overviewShortTermSessions = singlestat.new(
  title='Short Term Sessions',
  description='The number of sessions in the short term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessiond_ShortTermCount_sessions{client="all", instance="$instance"}',
    legendFormat='Short Term Sessions'
  )
);

local overviewLongTermSessions = singlestat.new(
  title='Long Term Sessions',
  description='The number of sessions in the long term containers.',
  datasource='Prometheus',
  decimals=0,
  valueName='avg',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'appsuite_sessiond_LongTermCount_sessions{client="all", instance="$instance"}',
    legendFormat='Long Term Sessions'
  )
);

local httpApiRequestsSeconds = graphPanel.new(
  title='Latencies: Percentiles & Max',
  description='HTTP API request times.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_alignAsTable=true,
  legend_rightSide=true,
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.5, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le))',
    legendFormat='p50'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.75, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le))',
    legendFormat='p75'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.9, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le))',
    legendFormat='p90'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.95, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le))',
    legendFormat='p95'
  )
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le))',
    legendFormat='p99'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_httpapi_requests_seconds_max{instance="$instance"})',
    legendFormat='max'
  )
);

local httpApiRequestsPercentilesByAction = graphPanel.new(
  title='99th percentile (group by action)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le, action))',
    legendFormat='{{action}}'
  )
);

local httpApiRequestsPercentilesByModule = graphPanel.new(
  title='99th percentile (group by module)',
  description='',
  datasource='Prometheus',
  nullPointMode='null as zero',
  min='0',
  format='s'
).addTarget(
  prometheus.target(
    'histogram_quantile(0.99, sum(rate(appsuite_httpapi_requests_seconds_bucket{instance="$instance"}[5m])) by (le, module))',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{status="OK", instance="$instance"}[5m]))',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{status!="OK", instance="$instance"}[5m]))',
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
    'sum(increase(appsuite_httpapi_requests_seconds_count{instance="$instance"}[5m]))',
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
    'sum(rate(appsuite_restapi_requests_timer_seconds_sum{instance="$instance"}[5m]) / rate(appsuite_restapi_requests_timer_seconds_count{instance="$instance"}[5m])) by (method)',
    legendFormat='{{method}}'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_restapi_requests_timer_seconds_max{instance="$instance"})',
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
    'sum(rate(appsuite_restapi_requests_timer_seconds_sum{instance="$instance"}[5m]) / rate(appsuite_restapi_requests_timer_seconds_count{instance="$instance"}[5m])) by (status)',
    legendFormat='{{status}}'
  )
).addTarget(
  prometheus.target(
    'sum(appsuite_restapi_requests_timer_seconds_max{instance="$instance"})',
    legendFormat='max'
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


dashboard.new(
  'AppSuite',
  tags=['java', 'appsuite-mw'],
  schemaVersion=22,
  refresh='1m',
  editable=true,
  graphTooltip='shared_crosshair',
).addTemplate(
  template.new(
    name='job',
    label='Job',
    hide='variable',
    datasource='Prometheus',
    query='label_values(jvm_info,job)',
    refresh='load'
  )
).addTemplate(
  template.new(
    name='host',
    label='Host',
    datasource='Prometheus',
    query='label_values(up{job=~"$job"}, job)',
    refresh='time',
  )
).addTemplate(
  template.new(
    name='instance',
    label='Instance',
    datasource='Prometheus',
    query='label_values(up{job=~"$host"}, instance)',
    refresh='time',
  )
)
.addPanels(
  [
    overviewRow { gridPos: { h: 1, w: 24, x: 0, y: 1 } },
    overviewTotalSessions { gridPos: { h: 6, w: 4, x: 0, y: 1 } },
    overviewActiveSessions { gridPos: { h: 6, w: 4, x: 4, y: 1 } },
    overviewShortTermSessions { gridPos: { h: 6, w: 4, x: 8, y: 1 } },
    overviewLongTermSessions { gridPos: { h: 6, w: 4, x: 12, y: 1 } },

    httpApiRow { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    httpApiRequestsOK { gridPos: { h: 6, w: 4, x: 0, y: 7 } },
    httpApiRequestsKO { gridPos: { h: 6, w: 4, x: 4, y: 7 } },
    httpApiRequestsTotal { gridPos: { h: 6, w: 4, x: 8, y: 7 } },

    httpApiRequestsSeconds { gridPos: { h: 8, w: 24, x: 0, y: 13 } },
    httpApiRequestsPercentilesByAction { gridPos: { h: 8, w: 12, x: 0, y: 21 } },
    httpApiRequestsPercentilesByModule { gridPos: { h: 8, w: 12, x: 12, y: 21 } },

    restApiRow { gridPos: { h: 1, w: 24, x: 0, y: 29 } },
    restApiRequestsByMethod { gridPos: { h: 8, w: 12, x: 0, y: 30 } },
    restApiRequestsByStatus { gridPos: { h: 8, w: 12, x: 12, y: 30 } },

    circuitBreakerRow { gridPos: { h: 1, w: 24, x: 0, y: 38 } },
    circuitBreakerIMAPStatus { gridPos: { h: 8, w: 24, x: 0, y: 39 } },
  ]
)
