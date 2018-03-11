package com.rainmaker.workchat

/**
 * Created by dmitry on 3/10/18.
 *
 */

infix fun Byte.shr(that: Byte): Int = this.toInt().shr(that.toInt())

infix fun Byte.and(that: Byte): Int = this.toInt().and(that.toInt())
