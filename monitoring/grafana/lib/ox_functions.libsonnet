local grafana = import 'grafonnet/grafana.libsonnet';
local dashboard = grafana.dashboard;
local template = grafana.template;

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
      current='auto',
    )
  ).addTemplate(
    template.new(
      name='job',
      label='Job',
      hide='variable',
      datasource='Prometheus',
      query='label_values(' + metric + ',job)',
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
  ),
}
