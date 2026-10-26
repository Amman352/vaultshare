# VaultShare

VaultShare is a three-instance AWS demonstration of secure file sharing. The final design has one shared VPC and one EC2 instance owned by each of three AWS accounts. The instances are tagged **Tanay**, **Amman**, and **Dhruv**. Amman owns the AWS Organization's consolidated bill.

## Five modules and individual ownership

| Module | Owner | Demonstration |
| --- | --- | --- |
| 1. File API and storage | Amman | Upload, list, download, delete, S3 persistence |
| 2. Basic application security | Amman | Private network access, restricted S3 permissions, server-side encryption, expiring links |
| 3. VPC and deployment | Amman and Tanay | Amman: AWS Organization, shared VPC and subnets. Tanay: his app instance, Kubernetes (K3s), IAM and Docker deployment |
| 4. CI/CD | Tanay | Maven checks, Docker build, Jenkins pipeline |
| 5. Monitoring and scaling | Dhruv | Prometheus, Grafana, Alertmanager, CloudWatch alarms, Kubernetes app pod scaling |

Each owner should present only work they actually implemented and can explain. Shared integration is a team demonstration.

## Architecture

**Deployment decision:** Amman will own the AWS Organization, shared VPC, and combined bill. Tanay and Dhruv join as participant accounts. See `docs/multi-account-setup.md` before creating resources. The existing `infra/` Terraform is a **single-account prototype** and must not be applied for this final design.

* **Amman account:** AWS Organization, shared VPC and subnets, plus PostgreSQL instance reachable only from the application.
* **Tanay account:** app instance in Amman's shared subnet, running a single-node K3s Kubernetes cluster. Kubernetes HPA can scale app pods from one to two on that instance.
* **Dhruv account:** monitoring instance in Amman's shared subnet, running Prometheus, Grafana, and Alertmanager.
* S3 stores files with AWS-managed server-side encryption. The app uses an IAM instance profile, never static AWS keys. The app has no login, so access to its API must be restricted to the demo team. CloudWatch alarms and Kubernetes pod scaling support the review.

## Start locally

Install Docker and run `docker compose up --build` from this directory. Copy `.env.example` to `.env` and set strong, unique values first. The local profile stores files on disk for demonstration; the AWS profile uses S3. Open `http://localhost:8080` for the app and `http://localhost:3000` for Grafana.

## Deploy to AWS

For the agreed three-account design, follow `docs/multi-account-setup.md` and `docs/kubernetes-deployment.md`. Tanay's verified live state is in `docs/tanay-live-status.md`, with Jenkins access steps in `docs/tanay-jenkins-handoff.md`. The `infra/` Terraform and `docs/aws-deployment.md` describe an older **single-account prototype** and must not be applied to the final review setup. Amman builds and shares the network foundation; Tanay and Dhruv then launch their own instances in its shared subnets.

## Review evidence and limits

Capture the VPC resource map, three EC2 instance IDs with **three different owner account IDs**, IAM/security groups, successful app workflow, Jenkins run, Grafana dashboard, an Alertmanager notification, a CloudWatch alarm, and Kubernetes HPA scaling from one app pod to two. HPA scales pods on Tanay's existing instance. Keep the no-login app reachable only by the demo team.

AWS resources have costs beyond EC2, including public IPv4 addresses, storage, traffic, and alarm usage. Set an AWS budget and remove the stack when the demonstration ends. Tanay's deployed infrastructure is managed by `infra/tanay/`; the older `infra/` prototype must not be applied. Docker, Kubernetes, Terraform, Maven, and Jenkins are part of the intended DevOps workflow. Ansible and GitHub Actions are optional.
