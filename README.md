# VaultShare

VaultShare is a file-sharing demo across three AWS accounts. Users can upload, list, download, delete, and share files using links with expiry and download limits. The demo app has no login. Use only disposable files while its public HTTP endpoint is open.

## Five modules and individual ownership

| Module | Owner | Demonstration |
| --- | --- | --- |
| 1. File API and storage | Amman | Spring Boot API, PostgreSQL metadata, S3 file storage |
| 2. Basic application security | Amman | Expiring links, S3 server-side encryption, scoped IAM and network rules |
| 3. AWS infrastructure and deployment | Amman and Tanay | Amman: Organization, shared VPC, RDS and S3. Tanay: EC2, Terraform, K3s and app deployment |
| 4. CI/CD | Tanay | Maven build, Docker image, Jenkins publish and K3s deploy stages |
| 5. Monitoring and scaling | Dhruv and Tanay | Dhruv: Prometheus scrape, Grafana dashboard and alert. Tanay: Kubernetes HPA |

Each member should present only work they actually implemented and can explain.

## Current AWS layout

* **Amman's account** owns the Organization, shared VPC, PostgreSQL RDS database, and S3 bucket.
* **Tanay's account** runs the app on EC2 in Amman's shared VPC. K3s runs the app with an HPA; Jenkins builds, publishes, and deploys Docker images.
* **Dhruv's account** will run Prometheus and Grafana on EC2 in his own VPC. Prometheus scrapes Tanay's public demo endpoint. Use `monitoring/compose.yaml` and `monitoring/prometheus.yml`.

**Review limitation:** Dhruv's separate VPC does not satisfy the original requirement for all three EC2 instances to be in one VPC. If that requirement is enforced, Dhruv must join Amman's Organization and launch in an Amman-shared subnet. An RDS database does not count as Amman's EC2 instance; confirm he has a separate EC2 instance if the review requires one per member.

## Deploy and verify

The app source is in `app/`, the Jenkins pipeline is `Jenkinsfile`, and Tanay's live infrastructure is in `infra/tanay/`. `docs/tanay-live-status.md` records verified AWS state. The root `infra/` directory is an older single-account prototype and must not be applied to the current setup.

For the monitoring demo, Dhruv starts the two services from `monitoring/` with `docker compose up -d`, connects Grafana to Prometheus at `http://prometheus:9090`, and creates a Grafana-managed alert on the `up{job="vaultshare"}` metric. Grafana and Prometheus ports bind to localhost on Dhruv's EC2 and can be viewed through AWS Systems Manager port forwarding.

Use the app's public endpoint only for the short demo. After the review, close Tanay's public TCP 30080 rule and remove demo resources. Docker, Kubernetes, Terraform, Maven, and Jenkins are in the actual workflow. Ansible, GitHub Actions, Alertmanager, and CloudWatch alarms are not part of the simplified implementation.
