## 1. Connection metrics

- [ ] 1.1 Add failing listener tests for logins, disconnects, backend connects, kicks, pings and sessions
- [ ] 1.2 Implement the listener, register it on start and unregister it on stop

## 2. Sampled metrics

- [ ] 2.1 Add failing sampler tests for client versions and latency
- [ ] 2.2 Extend snapshot, snapshot sampler and standard sampler

## 3. Backend health

- [ ] 3.1 Add failing probe tests for reachable, unreachable and removed backends
- [ ] 3.2 Implement the probe and its scheduled task

## 4. Wrap-up

- [ ] 4.1 Add the settings to `velocity.properties` and `docs/admin/configuration.md`
- [ ] 4.2 Add the metrics to `docs/admin/metrics.md`
- [ ] 4.3 Add `dashboards/grafana/proxy.json`
- [ ] 4.4 Open the pull request `feat(velocity): add network, session and backend metrics`
