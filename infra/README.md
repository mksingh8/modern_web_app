# Infrastructure

This directory contains infrastructure-as-code and deployment configuration.

## Structure

```
infra/
├── kubernetes/   # Kubernetes manifests
├── docker/       # Dockerfiles and docker-compose files
└── terraform/    # Terraform modules for cloud resources (Azure)
```

## Prerequisites

- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Terraform](https://www.terraform.io/downloads)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)

## Deploy to Kubernetes

```bash
kubectl apply -f kubernetes/
```

## Terraform (Azure)

```bash
cd terraform/
terraform init
terraform plan
terraform apply
```
