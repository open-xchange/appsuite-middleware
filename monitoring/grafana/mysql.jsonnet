local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local graphPanel = grafana.graphPanel;
local prometheus = grafana.prometheus;
local singlestat = grafana.singlestat;
local template = grafana.template;
local row = grafana.row;

local connectionRowConnections = graphPanel.new(
  title='MySQL Connections',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='max(max_over_time(mysql_global_status_threads_connected{instance=~"$instance"}[$interval]) or mysql_global_status_threads_connected{instance=~"$instance"})',
      legendFormat='Connections',
    ),
    prometheus.target(
      expr='mysql_global_status_max_used_connections{instance=~"$instance"}',
      legendFormat='Max Used Connections',
    ),
    prometheus.target(
      expr='mysql_global_variables_max_connections{instance=~"$instance"}',
      legendFormat='Max Connections',
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Max Connections',
    fill: 0,
  },
);

local connectionRowActivity = graphPanel.new(
  title='MySQL Client Thread Activity',
  description='**MySQL Active Threads**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='max_over_time(mysql_global_status_threads_connected{instance=~"$instance"}[$interval])',
      legendFormat='Peak Threads Connected',
    ),
    prometheus.target(
      expr='max_over_time(mysql_global_status_threads_running{instance=~"$instance"}[$interval])',
      legendFormat='Peak Threads Running',
    ),
    prometheus.target(
      expr='avg_over_time(mysql_global_status_threads_running{instance=~"$instance"}[$interval])',
      legendFormat='Avg Threads Running',
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Peak Threads Running',
    color: '#E24D42',
    lines: false,
    pointradius: 1,
    points: true,
  },
).addSeriesOverride(
  {
    alias: 'Peak Threads Connected',
    color: '#1F78C1',
  },
).addSeriesOverride(
  {
    alias: 'Avg Threads Running',
    color: '#EAB839',
  },
);

local commandRowCounters = graphPanel.new(
  title='Top Command Counters',
  description='**Top Command Counters**',
  decimals=2,
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
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
    expr='topk(5, rate(mysql_global_status_commands_total{instance=~"$instance"}[$interval])>0) or irate(mysql_global_status_commands_total{instance=~"$instance"}[$interval])>0',
    legendFormat='Com_{{ command }}',
  )
);

local commandRowCountersHourly = graphPanel.new(
  title='Top Command Counters Hourly',
  description='**Top Command Counters**',
  bars=true,
  decimals=2,
  fill=6,
  linewidth=2,
  lines=false,
  legend_alignAsTable=true,
  legend_rightSide=true,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  stack=true,
  time_from='24h',
).addTarget(
  prometheus.target(
    expr='topk(5, increase(mysql_global_status_commands_total{instance=~"$instance"}[1h])>0)',
    legendFormat='Com_{{ command }}',
    interval='1h',
    intervalFactor=1,
  )
);

local commandRowHandlers = graphPanel.new(
  title='MySQL Handlers',
  description='**MySQL Handlers**',
  decimals=2,
  fill=2,
  linewidth=2,
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
    expr='rate(mysql_global_status_handlers_total{instance=~"$instance", handler!~"commit|rollback|savepoint.*|prepare"}[$interval]) or irate(mysql_global_status_handlers_total{instance=~"$instance", handler!~"commit|rollback|savepoint.*|prepare"}[5m])',
    legendFormat='{{ handler }}',
    interval='$interval',
    intervalFactor=1,
  )
);

local commandRowTransactionHandlers = graphPanel.new(
  title='MySQL Transaction Handlers',
  description='**MySQL Transaction Handlers**',
  decimals=2,
  fill=2,
  linewidth=2,
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
    expr='rate(mysql_global_status_handlers_total{instance=~"$instance", handler=~"commit|rollback|savepoint.*|prepare"}[$interval]) or irate(mysql_global_status_handlers_total{instance=~"$host", handler=~"commit|rollback|savepoint.*|prepare"}[5m])',
    legendFormat='{{ handler }}',
    interval='$interval',
    intervalFactor=1,
  )
);

local memoryRowOverview = graphPanel.new(
  title='MySQL Internal Memory Overview',
  fill=6,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=true,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  legend_hideEmpty=true,
  legend_hideZero=true,
  stack=true,
  formatY1='bytes',
).addTargets(
  [
    prometheus.target(
      expr='mysql_global_status_innodb_page_size{instance=~"$instance"} * on (instance) mysql_global_status_buffer_pool_pages{instance=~"$instance",state="data"}',
      legendFormat='InnoDB Buffer Pool Data',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_innodb_log_buffer_size{instance=~"$instance"}',
      legendFormat='InnoDB Log Buffer Size',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_innodb_additional_mem_pool_size{instance=~"$instance"}',
      legendFormat='InnoDB Additional Memory Pool Size',
      interval='$interval',
      intervalFactor=2,
    ),
    prometheus.target(
      expr='mysql_global_status_innodb_mem_dictionary{instance=~"$instance"}',
      legendFormat='InnoDB Dictionary Size',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_key_buffer_size{instance=~"$instance"}',
      legendFormat='Key Buffer Size',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_query_cache_size{instance=~"$instance"}',
      legendFormat='Query Cache Size',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_status_innodb_mem_adaptive_hash{instance=~"$instance"}',
      legendFormat='Adaptive Hash Index Size',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
);

local tableLocksRowQuestions = graphPanel.new(
  title='MySQL Questions',
  description='**MySQL Questions**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTarget(
  prometheus.target(
    expr='rate(mysql_global_status_questions{instance=~"$instance"}[$interval]) or irate(mysql_global_status_questions{instance=~"$instance"}[5m])',
    legendFormat='Questions',
    interval='$interval',
    intervalFactor=1,
  )
);

local tableLocksRowThreadCache = graphPanel.new(
  title='MySQL Thread Cache',
  description='**MySQL Thread Cache**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='mysql_global_variables_thread_cache_size{instance=~"$instance"}',
      legendFormat='Thread Cache Size',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_status_threads_cached{instance=~"$instance"}',
      legendFormat='Threads Cached',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_threads_created{instance=~"$instance"}[$interval]) or irate(mysql_global_status_threads_created{instance=~"$instance"}[5m])',
      legendFormat='Threads Created',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Threads Created',
    fill: 0,
  }
);

local temporaryObjectsRowSorts = graphPanel.new(
  title='MySQL Temporary Objects',
  description='**MySQL Temporary Objects**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='rate(mysql_global_status_created_tmp_tables{instance=~"$instance"}[$interval]) or irate(mysql_global_status_created_tmp_tables{instance=~"$instance"}[5m])',
      legendFormat='Created Tmp Tables',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_created_tmp_disk_tables{instance=~"$instance"}[$interval]) or irate(mysql_global_status_created_tmp_disk_tables{instance=~"$instance"}[5m])',
      legendFormat='Created Tmp Disk Tables',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_created_tmp_files{instance=~"$instance"}[$interval]) or irate(mysql_global_status_created_tmp_files{instance=~"$instance"}[5m])',
      legendFormat='Created Tmp Files',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
);

local temporaryObjectsRowSlowQueries = graphPanel.new(
  title='MySQL Select Types',
  description='**MySQL Select Types**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='rate(mysql_global_status_select_full_join{instance=~"$instance"}[$interval]) or irate(mysql_global_status_select_full_join{instance=~"$instance"}[5m])',
      legendFormat='Select Full Join',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_select_full_range_join{instance=~"$instance"}[$interval]) or irate(mysql_global_status_select_full_range_join{instance=~"$instance"}[5m])',
      legendFormat='Select Full Range Join',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_select_range{instance=~"$instance"}[$interval]) or irate(mysql_global_status_select_range{instance=~"$instance"}[5m])',
      legendFormat='Select Range',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_select_range_check{instance=~"$instance"}[$interval]) or irate(mysql_global_status_select_range_check{instance=~"$instance"}[5m])',
      legendFormat='Select Range Check',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_select_scan{instance=~"$instance"}[$interval]) or irate(mysql_global_status_select_scan{instance=~"$instance"}[5m])',
      legendFormat='Select Scan',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
);

local queryCacheRowMemory = graphPanel.new(
  title='MySQL Query Cache Memory',
  description='**MySQL Query Cache Memory**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  formatY1='bytes',
).addTargets(
  [
    prometheus.target(
      expr='mysql_global_status_qcache_free_memory{instance=~"$instance"}',
      legendFormat='Free Memory',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_query_cache_size{instance=~"$instance"}',
      legendFormat='Query Cache Size',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
);

local queryCacheRowActivity = graphPanel.new(
  title='MySQL Query Cache Activity',
  description='**MySQL Query Cache Activity**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='rate(mysql_global_status_qcache_hits{instance=~"$instance"}[$interval]) or irate(mysql_global_status_qcache_hits{instance=~"$instance"}[5m])',
      legendFormat='Hits',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_qcache_inserts{instance=~"$instance"}[$interval]) or irate(mysql_global_status_qcache_inserts{instance=~"$instance"}[5m])',
      legendFormat='Inserts',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_qcache_not_cached{instance=~"$instance"}[$interval]) or irate(mysql_global_status_qcache_not_cached{instance=~"$instance"}[5m])',
      legendFormat='Not Cached',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_qcache_lowmem_prunes{instance=~"$instance"}[$interval]) or irate(mysql_global_status_qcache_lowmem_prunes{instance=~"$instance"}[5m])',
      legendFormat='Prunes',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_qcache_queries_in_cache{instance=~"$instance"}[$interval]) or irate(mysql_global_status_qcache_queries_in_cache{instance=~"$instance"}[5m])',
      legendFormat='Queries in Cache',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
);

local tableOpeningsRowCacheStatus = graphPanel.new(
  title='MySQL Table Open Cache Status',
  description='**MySQL Table Open Cache Status**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  formatY2='percentunit',
).addTargets(
  [
    prometheus.target(
      expr='rate(mysql_global_status_opened_tables{instance=~"$instance"}[$interval]) or irate(mysql_global_status_opened_tables{instance=~"$instance"}[5m])',
      legendFormat='Openings',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[5m])',
      legendFormat='Hits',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_table_open_cache_misses{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_misses{instance=~"$instance"}[5m])',
      legendFormat='Misses',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_table_open_cache_overflows{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_overflows{instance=~"$instance"}[5m])',
      legendFormat='Misses due to Overflows',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='(rate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[5m]))/((rate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_hits{instance=~"$instance"}[5m]))+(rate(mysql_global_status_table_open_cache_misses{instance=~"$instance"}[$interval]) or irate(mysql_global_status_table_open_cache_misses{instance=~"$instance"}[5m])))',
      legendFormat='Table Open Cache Hit Ratio',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Table Open Cache Hit Ratio',
    yaxis: 2,
  }
);

local tableOpeningsRowOpenTables = graphPanel.new(
  title='MySQL Open Tables',
  description='**MySQL Open Tables**',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  formatY2='percentunit',
).addTargets(
  [
    prometheus.target(
      expr='mysql_global_status_open_tables{instance=~"$instance"}',
      legendFormat='Open Tables',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='mysql_global_variables_table_open_cache{instance=~"$instance"}',
      legendFormat='Table Open Cache',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Table Open Cache Hit Ratio',
    yaxis: 2,
  }
);

local networkRowTraffic = graphPanel.new(
  title='MySQL Network Traffic',
  description='**MySQL Network Traffic**',
  decimals=2,
  fill=6,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  stack=true,
  formatY1='Bps',
).addTargets(
  [
    prometheus.target(
      expr='rate(mysql_global_status_bytes_received{instance=~"$instance"}[$interval]) or irate(mysql_global_status_bytes_received{instance=~"$instance"}[5m])',
      legendFormat='Inbound',
      interval='$interval',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='rate(mysql_global_status_bytes_sent{instance=~"$instance"}[$interval]) or irate(mysql_global_status_bytes_sent{instance=~"$instance"}[5m])',
      legendFormat='Outbound',
      interval='$interval',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Table Open Cache Hit Ratio',
    yaxis: 2,
  }
);

local networkRowTrafficHourly = graphPanel.new(
  title='MySQL Network Usage Hourly',
  description='**MySQL Network Usage Hourly**',
  decimals=2,
  fill=6,
  bars=true,
  lines=false,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=true,
  legend_avg=true,
  legend_values=true,
  stack=true,
  formatY1='bytes',
  time_from='24h',
).addTargets(
  [
    prometheus.target(
      expr='increase(mysql_global_status_bytes_received{instance=~"$instance"}[1h])',
      legendFormat='Received',
      interval='1h',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='increase(mysql_global_status_bytes_sent{instance=~"$instance"}[1h])',
      legendFormat='Sent',
      interval='1h',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Table Open Cache Hit Ratio',
    yaxis: 2,
  }
);

local overviewRowUptime = singlestat.new(
  title='MySQL Uptime',
  description='**MySQL Uptime**',
  valueName='current',
  colorValue=true,
  colors=[
    'rgba(245, 54, 54, 0.9)',
    'rgba(237, 129, 40, 0.89)',
    'rgba(50, 172, 45, 0.97)',
  ],
  decimals=1,
  format='s',
  postfix='s',
  prefixFontSize='80%',
  postfixFontSize='80%',
  thresholds='300,3600',
).addTarget(
  prometheus.target(
    expr='mysql_global_status_uptime{instance=~"$instance"}',
  )
);

local overviewRowQPS = singlestat.new(
  title='Current QPS',
  description='**Current QPS**',
  decimals=2,
  sparklineShow=true,
).addTarget(
  prometheus.target(
    expr='rate(mysql_global_status_queries{instance=~"$instance"}[$interval]) or irate(mysql_global_status_queries{instance=~"$instance"}[$interval])',
  )
);

local overviewRowInnoDB = singlestat.new(
  title='InnoDB Buffer Pool Size',
  thresholds='90,95',
  format='bytes',
).addTarget(
  prometheus.target(
    expr='mysql_global_variables_innodb_buffer_pool_size{instance=~"$instance"}',
  )
);

local overviewRowDBVersion = singlestat.new(
  title='MySQL Version',
  valueName='name',
).addTarget(
  prometheus.target(
    expr='mysql_version_info{instance=~"$instance"}',
    legendFormat='{{version}}',
  )
);

grafana.newDashboard(
  title='MySQL',
  tags=['MySQL', 'MariaDB'],
  metric='mysql_up'
).addTemplate(
  template.new(
    name='instance',
    label='Instance',
    datasource=grafana.default.datasource,
    query='label_values(up{job=~"$job"}, instance)',
    refresh='time',
    sort=1,
  ),
).addPanels(
  [
    row.new(
      title='Overview'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 1 } },
    overviewRowDBVersion { gridPos: { h: 4, w: 4, x: 0, y: 1 } },
    overviewRowUptime { gridPos: { h: 4, w: 4, x: 4, y: 1 } },
    overviewRowQPS { gridPos: { h: 4, w: 4, x: 8, y: 1 } },
    overviewRowInnoDB { gridPos: { h: 4, w: 4, x: 12, y: 1 } },
  ] + [
    row.new(
      title='Connections',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 5 } },
    connectionRowConnections { gridPos: { h: 8, w: 12, x: 0, y: 5 } },
    connectionRowActivity { gridPos: { h: 8, w: 12, x: 12, y: 5 } },
  ] + [
    row.new(
      title='Command & Handlers',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 13 } },
    commandRowCounters { gridPos: { h: 8, w: 24, x: 0, y: 14 } },
    commandRowCountersHourly { gridPos: { h: 8, w: 24, x: 0, y: 22 } },
    commandRowHandlers { gridPos: { h: 8, w: 24, x: 0, y: 30 } },
    commandRowTransactionHandlers { gridPos: { h: 8, w: 24, x: 0, y: 38 } },
  ] + [
    row.new(
      title='Memory',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 46 } },
    memoryRowOverview { gridPos: { h: 8, w: 24, x: 0, y: 47 } },
  ] + [
    row.new(
      title='Table Locks',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 55 } },
    tableLocksRowQuestions { gridPos: { h: 8, w: 12, x: 0, y: 56 } },
    tableLocksRowThreadCache { gridPos: { h: 8, w: 12, x: 12, y: 56 } },
  ] + [
    row.new(
      title='Temporary Objects',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 64 } },
    temporaryObjectsRowSorts { gridPos: { h: 8, w: 12, x: 0, y: 65 } },
    temporaryObjectsRowSlowQueries { gridPos: { h: 8, w: 12, x: 12, y: 65 } },
  ] + [
    row.new(
      title='Query Cache',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 73 } },
    queryCacheRowMemory { gridPos: { h: 8, w: 12, x: 0, y: 74 } },
    queryCacheRowActivity { gridPos: { h: 8, w: 12, x: 12, y: 74 } },
  ] + [
    row.new(
      title='Table Openings',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 82 } },
    tableOpeningsRowCacheStatus { gridPos: { h: 8, w: 12, x: 0, y: 83 } },
    tableOpeningsRowOpenTables { gridPos: { h: 8, w: 12, x: 12, y: 83 } },
  ] + [
    row.new(
      title='Network',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 91 } },
    networkRowTraffic { gridPos: { h: 8, w: 12, x: 0, y: 92 } },
    networkRowTrafficHourly { gridPos: { h: 8, w: 12, x: 12, y: 92 } },
  ]
)
