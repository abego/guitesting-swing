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

package org.abego.guitesting.swing.internal.util.prop;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Function;

class PropComputedNullableImpl<T> extends PropComputedBase<T> implements PropComputedNullable<T>{
    private PropComputedNullableImpl(EventAPIForProp eventAPIForProp,
                                     Function<DependencyCollector, T> valueComputation,
                                     @Nullable Object otherSource,
                                     @Nullable String otherPropertyName) {
        super(eventAPIForProp, valueComputation, otherSource, otherPropertyName);
    }

    static <T> PropComputedNullable<T> newPropComputedNullable(
            EventAPIForProp eventAPIForProp,
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return new PropComputedNullableImpl<>(eventAPIForProp, valueComputation, otherSource, otherPropertyName);
    }

    static <T> PropComputedNullable<T> newPropComputedNullable(
            EventAPIForProp eventAPIForProp,
            Function<DependencyCollector, T> valueComputation) {
        return new PropComputedNullableImpl<>(eventAPIForProp, valueComputation, null, null);
    }

    @Override
    public @Nullable T get() {
        return _get();
    }

    @Override
    public void set(@Nullable T value) {
        _set(value);
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
