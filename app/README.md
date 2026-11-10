# VaultShare Spring Boot app — handoff to Amman

This is the Spring Boot file-sharing demo. It includes file and share APIs, a browser UI, PostgreSQL metadata, S3 storage, expiring links with download limits, audit events, and Prometheus metrics. File listing and downloads require a client public IP in the admin allowlist. The demo admin page is `/admin.html`; uploads, share creation, and deletion remain open on the public HTTP endpoint, so use disposable files only.

The Jenkins pipeline builds with Maven, runs the client access tests, builds and scans the Docker image, and deploys to K3s.

## Client command

After adding the client's **public IPv4 address** in `/admin.html`, run this single command on the client instance:

```bash
bash <(curl -fsSL http://SERVER_PUBLIC_IP:30080/cli.sh) http://SERVER_PUBLIC_IP:30080
```

The menu lists files, asks for a number, and downloads the selected file to the current directory. An unapproved IP receives a clear `No access` message. The client needs Bash, curl, and Python 3. Since the demo endpoint uses HTTP, run this command only for the class demonstration against the team-controlled server.

## Build

Run `mvn -B verify` from this directory. The `Dockerfile` builds a runtime image from the source.

## Runtime settings

Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `STORAGE_MODE=s3`, and `S3_BUCKET`. Use `?sslmode=require` in the PostgreSQL JDBC URL. The S3 client uses the instance's AWS role, not access keys in the app. S3 uploads request AWS-managed server-side encryption. Keep database passwords outside Git.

For local experimentation, `STORAGE_MODE=local` writes files under `STORAGE_PATH` (default `/tmp/vaultshare-files`). PostgreSQL is still needed for the complete app workflow.

When publishing to GitHub, upload these source files and this README. Exclude `target/`, `.env`, credentials, generated keys, and local data.
