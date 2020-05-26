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

package org.abego.guitesting.internal;

import org.abego.commons.seq.Seq;
import org.abego.guitesting.ComponentBaseSupport;
import org.eclipse.jdt.annotation.NonNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.abego.commons.seq.SeqUtil.newSeq;

public final class ComponentSupportImpl implements ComponentBaseSupport {
    private final Supplier<Seq<Window>> allWindowsSupplier;

    private ComponentSupportImpl(Supplier<Seq<Window>> allWindowsSupplier) {
        this.allWindowsSupplier = allWindowsSupplier;
    }

    static ComponentBaseSupport newComponentSupport(Supplier<Seq<Window>> allWindowsSupplier) {
        return new ComponentSupportImpl(allWindowsSupplier);
    }

    private static <T extends Component> void addComponentsWith(List<T> result, Class<T> componentClass, Component root, Predicate<T> condition) {
        if (componentClass.isInstance(root)) {
            T c = componentClass.cast(root);
            if (condition.test(c)) {
                result.add(c);
            }
        }

        if (root instanceof Container) {
            addComponentsWith(result, componentClass, ((Container) root).getComponents(), condition);
        }
    }

    private static <T extends Component> void addComponentsWith(List<T> result, Class<T> componentClass, @NonNull Component[] roots, Predicate<T> condition) {
        for (@NonNull Component c : roots) {
            addComponentsWith(result, componentClass, c, condition);
        }
    }

    private static <T extends Component> void addComponentsWith(List<T> result, Class<T> componentClass, Seq<Component> roots, Predicate<T> condition) {
        for (Component c : roots) {
            addComponentsWith(result, componentClass, c, condition);
        }
    }

    @Override
    public <T extends Component> Seq<T> allComponentsWith(Class<T> componentClass, Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (Window w : allWindowsSupplier.get()) {
            addComponentsWith(result, componentClass, w.getComponents(), condition);
        }
        return newSeq(result);
    }

    @Override
    public <T extends Component> Seq<T> allComponentsWith(Class<T> componentClass, Seq<Component> components, Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        addComponentsWith(result, componentClass, components, condition);
        return newSeq(result);
    }

}
