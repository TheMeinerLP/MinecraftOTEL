#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DESTINATION="${ROOT_DIR}/opentelemetry-javaagent.jar"

VERSION="${1:-latest}"

if [[ "${VERSION}" == "latest" ]]; then
  VERSION="$(curl -fsSL https://api.github.com/repos/open-telemetry/opentelemetry-java-instrumentation/releases/latest \
    | grep -m1 '"tag_name":' \
    | sed -E 's/.*"([^"]+)".*/\\1/')"
fi

if [[ -z "${VERSION}" ]]; then
  echo "Failed to resolve the OpenTelemetry Java Agent version." >&2
  exit 1
fi

DOWNLOAD_URL="https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${VERSION}/opentelemetry-javaagent.jar"
TMP_FILE="$(mktemp)"

echo "Downloading OpenTelemetry Java Agent ${VERSION}..."
curl -fSL "${DOWNLOAD_URL}" -o "${TMP_FILE}"
mv "${TMP_FILE}" "${DESTINATION}"
echo "Saved to ${DESTINATION}"
