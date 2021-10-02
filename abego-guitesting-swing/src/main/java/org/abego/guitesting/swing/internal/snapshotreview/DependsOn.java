package org.abego.guitesting.swing.internal.snapshotreview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of properties the annotated method depends on.
 *
 * <p>When the return type of the annotated method is {@code void} it should be
 * called when one of the specified properties changed. When the return type is
 * non-{@code void} the method/function defines a "derived property". A derived
 * property may have changed when one of the properties it depends on has
 * changed.
 * <p>The annotated methods must not have parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DependsOn {
    String[] value();
}
