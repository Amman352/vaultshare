#!/bin/bash
set -euo pipefail

exec > >(tee -a /var/log/vaultshare-bootstrap.log) 2>&1

curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION='v1.35.9+k3s1' sh -s - server --disable=traefik --disable=servicelb

systemctl enable k3s
systemctl is-active --quiet k3s
/usr/local/bin/k3s kubectl get nodes
