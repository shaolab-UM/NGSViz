#!/usr/bin/env bash

set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readme="${repo_root}/README.md"

echo "Referenced environment files:"
rg -n 'requirement\.yml|environment\.yml' "${readme}" || true
echo
echo "JAR spellings used in README:"
rg -o '([Nn][Gg][Ss][Vv]i[rz][-_][0-9.]+\.jar)' "${readme}" | sort -u
echo
echo "Repository artifacts:"
for artifact in environment.yml requirement.yml NGSViz-1.2.jar; do
  if [[ -e "${repo_root}/${artifact}" ]]; then
    echo "present  ${artifact}"
  else
    echo "missing  ${artifact}"
  fi
done
echo
echo "Documented and implemented batch-size defaults:"
rg -n '\-BS.*2000' "${readme}" || true
rg -n 'BATCH_SIZE = ' "${repo_root}/src/main/java/com/NGSViz/configSet/InputParameterAttributes.java"
rg -n 'dashBS.*value = ' "${repo_root}/lib/shiny/ui_builder.R"
echo
echo "Tracked configuration paths:"
rg -n '"(tool_path|db_path|tool_name|java_path)"' "${repo_root}/NGSViz_setting.json"
