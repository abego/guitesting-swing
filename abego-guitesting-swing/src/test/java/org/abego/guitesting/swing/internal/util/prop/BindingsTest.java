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

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BindingsTest {

    @Test
    void bind() {
        PropService propService = PropServices.getDefault();

        Prop<Integer> propA = propService.newProp(3);
        assertEquals(3, propA.get());

        Prop<Integer> propB = propService.newProp(4);
        assertEquals(4, propB.get());

        Integer[] consumerOutput= new Integer[1];
        Consumer<Integer> consumer = i->{ consumerOutput[0] = i;};

        Bindings b = propService.newBindings();
        b.bind(propA, propB);
        b.bind(propA, consumer);

        assertEquals(3, propB.get());
        assertEquals(3, consumerOutput[0]);

        propA.set(7);

        assertEquals(7, propA.get());
        assertEquals(7, consumerOutput[0]);

        propB.set(8);

        assertEquals(8, propA.get());
        assertEquals(8, consumerOutput[0]);
    }

}