# Developer Notes

## Releasing a new version

To release a new version:

- Document all changes since the last release in `CHANGELOG.md`.
- Make sure you have a clean Git working tree (no pending changes).
- run `misc/releasenewversion {version} {nextVersion}` (with versions in `X.Y.Z` format, e.g. `0.10.0`). 
      `{nextVersion}` refers to the version development is targeting after this release.


