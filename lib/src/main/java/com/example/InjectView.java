package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lps on 2017/8/28.
 *
 * @version 1
 * @see
 * @since 2017/8/28 16:52
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface InjectView {
    int value();
}
