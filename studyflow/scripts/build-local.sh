#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
if [[ -z "${JAVA_HOME:-}" ]]; then
  for candidate in "$HOME/.cache/altitude-android/jdk" "$HOME/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2" /usr/lib/jvm/java-17-openjdk; do
    if [[ -x "$candidate/bin/java" ]] && "$candidate/bin/java" -version 2>&1 | head -1 | grep -q '"17\.'; then
      export JAVA_HOME="$candidate"
      break
    fi
  done
fi
if [[ -z "${JAVA_HOME:-}" ]] || [[ ! -x "$JAVA_HOME/bin/java" ]] || ! "$JAVA_HOME/bin/java" -version 2>&1 | head -1 | grep -q '"17\.'; then
  echo "JDK 17 is required. Set JAVA_HOME to a JDK 17 installation." >&2
  exit 1
fi
tasks=("$@")
if (( ${#tasks[@]} == 0 )); then
  tasks=(testDebugUnitTest assembleDebug)
fi

if [[ "$project_dir" == *'!'* ]]; then
  # Kotlin's JAR file system interprets ! in absolute paths as a JAR separator.
  # Compile a synced copy at a safe path, then return the generated APK.
  stage_dir="${XDG_CACHE_HOME:-$HOME/.cache}/studyflow-native-build"
  mkdir -p "$stage_dir"
  rsync -a --delete \
    --exclude='.git/' --exclude='.gradle/' --exclude='.kotlin/' \
    --exclude='build/' --exclude='local.properties' \
    "$project_dir/" "$stage_dir/"
  if [[ -f "$project_dir/local.properties" ]]; then
    cp "$project_dir/local.properties" "$stage_dir/local.properties"
  fi
  (cd "$stage_dir" && ./gradlew "${tasks[@]}")
  apk="$stage_dir/app/build/outputs/apk/debug/app-debug.apk"
  if [[ -f "$apk" ]]; then
    mkdir -p "$project_dir/app/build/outputs/apk/debug"
    cp "$apk" "$project_dir/app/build/outputs/apk/debug/app-debug.apk"
    echo "APK: $project_dir/app/build/outputs/apk/debug/app-debug.apk"
  fi
else
  (cd "$project_dir" && ./gradlew "${tasks[@]}")
fi
