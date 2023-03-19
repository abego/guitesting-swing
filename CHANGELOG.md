# CHANGELOG

## 0.15.0

### New

- "Copy Description to Clipboard" feature
- add "close()" to Widget API/"Widget related" category
- add GT.type(KeyStroke)/typeKey(String)
- add GuiTesting.startSnapshotReview
- add get...Prop methods
- add pre-build SnapshotReviewApp.jar to "tools" directory
- move binding code to new Bindings API
- add ImageDifferenceIgnored[Border|Corner]Size
- add resetMouse and resetScreenCaptureSupport

### Changes (Possibly incompatible)

- "inline" PropFactory in PropService
- add 'visible' to BoxStyle
- add VStackWidget (and use it)
- avoid using Var/Nullable Udo Borkowski 
- change parameter order for bindSwingCode
- hide eventAPIForProp
- in Prop use "isReadOnly" instead of "isEditable"
- make bindSwingCode support multiple props
- moved runDependingSwingCode to Bindings
- new method getSnapshotNameDefault() replaces SNAPSHOT_NAME_DEFAULT
- remove PropField... and PropComputed... from API
- rename some factory methods to newComputedProp...
- replace "bind" with "bind[...]To" methods 
- replace AssertRetryingSupport by AssertRetryingService 
- replace PollingSupport by PollingService
- replace TimeoutSupport by TimeoutService 
- runDependingSwingCode -> bindSwingCode 
- use BoxStyle instead of AWT Border in client code 
- use testResourcesDirectory, not ...Path

### Bug Fixes

- GTHeadlessImpl must not call code requiring "head" (mouse, display)
- JCheckBoxBindable selected state not initialized with Prop value
- PropComputedNullable not updated automatically
- PseudoProp triggers no events until first call to `get`
- bindSwingCode observer is not removed
- disposeAllWindows must run in EDT
- endless recursion when using BorderedPanel#east
- make sure to run JFrame.show in the EDT
- tooltips empty for VList's "next/previous" buttons

### Improvements

- BoxStylingSwing sets JComponent#opaque attribute 
- SnapshotReview: various improvements
- add GT.makeScreenshotMatchingTolerant() and use it
- add bindSwingCode(...,Consumer) to Bindings
- add bindSwingCodeTo[Nullable](Consumer, Supplier)
- add factory PropServices.newPropService()
- add getEventService() to PropService API
- basic "multi-variant" snapshot support
- better grouping of issues in SnapshotReviewWidget 
- don't shrink images below a minimum size 
- emit "selected" PropertyChanged event for JCheckBoxBindable
- emit "shrinkToFit" PropertyChanged event for SnapshotReviewWidget
- impleement bindSwingCode; add Bindings.isUpdating()
- improved waitUntilAllMenuRelatedScreenshotsMatchSnapshot
- improved waitUntilAllMenuRelatedScreenshotsMatchSnapshot
- in issue list, display test name first
- move code from SnapshotReviewApp to ScreenCaptureSupport
- nicer styling of the titlebar
- release modifier keys (e.g "shift") before running GUI tests
- remove dependency to GT from SnapshotReview(Impl)
- resetMouse when createing a GTImpl
- showIssues support optional initializer code
- some more distance between Copy-button and issue description
- support PseudoProp
- support binding a Consumer to a Prop
- support otherSource/PropertyName in Prop/Binding
- use BoxStyle to style snapshotIssuesVList borders

## 0.14.0

### Improvements

- Improved screen capture HTML report:
  - platform-specific command to overwrite/create expected images ( Windows: "copy", Mac:"cp")
  - button to copy the command to overwrite/create expected images into the clipboard.
- use gt.captureScreen(null) to capture the full screen

### Bug Fixes

- (Timeout)Exception when snapshotting a menu that has no menu items
- ScreenCaptureSupport.writeImage fails when directory does not exist

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
