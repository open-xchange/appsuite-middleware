local grafana = (import 'grafonnet/grafana.libsonnet')
                + (import './lib/ox_functions.libsonnet');
local graphPanel = grafana.graphPanel;
local prometheus = grafana.prometheus;
local singlestat = grafana.singlestat;
local template = grafana.template;
local row = grafana.row;

local overviewRowUptime = singlestat.new(
  title='System Uptime',
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
    expr='sum(time() - node_boot_time_seconds{instance=~"$instance"}) or sum(time() - node_boot_time{instance=~"$instance"})',
    instant=true,
  )
);

local overviewRowTotalRAM = singlestat.new(
  title='Total RAM',
  valueName='current',
  decimals=2,
  format='bytes',
).addTarget(
  prometheus.target(
    expr='sum(node_memory_MemTotal_bytes{instance=~"$instance"}) or sum(node_memory_MemTotal{instance=~"$instance"})',
    legendFormat='{{instance}}',
    instant=true,
  )
);

local overviewRowCPUCores = singlestat.new(
  title='CPU Cores',
  valueName='current',
).addTarget(
  prometheus.target(
    expr='sum(count(node_cpu_seconds_total{instance=~"$instance", mode="system"}) or count(node_cpu{instance=~"$instance", mode="system"}) by (cpu))',
    instant=true,
  )
);

local overviewRowIOWait = singlestat.new(
  title='IOWait',
  valueName='avg',
  colorValue=true,
  decimals=2,
  format='percent',
  sparklineShow=true,
  thresholds='30,40,50',
).addTarget(
  prometheus.target(
    expr='(avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="iowait"}[30m])) or avg(irate(node_cpu{instance=~"$instance",mode="iowait"}[30m]))) * 100',
  )
);

// TODO switch to gauge
local overviewRowCPUUsage = singlestat.new(
  title='CPU usage',
  thresholds='60,80',
  format='percent',
  valueName='current',
  gaugeShow=true,
).addTarget(
  prometheus.target(
    expr='100 - (avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="idle"}[30m])) * 100) OR\n100 - (avg(irate(node_cpu{instance=~"$instance",mode="idle"}[30m])) * 100)',
    instant=true,
  )
);

// TODO switch to gauge
local overviewRowRAMUsage = singlestat.new(
  title='RAM usage',
  thresholds='70,80,90',
  format='percent',
  valueName='current',
  gaugeShow=true,
).addTarget(
  prometheus.target(
    expr='(1 - (node_memory_MemAvailable_bytes{instance=~"$instance"} / (node_memory_MemTotal_bytes{instance=~"$instance"})))* 100 OR\n(1 - (node_memory_MemAvailable{instance=~"$instance"} / (node_memory_MemTotal{instance=~"$instance"})))* 100',
    instant=true,
  )
);

// TODO switch to gauge
local overviewRowSwapUsage = singlestat.new(
  title='Swap usage',
  thresholds='50,70,90',
  format='percent',
  valueName='current',
  gaugeShow=true,
).addTarget(
  prometheus.target(
    expr='(1 - (node_memory_SwapFree_bytes{instance=~"$instance"} / node_memory_SwapTotal_bytes{instance=~"$instance"})) * 100 OR\n(1 - (node_memory_SwapFree{instance=~"$instance"} / node_memory_SwapTotal{instance=~"$instance"})) * 100',
    instant=true,
  )
);

// TODO switch to gauge
local overviewRowFSUsage = singlestat.new(
  title='Filesystem usage',
  thresholds='60,80',
  format='percent',
  valueName='current',
  gaugeShow=true,
).addTarget(
  prometheus.target(
    expr='topk(1, 100 - ((node_filesystem_avail_bytes{instance=~"$instance",fstype=~"ext[2-4]|[x|z]fs"} * 100) / node_filesystem_size_bytes {instance=~"$instance",fstype=~"ext[2-4]|[x|z]fs"})) OR topk(1, 100 - ((node_filesystem_avail{instance=~"$instance",fstype=~"ext[2-4]|[x|z]fs"} * 100) / node_filesystem_size {instance=~"$instance",fstype=~"ext[2-4]|[x|z]fs"}))',
    instant=true,
  )
);

local cpuRowLoad = graphPanel.new(
  title='Load',
  description='The load average.',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  nullPointMode='null as zero',
).addTargets(
  [
    prometheus.target(
      expr='node_load1{instance=~"$instance"}',
      legendFormat='shortterm (1m)',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_load5{instance=~"$instance"}',
      legendFormat='midterm (5m)',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_load15{instance=~"$instance"}',
      legendFormat='longterm (15m)',
      intervalFactor=1,
    ),
  ],
);

local cpuRowCPU = graphPanel.new(
  title='CPU',
  description='Average time cpus spent in each mode.',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  formatY1='percentunit',
).addTargets(
  [
    prometheus.target(
      expr='avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="system"}[30m])) OR avg(irate(node_cpu{instance=~"$instance",mode="system"}[30m]))',
      legendFormat='System',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="user"}[30m])) OR avg(irate(node_cpu{instance=~"$instance",mode="user"}[30m]))',
      legendFormat='User',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="iowait"}[30m])) OR avg(irate(node_cpu{instance=~"$instance",mode="iowait"}[30m]))',
      legendFormat='Iowait',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='1-avg(irate(node_cpu_seconds_total{instance=~"$instance",mode="idle"}[30m])) OR 1-avg(irate(node_cpu{instance=~"$instance",mode="idle"}[30m]))',
      legendFormat='Idle',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Idle',
    color: '#C4162A',
    fill: 0,
  }
);

local cpuRowProcesses = graphPanel.new(
  title='Processes',
  description='Number of processes blocked *(waiting for I/O to complete)* and in runnable state.',
  decimals=2,
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='node_procs_blocked{instance=~"$instance"}',
      legendFormat='Blocked',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_procs_running{instance=~"$instance"}',
      legendFormat='Running',
      intervalFactor=1,
    ),
  ],
);

local memoryRowMemory = graphPanel.new(
  title='Memory',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  formatY1='bytes',
).addTargets(
  [
    prometheus.target(
      expr='node_memory_MemTotal_bytes{instance=~"$instance"} OR node_memory_MemTotal{instance=~"$instance"}',
      legendFormat='Total',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_memory_MemTotal_bytes{instance=~"$instance"} - node_memory_MemAvailable_bytes{instance=~"$instance"} OR node_memory_MemTotal{instance=~"$instance"} - node_memory_MemAvailable{instance=~"$instance"}',
      legendFormat='Used',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_memory_MemAvailable_bytes{instance=~"$instance"} OR node_memory_MemAvailable{instance=~"$instance"}',
      legendFormat='Available',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Total',
    color: '#C4162A',
    fill: 0,
  }
);

local kernelRowForks = graphPanel.new(
  title='Forks',
  description='Number of forks.',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  formatY1='ops',
).addTarget(
  prometheus.target(
    expr='irate(node_forks_total{instance=~"$instance"}[$interval]) OR irate(node_forks{instance=~"$instance"}[$interval])',
    legendFormat='Forks',
    intervalFactor=1,
  )
);

local kernelRowSwitches = graphPanel.new(
  title='Context Switches',
  description='Number of context switches.',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  formatY1='ops',
).addTarget(
  prometheus.target(
    expr='irate(node_context_switches_total{instance=~"$instance"}[$interval]) OR irate(node_context_switches{instance=~"$instance"}[$interval])',
    legendFormat='Context Switches',
    intervalFactor=1,
  )
);

local kernelRowFD = graphPanel.new(
  title='File Descriptors',
  description='File descriptor information.',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='avg',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
).addTargets(
  [
    prometheus.target(
      expr='node_filefd_allocated{instance=~"$instance"}',
      legendFormat='Allocated',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='node_filefd_maximum{instance=~"$instance"}',
      legendFormat='Maximum',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: 'Maximum',
    color: '#C4162A',
    fill: 0,
  }
);

local networkRowTraffic = graphPanel.new(
  title='Traffic',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=true,
  legend_sort='current',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=false,
  legend_current=true,
  legend_values=true,
  legend_hideEmpty=true,
  legend_hideZero=true,
  formatY1='bps',
  sort='decreasing',
).addTargets(
  [
    prometheus.target(
      expr='irate(node_network_receive_bytes_total{instance=~"$instance",device!~"tap.*|veth.*|br.*|docker.*|virbr*|lo*"}[30m])*8 OR irate(node_network_receive_bytes{instance=~"$instance",device!~"tap.*|veth.*|br.*|docker.*|virbr*|lo*"}[30m])*8',
      legendFormat='{{device}}_Receive',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='irate(node_network_transmit_bytes_total{instance=~"$instance",device!~"tap.*|veth.*|br.*|docker.*|virbr*|lo*"}[30m])*8 OR irate(node_network_transmit_bytes{instance=~"$instance",device!~"tap.*|veth.*|br.*|docker.*|virbr*|lo*"}[30m])*8',
      legendFormat='{{device}}_Transmit',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: '/.*_Transmit$/',
    transform: 'negative-Y',
  }
);

local diskRowIops = graphPanel.new(
  title='Disk IOPS',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='current',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  legend_hideEmpty=true,
  legend_hideZero=true,
  formatY1='iops',
  sort='decreasing',
).addTargets(
  [
    prometheus.target(
      expr='irate(node_disk_reads_completed_total{instance=~"$instance"}[30m]) OR irate(node_disk_reads_completed{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Reads',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='irate(node_disk_writes_completed_total{instance=~"$instance"}[30m]) OR irate(node_disk_writes_completed{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Writes',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: '/.*_Writes$/',
    transform: 'negative-Y',
  }
);

local diskRowIoBytes = graphPanel.new(
  title='Disk R/W Bytes',
  fill=2,
  linewidth=2,
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='current',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  legend_hideEmpty=true,
  legend_hideZero=true,
  formatY1='Bps',
  sort='decreasing',
).addTargets(
  [
    prometheus.target(
      expr='irate(node_disk_read_bytes_total{instance=~"$instance"}[30m]) OR irate(node_disk_bytes_read{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Read',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='irate(node_disk_written_bytes_total{instance=~"$instance"}[30m]) OR irate(node_disk_bytes_written{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Written',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: '/.*_Written$/',
    transform: 'negative-Y',
  }
);

local diskRowIoTime = graphPanel.new(
  title='Disk R/W Time',
  fill=2,
  linewidth=2,
  nullPointMode='null as zero',
  legend_alignAsTable=true,
  legend_rightSide=false,
  legend_sort='current',
  legend_sortDesc=true,
  legend_max=true,
  legend_min=false,
  legend_avg=true,
  legend_current=true,
  legend_values=true,
  legend_hideEmpty=true,
  legend_hideZero=true,
  formatY1='s',
  sort='decreasing',
).addTargets(
  [
    prometheus.target(
      expr='irate(node_disk_read_time_seconds_total{instance=~"$instance"}[30m]) / irate(node_disk_reads_completed_total{instance=~"$instance"}[30m]) OR\n      irate(node_disk_read_time_ms{instance=~"$instance"}[30m]) / irate(node_disk_reads_completed{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Read',
      intervalFactor=1,
    ),
    prometheus.target(
      expr='irate(node_disk_write_time_seconds_total{instance=~"$instance"}[30m]) / irate(node_disk_writes_completed_total{instance=~"$instance"}[30m]) OR\n      irate(node_disk_write_time_ms{instance=~"$instance"}[30m]) / irate(node_disk_writes_completed{instance=~"$instance"}[30m])',
      legendFormat='{{device}}_Write',
      intervalFactor=1,
    ),
  ],
).addSeriesOverride(
  {
    alias: '/.*_Write$/',
    transform: 'negative-Y',
  }
);

grafana.newDashboard(
  title='Operating System',
  tags=['OS'],
  metric='node_uname_info'
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
      title='Overview',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 1 } },
    overviewRowUptime { gridPos: { h: 4, w: 4, x: 0, y: 2 } },
    overviewRowTotalRAM { gridPos: { h: 4, w: 4, x: 4, y: 2 } },
    overviewRowCPUCores { gridPos: { h: 4, w: 4, x: 8, y: 2 } },
    overviewRowIOWait { gridPos: { h: 4, w: 4, x: 12, y: 2 } },
    //
    overviewRowCPUUsage { gridPos: { h: 4, w: 4, x: 0, y: 6 } },
    overviewRowRAMUsage { gridPos: { h: 4, w: 4, x: 4, y: 6 } },
    overviewRowSwapUsage { gridPos: { h: 4, w: 4, x: 8, y: 6 } },
    overviewRowFSUsage { gridPos: { h: 4, w: 4, x: 12, y: 6 } },
  ] + [
    row.new(
      title='CPU',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 10 } },
    cpuRowLoad { gridPos: { h: 8, w: 24, x: 0, y: 11 } },
    //
    cpuRowCPU { gridPos: { h: 8, w: 12, x: 0, y: 19 } },
    cpuRowProcesses { gridPos: { h: 8, w: 12, x: 12, y: 19 } },
  ] + [
    row.new(
      title='Memory',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 27 } },
    memoryRowMemory { gridPos: { h: 8, w: 24, x: 0, y: 28 } },
  ] + [
    row.new(
      title='Kernel',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 36 } },
    kernelRowForks { gridPos: { h: 8, w: 8, x: 0, y: 37 } },
    kernelRowSwitches { gridPos: { h: 8, w: 8, x: 8, y: 37 } },
    kernelRowFD { gridPos: { h: 8, w: 8, x: 16, y: 37 } },
  ] + [
    row.new(
      title='Network',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 45 } },
    networkRowTraffic { gridPos: { h: 8, w: 24, x: 0, y: 46 } },
  ] + [
    row.new(
      title='Disk',
    ) + { gridPos: { h: 1, w: 24, x: 0, y: 54 } },
    diskRowIops { gridPos: { h: 8, w: 8, x: 0, y: 55 } },
    diskRowIoBytes { gridPos: { h: 8, w: 8, x: 8, y: 55 } },
    diskRowIoTime { gridPos: { h: 8, w: 8, x: 16, y: 55 } },
  ]
)
