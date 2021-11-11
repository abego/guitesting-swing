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

import java.util.Objects;

//TODO can we share code between PropFieldNullable and PropField
class PropFieldNullableImpl<T> extends PropBase<T> implements PropFieldNullable<T> {
    private PropNullable<T> valueHolder; //TODO: do we need this anymore? Embed?

    private class SimpleField implements PropNullable<T> {
        private @Nullable T value;

        public SimpleField(T initialValue) {
            this.value = initialValue;
        }

        @Override
        public @Nullable T get() {
            return value;
        }

        @Override
        public void set(@Nullable T value) {
            if (Objects.equals(value, this.value)) {
                return;
            }
            this.value = value;
            PropFieldNullableImpl.this.postPropertyChanged();
        }

        @Override
        public void runDependingSwingCode(Runnable code) {
            PropFieldNullableImpl.this.runDependingSwingCode(code);
        }
    }

    private PropFieldNullableImpl(EventAPIForProp eventAPIForProp,
                                  @Nullable T initialValue,
                                  @Nullable Object otherSource,
                                  @Nullable String otherPropertyName) {
        super(eventAPIForProp, otherSource, otherPropertyName);
        valueHolder = new SimpleField(initialValue);
    }

    public static <T> PropFieldNullableImpl<T> newPropFieldNullable(
            EventAPIForProp eventAPIForProp,
            @Nullable T initialValue,
            @Nullable Object otherSource,
            @Nullable String otherPropertyName) {
        return new PropFieldNullableImpl<>(eventAPIForProp,initialValue, otherSource, otherPropertyName);
    }

    public static <T> PropFieldNullableImpl<T> newPropFieldNullable(
            EventAPIForProp eventAPIForProp,
            @Nullable T initialValue) {
        return new PropFieldNullableImpl<>(eventAPIForProp,initialValue, null, null);
    }

    @Override
    public @Nullable T get() {
        return valueHolder.get();
    }

    @Override
    public void set(@Nullable T value) {
        valueHolder.set(value);
    }

}
