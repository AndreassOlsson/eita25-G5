#!/bin/bash
set -euo pipefail

DB_DIR="localDB"

if [ ! -d "$DB_DIR" ]; then
  echo "localDB directory not found at $DB_DIR. Nothing to purge."
  exit 0
fi

# Remove every file and folder inside localDB while keeping the root folder itself intact.
find "$DB_DIR" -mindepth 1 -exec rm -rf {} +

echo "Purged contents of $DB_DIR."
