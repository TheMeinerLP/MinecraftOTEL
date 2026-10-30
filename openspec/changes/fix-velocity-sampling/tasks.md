## 1. Test setup

- [ ] 1.1 Add JUnit, Mockito and OpenTelemetry SDK testing in test scope and enable the JUnit platform

## 2. Scheduling

- [ ] 2.1 Add a failing test that the sampling task is scheduled with the plugin instance
- [ ] 2.2 Pass the plugin instance into `VelocityTelemetryService` and use it as task owner

## 3. Current gauges

- [ ] 3.1 Add failing tests that `replaceLongGauge` drops absent series and that the default method records every entry
- [ ] 3.2 Implement `replaceLongGauge` in `TelemetryCollector` and `MeterTelemetryCollector`
- [ ] 3.3 Add failing sampler tests for removed backends, a count of 0 and a disabled count
- [ ] 3.4 Update `VelocityStandardSnapshotTelemetrySampler` and create it in `start()`

## 4. Wrap-up

- [ ] 4.1 Update `docs/admin/metrics.md`
- [ ] 4.2 Open the pull request `fix(velocity): start sampling and keep proxy gauges current`
