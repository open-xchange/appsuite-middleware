local grafana = import 'grafonnet/grafana.libsonnet';
local dashboard = grafana.dashboard;
local template = grafana.template;
local annotation = grafana.annotation;

local prometheus_ds = 'Prometheus';

{
  newDashboard(title, metric, tags=[]):: dashboard.new(
    title=title,
    tags=tags,
    schemaVersion=22,
    refresh='1m',
    editable=true,
    graphTooltip='shared_crosshair',
  ).addTemplate(
    template.interval(
      name='interval',
      label='Interval',
      query='auto,1m,5m,1h,6h,1d',
      auto_count=200,
      auto_min='1s',
      current='5m',
    )
  ).addTemplate(
    template.new(
      name='job',
      label='Job',
      hide='variable',
      datasource=prometheus_ds,
      query='label_values(' + metric + ',job)',
      refresh='load'
    )
  ).addTemplate(
    template.new(
      name='instance',
      label='Instance',
      datasource=prometheus_ds,
      query='label_values(up{job=~"$job"}, instance)',
      refresh='time',
    )
  ).addAnnotations(
    [
      annotation.default,
      annotation.datasource(
        name='OX Events',
        datasource=annotation.default.datasource,
        iconColor='rgb(255, 255, 255)',
        type='tags',
        tags=[
          'OX',
        ],
      ),
    ]
  ),
}
