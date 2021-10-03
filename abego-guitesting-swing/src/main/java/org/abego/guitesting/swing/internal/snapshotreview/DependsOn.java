package org.abego.guitesting.swing.internal.snapshotreview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of properties the annotated method/field depends on.
 *
 * <p>
 * <b>Annotated Methods</b><
 * <p>
 * Annotated methods must not have parameters.
 * <p>
 * When the return type of the annotated method is {@code void} it should be
 * called when any of the specified properties changed. When the return type is
 * non-{@code void} the method/function defines a "derived property". A derived
 * property may have changed when any of the properties it depends on has
 * changed.
 * <p>
 * <b>Annotated Fields</b>
 * <p>
 * Annotated fields must be final an of a type that implements
 * {@link org.abego.guitesting.swing.internal.util.Updateable}.
 * <p>
 * The field's {@code update} method must be called when any of the
 * specified properties changed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DependsOn {
    String[] value();
}
