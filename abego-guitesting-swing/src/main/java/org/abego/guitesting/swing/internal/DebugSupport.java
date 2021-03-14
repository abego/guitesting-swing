/*
 * MIT License
 *
 * Copyright (c) 2019 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.guitesting.swing.internal;

import org.abego.commons.lang.StringUtil;
import org.abego.commons.seq.Seq;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.abego.commons.lang.StringUtil.limitString;
import static org.abego.commons.lang.StringUtil.quoted;

class DebugSupport {

    static void dumpAllComponents(Supplier<Seq<Window>> allWindowsSupplier, PrintStream out) {
        visitAll(allWindowsSupplier.get(), new DumpingVisitor(out));
    }

    private static void visitAll(Iterable<Window> windows, DumpingVisitor v) {
        InnerVisitor innerVisitor = new InnerVisitor(v);
        for (Window w : windows) {
            innerVisitor.visitingWindow(w);
            visit(innerVisitor, w.getComponents());
            innerVisitor.visitedWindow(w);
        }
    }


    @Nullable
    private static String guessTitleOrNull(Object object) {
        @Nullable String result = callStringGetter(object, "getTitle");
        if (result == null)
            result = callStringGetter(object, "getLabel");
        if (result == null)
            result = callStringGetter(object, "getText");
        return result != null ? limitString(result, 40) : null;
    }

    @Nullable
    private static String callStringGetter(Object object, String methodName) {
        try {
            Method m = object.getClass().getMethod(methodName);
            return m.invoke(object).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static void visit(InnerVisitor innerVisitor, Component root) {

        if (root instanceof Container) {
            innerVisitor.visitingContainer((Container) root);
            visit(innerVisitor, ((Container) root).getComponents());
            innerVisitor.visitedContainer((Container) root);
        } else {
            innerVisitor.visit(root);
        }
    }

    private static void visit(InnerVisitor innerVisitor, @NonNull Component[] components) {
        for (Component c : components) {
            visit(innerVisitor, c);
        }
    }

    public interface WindowAndContentVisitor {
        default void willVisitWindow(Window window, int level) {
        }

        default void didVisitWindow(Window window, int level) {
        }

        default void didVisitComponent(Component component, int level) {
        }

        default void willVisitContainer(Container container, int level) {
        }

        default void didVisitContainer(Container container, int level) {
        }
    }

    private static class DumpingVisitor implements WindowAndContentVisitor {
        private final PrintStream out;

        DumpingVisitor(PrintStream out) {
            this.out = out;
        }

        private void println(Class<?> type, @Nullable String nameOrNull, @Nullable String titleOrNull, Rectangle bounds, int level) {
            //noinspection HardCodedStringLiteral
            out.printf("%s%s (%s)\t%s\t%s\t@(%d,%d) %dx%d%n",
                    getIndentString(level),
                    type.getSimpleName(),
                    type.getPackage().getName(),
                    quoted(nameOrNull),
                    quoted(titleOrNull),
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height);
        }

        private String getIndentString(int indentLevel) {
            return StringUtil.repeat("    ", indentLevel);
        }

        public void willVisitWindow(Window window, int level) {
            println(window.getClass(), window.getName(), guessTitleOrNull(window), window.getBounds(), level);
        }

        public void didVisitComponent(Component component, int level) {

            println(component.getClass(), component.getName(), guessTitleOrNull(component), component.getBounds(), level);
        }
    }

    private static class InnerVisitor {
        private final WindowAndContentVisitor v;
        private int indentLevel = 0;

        private InnerVisitor(WindowAndContentVisitor v) {
            this.v = v;
        }

        private void visitingWindow(Window window) {
            v.willVisitWindow(window, indentLevel);
            indentLevel++;
        }


        private void visitedWindow(Window window) {
            indentLevel--;
            v.didVisitWindow(window, indentLevel);
        }


        private void visitingContainer(Container container) {
            v.willVisitContainer(container, indentLevel);
            visit(container);
            indentLevel++;
        }

        private void visitedContainer(Container container) {
            indentLevel--;

            v.didVisitContainer(container, indentLevel);
        }

        private void visit(Component component) {
            v.didVisitComponent(component, indentLevel);
        }
    }

}



