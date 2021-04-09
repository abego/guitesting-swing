# CHANGELOG

## 0.13.0

### New

- force early timeout of "wait..." methods by interrupting their Thread.

## 0.12.0

### New

- add waitForComponent(Class<T>)

### Improvements

- ScreenCaptureSupport:
  - improved automatic snapshot naming
  - "absolute" snapshot name support
  - better support for menu screenshots

## 0.11.0

### New

- add ScreenCaptureSupport
- add assertSuccessRetrying

### Improvements

- more robust tests
- improved code coverage
- Fix typos
- Documentation

## 0.10.1

### Documentation

- update release process documentation

## 0.10.0

### Changes

- GuiTesting -> GT, GuiTestingFactory -> GuiTesting
- clickAtStartOf -> clickAtStartOfSubstring

### New

- GuiTestingException

- add GuiTesting#debug
- add MouseSupport#clickCharacterAtIndex
- add TimeoutSupport#setTimeoutMillis(long)

### Improvements

- Fix typos
- Documentation

### Bug Fixes

- Bug: NPE in toDebugString(JTree,...) when tree is not a JViewport
- Bug: Exception in JTextComponentTestUtil#getTextAndHighlights 

## 0.9.5

### New

- Support Sonatype Nexus server

### Improvements

- Fix typos

### Bug Fixes

- Fix JavaDoc issues


## 0.9.4

- Initial Release
