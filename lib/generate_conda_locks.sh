#!/usr/bin/env bash
set -euo pipefail

# Generate native Conda explicit lock files for every supported platform.
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "${script_dir}/.." && pwd)"
lock_command="${1:-conda-lock}"

mkdir -p "${project_dir}/conda-lock"
"${lock_command}" lock \
  --file "${project_dir}/environment.yml" \
  --kind explicit \
  --no-mamba \
  --platform "linux-64" \
  --platform "osx-64" \
  --platform "osx-arm64" \
  --filename-template "${project_dir}/conda-lock/{platform}.lock"
