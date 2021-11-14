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

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropComputedTest {
    GT gt = GuiTesting.newGT();

    @Test
    void compute() {
        PropService propService = PropServices.getDefault();

        int[] extraInt = new int[]{2};
        Prop<Integer> propA = propService.newProp(3);
        Prop<Integer> propB = propService.newProp(4);
        PropComputed<Integer> sum = propService.newPropComputed(coll -> {
            coll.dependsOnProp(propA);
            coll.dependsOnProp(propB);
            return propA.get(coll) + propB.get(coll) + extraInt[0];
        });

        // the sum is computed correctly
        assertEquals(9, sum.get());

        // changing propA updates the sum
        propA.set(1);
        gt.assertEqualsRetrying(7, sum);

        // changing propB updates the sum
        propB.set(2);
        gt.assertEqualsRetrying(5, sum);

        // changing "extraInt" does NOT update the sum automatically.
        // We need to call compute as "extraInt" is not observed
        extraInt[0] = 3;
        sum.compute();
        gt.assertEqualsRetrying(6, sum);
    }

}