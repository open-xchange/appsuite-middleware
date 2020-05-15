local grafana = import 'grafonnet/grafana.libsonnet';
local dashboard = grafana.dashboard;
local template = grafana.template;
local annotation = grafana.annotation;

{
  default::  
  {
    datasource: 'Prometheus',
  },
  newDashboard(title, metric, tags=[]):: dashboard.new(
    title=title,
    tags=tags,
    schemaVersion=22,
    refresh='1m',
    editable=true,
    graphTooltip='shared_crosshair',
  ).addTemplates(
    [
      template.interval(
        name='interval',
        label='Interval',
        query='auto,1m,5m,1h,6h,1d',
        auto_count=200,
        auto_min='1s',
        current='5m',
      ),
      template.new(
        name='job',
        label='Job',
        hide='variable',
        datasource=self.default.datasource,
        query='label_values(' + metric + ',job)',
        refresh='load'
      ),
    ]
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
