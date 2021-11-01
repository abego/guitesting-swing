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

import static org.abego.guitesting.swing.internal.util.prop.PropField.newPropField;

class PropsDefault implements Props {
    private final EventsForProp eventsForProp;

    private PropsDefault(EventsForProp eventsForProp) {this.eventsForProp = eventsForProp;}

    public static Props newProps(EventsForProp eventsForProp) {
        return new PropsDefault(eventsForProp);
    }

    @Override
    public void close() {
        eventsForProp.close();
    }

    @Override
    public <T> Prop<T> newProp(T value) {
        return newPropField(eventsForProp, value, null, null);
    }

    @Override
    public <T> Prop<T> newProp(
            T value, Object otherSource, String otherPropertyName) {
        return newPropField(eventsForProp, value, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropNullable<T> newPropNullable() {
        return PropFieldNullable.newPropFieldNullable(eventsForProp, null);
    }

    @Override
    public <T> PropNullable<T> newPropNullable(T value) {
        return PropFieldNullable.newPropFieldNullable(eventsForProp, value);
    }

    @Override
    public <T> PropNullable<T> newPropNullable(
            @Nullable T value, Object otherSource, String otherPropertyName) {
        return PropFieldNullable.newPropFieldNullable(eventsForProp, value, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropComputed<T> newPropComputed(Function<DependencyCollector, T> valueComputation) {
        return PropComputedImpl.newPropComputed(eventsForProp, valueComputation);
    }

    @Override
    public <T> PropComputed<T> newPropComputed(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedImpl.newPropComputed(eventsForProp, valueComputation, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropComputedNullable<T> newPropComputedNullable(Function<DependencyCollector, T> valueComputation) {
        return PropComputedNullableImpl.newPropComputedNullable(eventsForProp, valueComputation);
    }

    @Override
    public <T> PropComputedNullable<T> newPropComputedNullable(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedNullableImpl.newPropComputedNullable(eventsForProp, valueComputation, otherSource, otherPropertyName);
    }
}
