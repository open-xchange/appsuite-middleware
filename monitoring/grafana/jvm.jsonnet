local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local singlestat = grafana.singlestat;
local graphPanel = grafana.graphPanel;
local row = grafana.row;
local prometheus = grafana.prometheus;

local memoryHeapUsage = graphPanel.new(
  title='Heap',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  format='decbytes',
  min='0'
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_used{instance=~"$instance", area="heap"}',
    legendFormat='Used',
  )
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_committed{instance=~"$instance", area="heap"}',
    legendFormat='Committed',
  )
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_max{instance=~"$instance", area="heap"}',
    legendFormat='Max',
  )
);

local memoryNonHeapUsage = graphPanel.new(
  title='Non-Heap',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  format='decbytes',
  min='0'
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_used{instance=~"$instance", area="nonheap"}',
    legendFormat='Used',
  )
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_committed{instance=~"$instance", area="nonheap"}',
    legendFormat='Committed',
  )
).addTarget(
  prometheus.target(
    'jvm_memory_bytes_max{instance=~"$instance", area="nonheap"}',
    legendFormat='Max',
  )
);

local memoryPoolOldNewGen = graphPanel.new(
  title='Old/New Gen usage',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  format='decbytes',
  min='0'
).addTarget(
  prometheus.target(
    'jvm_memory_pool_bytes_used{instance=~"$instance", pool="CMS Old Gen"}',
    legendFormat='Old-Gen',
  )
).addTarget(
  prometheus.target(
    'jvm_memory_pool_bytes_used{instance=~"$instance", pool="Par Eden Space"}',
    legendFormat='New-Gen',
  )
);

local memoryPools = graphPanel.new(
  title='Pools',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  legend_max=true,
  legend_current=true,
  format='decbytes',
  min='0'
).addTarget(
  prometheus.target(
    'jvm_memory_pool_bytes_used{instance=~"$instance"}',
    legendFormat='{{pool}}',
  )
);

local overviewThreads = singlestat.new(
  title='Threads',
  description='Current thread count of a JVM.',
  datasource='Prometheus',
  valueName='current',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'jvm_threads_current{instance=~"$instance"}'
  )
);

local overviewClassesLoaded = singlestat.new(
  title='Loaded classes',
  description='The number of classes that are currently loaded in the JVM.',
  datasource='Prometheus',
  valueName='current',
  sparklineShow=true
).addTarget(
  prometheus.target(
    'jvm_classes_loaded{instance=~"$instance"}'
  )
);

local gcDuration = graphPanel.new(
  title='GC duration',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  format='s'
).addTarget(
  prometheus.target(
    'rate(jvm_gc_collection_seconds_sum{instance=~"$instance"}[$interval])/rate(jvm_gc_collection_seconds_count{instance=~"$instance"}[$interval])',
    legendFormat='{{gc}}'
  )
);

local gcDurationCount = graphPanel.new(
  title='Collection',
  description='Used bytes of a given JVM memory area.',
  datasource='Prometheus',
  nullPointMode='null as zero',
  decimals=2,
  format='ops'
).addTarget(
  prometheus.target(
    'rate(jvm_gc_collection_seconds_count{instance=~"$instance"}[$interval])',
    legendFormat='{{gc}}'
  )
);

grafana.newDashboard(
  title='JVM',
  tags=['java'],
  metric='jvm_info'
).addPanels(
  [
    row.new(
      title='Overview'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 0 } },
    overviewThreads { gridPos: { h: 6, w: 4, x: 0, y: 1 } },
    overviewClassesLoaded { gridPos: { h: 6, w: 4, x: 4, y: 1 } },
  ] + [
    row.new(
      title='Memory'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 7 } },
    memoryHeapUsage { gridPos: { h: 8, w: 12, x: 0, y: 8 } },
    memoryNonHeapUsage { gridPos: { h: 8, w: 12, x: 12, y: 8 } },
  ] + [
    row.new(
      title='Memory Pool'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 16 } },
    memoryPoolOldNewGen { gridPos: { h: 8, w: 12, x: 0, y: 17 } },
    memoryPools { gridPos: { h: 8, w: 12, x: 12, y: 17 } },
  ] + [
    row.new(
      title='Garbage Collector'
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 25 } },
    gcDuration { gridPos: { h: 8, w: 12, x: 0, y: 26 } },
    gcDurationCount { gridPos: { h: 8, w: 12, x: 12, y: 26 } },
  ]
)
