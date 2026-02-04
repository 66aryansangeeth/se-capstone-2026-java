#!/bin/bash
# Script to detect which services have changed
# This script checks git diff to determine which services need to be rebuilt

set -e

# Get the base commit (use COMMIT_SHA if provided, otherwise use HEAD^)
BASE_COMMIT=${1:-HEAD^}
CURRENT_COMMIT=${2:-HEAD}

# Check if we're in a git reposit
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  # If not a git repo, deploy all services
  echo "auth-service,product-service,order-service,payment-service,api-gateway"
  exit 0
fi

# Get list of changed files
CHANGED_FILES=$(git diff --name-only ${BASE_COMMIT} ${CURRENT_COMMIT} 2>/dev/null || echo "")

# If no changes detected or empty, deploy all services
if [ -z "$CHANGED_FILES" ]; then
  echo "auth-service,product-service,order-service,payment-service,api-gateway"
  exit 0
fi

# Track which services need deployment
SERVICES=""

# Check each service directory
# Note: Changes to Containerfile, pom.xml, or any files in service directory trigger rebuild
if echo "$CHANGED_FILES" | grep -qE "^auth-service/|^pom.xml$|^cloudbuild.yaml$"; then
  SERVICES="${SERVICES}auth-service,"
fi

if echo "$CHANGED_FILES" | grep -qE "^product-service/|^pom.xml$|^cloudbuild.yaml$"; then
  SERVICES="${SERVICES}product-service,"
fi

if echo "$CHANGED_FILES" | grep -qE "^order-service/|^pom.xml$|^cloudbuild.yaml$"; then
  SERVICES="${SERVICES}order-service,"
fi

if echo "$CHANGED_FILES" | grep -qE "^payment-service/|^pom.xml$|^cloudbuild.yaml$"; then
  SERVICES="${SERVICES}payment-service,"
fi

if echo "$CHANGED_FILES" | grep -qE "^api-gateway/|^pom.xml$|^cloudbuild.yaml$"; then
  SERVICES="${SERVICES}api-gateway,"
fi

# Remove trailing comma and output
echo "${SERVICES%,}"
