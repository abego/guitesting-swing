# Developer Notes

## Releasing a new version

To release a new version:

- Document all changes since the last release in `CHANGELOG.md`.
- Make sure you have a clean Git working tree (without pending changes,) and you are on the `master` branch.
- Run `misc/releasenewversion {version} {nextVersion}`, with versions in `X.Y.Z` format, e.g. `0.10.0`.
  _(`{nextVersion}` refers to the version development is targeting after this release.)_
- Perform the remaining manual steps, as printed at the end of the automatic release process.


