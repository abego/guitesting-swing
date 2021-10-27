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

package org.abego.guitesting.swing.internal.util;

import org.abego.commons.var.Var;
import org.abego.event.EventServices;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link Var} emitting {@link org.abego.event.PropertyChanged} events
 * when its value changed (via {@link org.abego.event.EventServices} default).
 */
public class Prop<T> implements Var<T> {
    private @Nullable T value;

    private Prop() {
    }

    private Prop(@NonNull T value) {
        this.value = value;
    }

    public static <T> Prop<T> newProp() {
        return new Prop<T>();
    }

    public static <T> Prop<T> newProp(@NonNull T value) {
        return new Prop<T>(value);
    }

    @Override
    public @NonNull T get() {
        @Nullable T v = value;
        if (v == null) {
            throw new IllegalStateException("Var has no value"); //NON-NLS
        }
        return v;
    }

    @Override
    public void set(@NonNull T value) {
        if (!value.equals(this.value)) {
            this.value = value;
            EventServices.getDefault().postPropertyChanged(this, "value"); //NON-NLS
        }
    }

    @Override
    public boolean hasValue() {
        return value != null;
    }
}
