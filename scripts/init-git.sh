#!/usr/bin/env bash
# Initialize the Nestmate git repository and make the first commit.
# Run ONCE from the project root:
#     bash scripts/init-git.sh
#
# (Claude's sandbox can't reach this secondary drive, so git is run here on your machine.)

set -euo pipefail
cd "$(dirname "$0")/.."

if [ -d .git ]; then
  echo "==> git repo already exists — skipping 'git init'."
else
  echo "==> Initializing git repository..."
  git init
  git branch -M main
fi

echo "==> Staging files (respecting .gitignore)..."
git add .

echo "==> Creating initial commit..."
git commit -m "chore: initial Android project + Nestmate docs scaffolding"

echo ""
echo "==> Done. Status:"
git status
echo ""
echo "Next (optional) — push to a remote:"
echo "    git remote add origin <your-repo-url>"
echo "    git push -u origin main"
