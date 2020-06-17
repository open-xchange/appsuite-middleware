local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local row = grafana.row;
local prometheus = grafana.prometheus;
local template = grafana.template;
local link = grafana.link;

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
    name: 'mem_pools_heap',
    label: 'Memory Pools Heap',
    query: 'label_values(jvm_memory_used_bytes{instance="$instance", area="heap"}, id)',
    hide: 'variable',
    includeAll: true,
  },
  {
    name: 'mem_pools_nonheap',
    label: 'Memory Pools Non-Heap',
    query: 'label_values(jvm_memory_used_bytes{instance="$instance", area="nonheap"}, id)',
    hide: 'variable',
    includeAll: true,
  },
];

local threadThreads = graphPanel.new(
  title='Threads',
  datasource=grafana.default.datasource,
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  fill=2,
  linewidth=2,
  min='0',
).addTargets(
  [
    prometheus.target(
      'jvm_threads_live_threads{instance=~"$instance"}',
      legendFormat='Live',
    ),
    prometheus.target(
      'jvm_threads_daemon_threads{instance=~"$instance"}',
      legendFormat='Daemon',
    ),
    prometheus.target(
      'jvm_threads_peak_threads{instance=~"$instance"}',
      legendFormat='Peak',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Peak',
    fill: 0,
  },
);

local threadThreadStates = graphPanel.new(
  title='Thread States',
  datasource=grafana.default.datasource,
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  fill=2,
  linewidth=2,
  min='0',
).addTarget(
  prometheus.target(
    'jvm_threads_states_threads{instance=~"$instance"}',
    legendFormat='{{state}}',
  )
).addSeriesOverride(
  {
    alias: 'Peak',
    fill: 0,
  },
);

local overviewHeapUsage = graphPanel.new(
  title='Heap',
  description='Used bytes of a given JVM memory area.',
  datasource=grafana.default.datasource,
  aliasColors={
    Max: 'dark-red',
  },
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  fill=2,
  linewidth=2,
  format='decbytes',
  min='0',
).addTargets(
  [
    prometheus.target(
      'sum(jvm_memory_used_bytes{instance=~"$instance", area="heap"})',
      legendFormat='Used',
    ),
    prometheus.target(
      'sum(jvm_memory_committed_bytes{instance=~"$instance", area="heap"})',
      legendFormat='Committed',
    ),
    prometheus.target(
      'sum(jvm_memory_max_bytes{instance=~"$instance", area="heap"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Max',
    fill: 0,
  },
);

local overviewNonHeapUsage = graphPanel.new(
  title='Non-Heap',
  description='Used bytes of a given JVM memory area.',
  datasource=grafana.default.datasource,
  nullPointMode='null as zero',
  aliasColors={
    Max: 'dark-red',
  },
  fill=2,
  linewidth=2,
  legend_max=true,
  legend_current=true,
  format='decbytes',
  min='0',
).addTargets(
  [
    prometheus.target(
      'sum(jvm_memory_used_bytes{instance=~"$instance", area="nonheap"})',
      legendFormat='Used',
    ),
    prometheus.target(
      'sum(jvm_memory_committed_bytes{instance=~"$instance", area="nonheap"})',
      legendFormat='Committed',
    ),
    prometheus.target(
      'sum(jvm_memory_max_bytes{instance=~"$instance", area="nonheap"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Max',
    fill: 0,
  },
);

local overviewUptime = singlestat.new(
  title='Uptime',
  datasource=grafana.default.datasource,
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
    'process_uptime_seconds{instance=~"$instance"}'
  )
);

local overviewThreads = singlestat.new(
  title='Threads',
  description='Current thread count of a JVM.',
  datasource=grafana.default.datasource,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    'jvm_threads_live_threads{instance=~"$instance"}'
  )
);

local overviewClassesLoaded = singlestat.new(
  title='Classes loaded',
  datasource=grafana.default.datasource,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    'jvm_classes_loaded_classes{instance=~"$instance"}'
  )
);

local overviewClassesUnloaded = singlestat.new(
  title='Classes unloaded',
  datasource=grafana.default.datasource,
  valueName='current',
  sparklineShow=true,
).addTarget(
  prometheus.target(
    'jvm_classes_unloaded_classes_total{instance=~"$instance"}'
  )
);

local gcDuration = graphPanel.new(
  title='GC duration',
  description='Used bytes of a given JVM memory area.',
  datasource=grafana.default.datasource,
  nullPointMode='null as zero',
  fill=2,
  linewidth=2,
  format='s',
).addTarget(
  prometheus.target(
    'rate(jvm_gc_collection_seconds_sum{instance=~"$instance"}[$interval])/rate(jvm_gc_collection_seconds_count{instance=~"$instance"}[$interval])',
    legendFormat='{{gc}}'
  )
);

local gcDurationCount = graphPanel.new(
  title='Collection',
  description='Used bytes of a given JVM memory area.',
  datasource=grafana.default.datasource,
  nullPointMode='null as zero',
  fill=2,
  linewidth=2,
  decimals=2,
  format='ops',
).addTarget(
  prometheus.target(
    'rate(jvm_gc_collection_seconds_count{instance=~"$instance"}[$interval])',
    legendFormat='{{gc}}'
  )
);

local memoryPoolsHeap = graphPanel.new(
  title='$mem_pools_heap',
  datasource=grafana.default.datasource,
  aliasColors={
    Max: 'dark-red',
  },
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  fill=2,
  linewidth=2,
  format='decbytes',
  min='0',
  repeat='mem_pools_heap',
  repeatDirection='h',
).addTargets(
  [
    prometheus.target(
      'sum(jvm_memory_used_bytes{instance=~"$instance", area="heap", id=~"$mem_pools_heap"})',
      legendFormat='Used',
    ),
    prometheus.target(
      'sum(jvm_memory_committed_bytes{instance=~"$instance", area="heap", id=~"$mem_pools_heap"})',
      legendFormat='Committed',
    ),
    prometheus.target(
      'sum(jvm_memory_max_bytes{instance=~"$instance", area="heap", id=~"$mem_pools_heap"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Max',
    fill: 0,
  },
);

local memoryPoolsNonHeap = graphPanel.new(
  title='$mem_pools_nonheap',
  datasource=grafana.default.datasource,
  aliasColors={
    Max: 'dark-red',
  },
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  fill=2,
  linewidth=2,
  format='decbytes',
  min='0',
  repeat='mem_pools_nonheap',
  repeatDirection='h',
).addTargets(
  [
    prometheus.target(
      'sum(jvm_memory_used_bytes{instance=~"$instance", area="nonheap", id=~"$mem_pools_nonheap"})',
      legendFormat='Used',
    ),
    prometheus.target(
      'sum(jvm_memory_committed_bytes{instance=~"$instance", area="nonheap", id=~"$mem_pools_nonheap"})',
      legendFormat='Committed',
    ),
    prometheus.target(
      'sum(jvm_memory_max_bytes{instance=~"$instance", area="nonheap", id=~"$mem_pools_nonheap"})',
      legendFormat='Max',
    ),
  ]
).addSeriesOverride(
  {
    alias: 'Max',
    fill: 0,
  },
);

grafana.newDashboard(
  title='JVM',
  tags=['Java'],
  metric='jvm_info'
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
      sort=if std.objectHas(obj, 'sort') then obj.sort else 0,
      regex=if std.objectHas(obj, 'regex') then obj.regex else '',
      refresh='load',
    )
    for obj in templates
  ]
).addPanels(
  [
    row.new(
      title='Overview'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 0 } },
    overviewUptime { gridPos: { h: 4, w: 3, x: 0, y: 1 } },
    overviewThreads { gridPos: { h: 4, w: 3, x: 3, y: 1 } },
    overviewHeapUsage { gridPos: { h: 8, w: 9, x: 6, y: 1 } },
    overviewNonHeapUsage { gridPos: { h: 8, w: 9, x: 15, y: 1 } },
    overviewClassesLoaded { gridPos: { h: 4, w: 3, x: 0, y: 5 } },
    overviewClassesUnloaded { gridPos: { h: 4, w: 3, x: 3, y: 5 } },
  ] + [
    row.new(
      title='Memory Pools (Heap)'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    memoryPoolsHeap { gridPos: { h: 8, w: 8, x: 0, y: 8 } },
  ] + [
    row.new(
      title='Memory Pools (Non-Heap)'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 16 } },
    memoryPoolsNonHeap { gridPos: { h: 8, w: 8, x: 0, y: 17 } },
  ] + [
    row.new(
      title='Garbage Collector'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 25 } },
    gcDuration { gridPos: { h: 8, w: 12, x: 0, y: 26 } },
    gcDurationCount { gridPos: { h: 8, w: 12, x: 12, y: 26 } },
  ] + [
    row.new(
      title='Thread'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 34 } },
    threadThreads { gridPos: { h: 8, w: 12, x: 0, y: 35 } },
    threadThreadStates { gridPos: { h: 8, w: 12, x: 12, y: 35 } },
  ]
)
