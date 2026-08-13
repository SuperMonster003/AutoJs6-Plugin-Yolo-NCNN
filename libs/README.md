# Local protocol AAR handoff

Before `R1-BUILD`, stage exactly these AARs from one frozen AutoJs6 R0 source
revision or source snapshot:

- `common-plugin-api.aar`
- `protocol-wire-api.aar`
- `yolo-api.aar`

Record their SHA-256 values and source identity in `protocol-aars.lock.json`.
The three AARs are staged from the exact frozen AutoJs6 source commit
`7c48add4a5a77efcee7a0fa782749312d3eee5f1`. The deterministic YOLO API
`src/main` snapshot SHA-256
`4cd47305c70b5c1533fbb16efe85bd9c572e571d05b841770976ee3593dd9174`
corroborates that source identity in `protocol-aars.lock.json`. Exact AAR
lengths and SHA-256 digests are recorded there. Presence and hashing are
handoff evidence only; the sibling Gradle build remains a separate gate.
