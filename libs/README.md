# Local protocol AAR handoff

Before `R1-BUILD`, stage exactly these AARs from one frozen AutoJs6 R0 source
revision or source snapshot:

- `common-plugin-api.aar`
- `protocol-wire-api.aar`
- `yolo-api.aar`

Record their SHA-256 values and source identity in `protocol-aars.lock.json`.
The three AARs are staged from AutoJs6 base commit
`3c170896d8e4b528f1aa85953a4698d1742c8344`. The YOLO API additionally includes
the SDK-safe open-session error codec identified by the deterministic
`src/main` snapshot hash in `protocol-aars.lock.json`. Exact AAR lengths and
SHA-256 digests are recorded there. Presence and hashing are handoff evidence
only; the sibling Gradle build remains a separate gate.
