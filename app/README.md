# VaultShare Spring Boot app — handoff to Amman

This is the simplified source scaffold for Amman's file-sharing module. It includes file and share APIs, a browser UI, PostgreSQL metadata, S3 storage, expiring links with download limits, audit events, and Prometheus metrics. The app has no login. Anyone who can reach its API can list, download, share, and delete files, so keep the demo endpoint restricted to the team.

The source compiled and packaged successfully with Java 21 and Maven 3.9.11 on 26 October 2026. There are **no automated application tests yet**, and the full PostgreSQL/S3 workflow has not been tested. Review and finish the code before treating it as Amman's final implementation.

## Build

Run `mvn -B verify` from this directory. The `Dockerfile` builds a runtime image from the source.

## Runtime settings

Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `STORAGE_MODE=s3`, and `S3_BUCKET`. Use `?sslmode=require` in the PostgreSQL JDBC URL. The S3 client uses the instance's AWS role, not access keys in the app. S3 uploads request AWS-managed server-side encryption. Keep database passwords outside Git.

For local experimentation, `STORAGE_MODE=local` writes files under `STORAGE_PATH` (default `/tmp/vaultshare-files`). PostgreSQL is still needed for the complete app workflow.

When publishing to GitHub, upload these source files and this README. Exclude `target/`, `.env`, credentials, generated keys, and local data.
