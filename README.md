# abego GuiTesting Swing
 
__Easily write GUI Tests in Java (for Swing)__

## Overview

Use the abego GuiTesting Swing library to quickly write GUI tests for Swing 
applications in the same way you write "headless" JUnit tests.

## The GT Interface
 
GUITesting Swing provides most of its features through the GT interface.
The interface covers many areas  related to GUI testing, 
like dealing with windows or components, using input devices like 
keyboard or mouse, checking test results with GUI specific 
"assert..." methods and many more.

A typical code snippet using GT may look like this:

```java
import static org.abego.guitesting.swing.GuiTesting.newGT;

...

// A GT instance is the main thing we need when testing GUI code.
GT gt = newGT();

// run some application code that opens a window
openSampleWindow();

// In that window we are interested in a JTextField named "input"
JTextField input = gt.waitForComponentNamed(JTextField.class, "input");

// we move the focus to that input field and type "Your name" ", please!"
gt.setFocusOwner(input);
gt.type("Your name");
gt.type(", please!");

// Verify if the text field really contains the expected text.
gt.assertEqualsRetrying("Your name, please!", input::getText);

// When we are done with our tests we can ask GT to cleanup
// (This will dispose open windows etc.)
gt.cleanup();
```
   
## Unit Testing

When writing JUnit tests you may want to subclass from `GuiTestBase`.

## Sample Code

For samples how to use the GUITesting Swing library have a look 
at the test code of this project, in the `src/test/java` folder.

## Installation

__Maven:__

To use abego GUITesting Swing in a project built with Maven, 
add the following to the <dependencies> element in your pom.xml file.

```
<dependency>
    <groupId>org.abego.guitesting</groupId>
    <artifactId>abego-guitesting-swing</artifactId>
    <version>0.10.1</version>
</dependency>
```


__Download:__ https://github.com/abego/guitesting-swing/releases/tag/v0.10.1

## Development

You may check out the source code from the 
[GitHub repository](https://github.com/abego/guitesting-swing).

## Known Issues

- Does not yet work well on Travis CI etc. (Help greatly appreciated!)

## Links

- Sources: https://github.com/abego/guitesting-swing
- Twitter: @abego (e.g. for announcements of new releases)

## License

abego GuiTesting is available under a business friendly [MIT license](https://www.abego-software.de/legal/mit-license.html).
