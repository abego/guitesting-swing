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

import org.abego.commons.var.Var;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Function;

import static org.abego.guitesting.swing.internal.util.prop.PropField.newPropField;

//TODO: review JavaDoc

/**
 * A {@link Var} emitting {@link org.abego.event.PropertyChanged} events
 * when its value changed (via {@link org.abego.event.EventServices} default).
 * <p>
 * The source of the PropertyChanged event will be the Prop object
 * and the property name "value". In addition a second PropertyChanged event
 * may be generated with another source object and property name. The
 * "other source" typically is the object containing the Prop object and the
 * property name the name of the Prop within that container.
 */
//TODO: check if we can reuse some code of the different "Prop..." classes
class PropServiceDefault implements PropService {
    private static PropService DEFAULT_INSTANCE = newPropService(EventServices.getDefault());
    private final EventService eventService;

    private PropServiceDefault(EventService eventService) {this.eventService = eventService;}

    static PropService getDefault() {
        return DEFAULT_INSTANCE;
    }

    public static PropService newPropService(EventService eventService) {
        return new PropServiceDefault(eventService);
    }

    public static PropService newPropService() {
        return newPropService(EventServices.getDefault());
    }

    @Override
    public  <T> Prop<T> newProp(T value) {
        return newPropField(eventService, value, null, null);
    }

    @Override
    public  <T> Prop<T> newProp(
            T value, Object otherSource, String otherPropertyName) {
        return newPropField(eventService, value, otherSource, otherPropertyName);
    }

    @Override
    public  <T> PropNullable<T> newPropNullable() {
        return PropFieldNullable.newPropFieldNullable(eventService, null);
    }

    @Override
    public  <T> PropNullable<T> newPropNullable(T value) {
        return PropFieldNullable.newPropFieldNullable(eventService, value);
    }

    @Override
    public  <T> PropNullable<T> newPropNullable(
            @Nullable T value, Object otherSource, String otherPropertyName) {
        return PropFieldNullable.newPropFieldNullable(eventService, value, otherSource, otherPropertyName);
    }

    @Override
    public  <T> PropComputed<T> newPropComputed(Function<DependencyCollector, T> valueComputation) {
        return PropComputedImpl.newPropComputed(eventService, valueComputation);
    }

    @Override
    public  <T> PropComputed<T> newPropComputed(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedImpl.newPropComputed(eventService, valueComputation, otherSource, otherPropertyName);
    }

    @Override
    public  <T> PropComputedNullable<T> newPropComputedNullable(Function<DependencyCollector, T> valueComputation) {
        return PropComputedImpl.newPropComputed(eventService, valueComputation);
    }

    @Override
    public  <T> PropComputedNullable<T> newPropComputedNullable(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedImpl.newPropComputed(eventService, valueComputation, otherSource, otherPropertyName);
    }

}
