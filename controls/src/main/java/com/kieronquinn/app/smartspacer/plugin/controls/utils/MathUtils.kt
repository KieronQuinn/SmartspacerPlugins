package com.kieronquinn.app.smartspacer.plugin.controls.utils

object MathUtils {

    /**
     * Calculates a value in [rangeMin, rangeMax] that maps value in [valueMin, valueMax] to
     * returnVal in [rangeMin, rangeMax].
     *
     *
     * Always returns a constrained value in the range [rangeMin, rangeMax], even if value is
     * outside [valueMin, valueMax].
     *
     *
     * Eg:
     * constrainedMap(0f, 100f, 0f, 1f, 0.5f) = 50f
     * constrainedMap(20f, 200f, 10f, 20f, 20f) = 200f
     * constrainedMap(20f, 200f, 10f, 20f, 50f) = 200f
     * constrainedMap(10f, 50f, 10f, 20f, 5f) = 10f
     *
     * @param rangeMin minimum of the range that should be returned.
     * @param rangeMax maximum of the range that should be returned.
     * @param valueMin minimum of range to map `value` to.
     * @param valueMax maximum of range to map `value` to.
     * @param value to map to the range [`valueMin`, `valueMax`]. Note, can be outside
     * this range, resulting in a clamped value.
     * @return the mapped value, constrained to [`rangeMin`, `rangeMax`.
     */
    fun constrainedMap(
        rangeMin: Float, rangeMax: Float, valueMin: Float, valueMax: Float, value: Float
    ): Float {
        return lerp(rangeMin, rangeMax, lerpInvSat(valueMin, valueMax, value))
    }

    fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }

    /** Returns the saturated (constrained between [0, 1]) result of [.lerpInv].  */
    fun lerpInvSat(a: Float, b: Float, value: Float): Float {
        return saturate(lerpInv(a, b, value))
    }

    /** Returns the single argument constrained between [0.0, 1.0].  */
    fun saturate(value: Float): Float {
        return constrain(value, 0.0f, 1.0f)
    }

    fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low else if (amount > high) high else amount
    }

    /**
     * Returns the interpolation scalar (s) that satisfies the equation: `value = `[ ][.lerp]`(a, b, s)`
     *
     *
     * If `a == b`, then this function will return 0.
     */
    fun lerpInv(a: Float, b: Float, value: Float): Float {
        return if (a != b) (value - a) / (b - a) else 0.0f
    }

}