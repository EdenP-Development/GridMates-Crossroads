# Repository setup
Current GitHub repository: https://github.com/PlayerEden/GridMates-Crossroads
- Work off development branches, use pull requests, and obtain owner approval before merging unfinished design/code, importing another project's assets, shipping APKs, or starting public release.
- In GitHub Settings, check **repository visibility**: the owner initially discussed private, but the new repository was created **public**.
- Suggested labels: `design`, `research`, `engine`, `android`, `networking`, `art`, `qa`, `blocked`.
- Local checks (requires Node 20+): `npm test`, `npm run validate`. These do not build Android.
- Future Android CI: add only after app scaffold and known reproducible Gradle baseline; don't invent green Android checks.
- Use unique app ID and native signing secrets; never commit signing keys, private APK credentials or user data.
- New documentation decisions go into `DECISION_LOG.md` with status and date.
- Reference source projects, but don't alter them without a specific separate request.
