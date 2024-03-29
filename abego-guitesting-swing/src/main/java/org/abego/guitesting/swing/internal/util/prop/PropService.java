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

import org.abego.event.EventService;
import org.eclipse.jdt.annotation.Nullable;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PropService {
    String VALUE_PROPERTY_NAME = "value";
    Duration PSEUDO_PROP_RECHECK_PERIOD_DEFAULT = Duration.ofMillis(50);

    //region Prop/PropNullable
    <T> Prop<T> newProp(T value);

    <T> Prop<T> newProp(
            T value, Object otherSource, String otherPropertyName);

    <T> PropNullable<T> newPropNullable();

    <T> PropNullable<T> newPropNullable(T value);

    <T> PropNullable<T> newPropNullable(
            @Nullable T value, Object otherSource, String otherPropertyName);
    //endregion
    //region PropComputed/PropComputedNullable
    <T> PropComputed<T> newPropComputed(Function<DependencyCollector, T> valueComputation);

    <T> PropComputed<T> newPropComputed(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName);

    <T> PropComputedNullable<T> newPropComputedNullable(Function<DependencyCollector, T> valueComputation);

    <T> PropComputedNullable<T> newPropComputedNullable(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName);
    //endregion
    //region PseudoProp
    <T> PropComputed<T> newPseudoProp(Supplier<T> valueComputation);

    <T> PropComputedNullable<T> newPseudoPropNullable(Supplier<T> valueComputation);

    Duration getPseudoPropRecheckPeriod();

    void setPseudoPropRecheckPeriod(Duration period);
    //endregion
    //region Bindings
    Bindings newBindings();
    //endregion
    //region EventService
    EventService getEventService();
    //endregion
    //region Closing
    void close();
    //endregion
}
