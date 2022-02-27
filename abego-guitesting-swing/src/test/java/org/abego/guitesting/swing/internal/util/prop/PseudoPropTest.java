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

import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class PseudoPropTest {
    GT gt = GuiTesting.newGT();

    @Test
    void smoketest() {
        // set a shorter period than the default, to run the test faster
        PropService propService = PropServices.newPropService();
        propService.setPseudoPropRecheckPeriod(Duration.ofMillis(1L));

        // Our test is calculating the sum of a and b using a PseudeProp sum
        int[] a = new int[]{2};
        int[] b = new int[]{3};

        PropComputed<Integer> sum =
                propService.newPseudoProp(() -> a[0] + b[0]);

        // We also log how often the sum changed.
        int[] sumChangeCount = new int[]{0};
        EventServices.getDefault().addObserver(PropertyChanged.class, sum,
                e -> sumChangeCount[0] = sumChangeCount[0] + 1);

        // Check the initial value
        gt.assertEqualsRetrying(0, () -> sumChangeCount[0]);
        gt.assertEqualsRetrying(5, sum::get);

        // Change an input value.
        //
        // The PseudoProp sum should recognize the change and calculate the
        // new sum even though a and b are not "directly" observed (e.g. via
        // Prop objects).

        a[0] = 4;
        gt.assertEqualsRetrying(1, () -> sumChangeCount[0]);
        gt.assertEqualsRetrying(7, sum::get);

        // Another input value change.
        //
        // This time the asserts are in a differnt order, to make sure the
        // new value is provided even without explicitly waiting for the
        // "PropertyChanged" event.
        b[0] = 5;
        gt.assertEqualsRetrying(9, sum::get);
        gt.assertEqualsRetrying(2, () -> sumChangeCount[0]);
    }
}