# jishe

## Runtime secrets

Credentials are read from environment variables. Never put real values in
tracked source files or commit a local `.env` file.

Copy `.env.example` only as a checklist, then configure the variables in your
IDE, process manager, CI/CD secret store, or deployment platform. Required
secrets are `DB_PASSWORD`, `JWT_SECRET`, `ALIYUN_API_KEY`,
`ALIYUN_OSS_ACCESS_KEY_ID`, and `ALIYUN_OSS_ACCESS_KEY_SECRET`.

After a credential exposure, revoke the old credential before issuing and
deploying its replacement. Removing it from Git does not make it safe again.
