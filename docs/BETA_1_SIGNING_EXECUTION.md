# Private Beta 1 signing execution checklist

Status: registration PR #122 is draft/unmerged. Release workflow signing job now references environment crossroads-beta-signing. No environment settings have been configured or verified through this connector. Merely naming an environment does not protect it; GitHub can create an unprotected environment automatically. DO NOT dispatch before actual protection and credential scope are verified.

## Before execution
- Verify current exact release head, its complete diff and successful rehearsal/QA checks. Previous verified head d96ccbd6707c11f55f102615d6ff3da9d444301d: rehearsal #6 run 46123639606 and Android #1100 run 46123639567 succeeded.
- Verify owner has independent recoverable encrypted private keystore/password backups. Do not send secrets in chat, commit them or regenerate the signer.
- In GitHub repository Settings > Environments, configure crossroads-beta-signing, restrict deployment to release/beta-1-candidate, configure required reviewers where supported and check actual availability. Review organization/repository secret access; existing organization-level secrets are NOT protected simply by adding environment to job. Prefer environment-scoped credentials and remove broader access only after verifying safe migration and recovery, with owner authorization. Review workflow and action trust; restrict branch writers. Do not assume plan supports all controls.
- Review and approve only registration PR #122 (one fail-closed workflow file; no game runtime). Merge only when authorized. Main should not sign.
- Confirm latest release SHA and approved scope. On Actions select Crossroads private Beta 1 candidate, Run workflow, branch release/beta-1-candidate, reviewed_source exact SHA. Approve protected environment if required. Connector currently lacks dispatch or settings-management actions.
- Confirm jobs passed, expected public cert e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914, application ID com.playereden.gridmates.crossroads, version greater than historical 2002, APK SHA256 and receipt, offline permissions. Preserve artifact privately for owner testing.
- No public upload until explicit physical acceptance and publication approval.

If required environment controls are unavailable, stop and review an alternate trusted owner-controlled signing path. Do not add secret-bearing push or PR trigger, merge game into main, or disable verification to work around dispatch.
