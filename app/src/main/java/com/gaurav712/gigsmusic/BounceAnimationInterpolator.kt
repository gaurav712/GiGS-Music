package com.gaurav712.gigsmusic

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

/*
* This Code is by Evgenii Neumerzhitckii (https://github.com/evgenyneu)
* The original version was in Java. You can find it at:
* https://github.com/evgenyneu/bounce-button-animation-android

* MIT License

* Copyright (c) 2015-2020 Evgenii Neumerzhitckii
* Copyright (c) 2020 Gaurav Kumar Yadav

* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:

* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* */

open class BounceAnimationInterpolator(amplitude: Double, frequency: Double ) : Interpolator {

    private var mAmplitude = 1.0
    private var mFrequency = 10.0

    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.E.pow(-time / mAmplitude) *
                cos(mFrequency * time) + 1).toFloat()
    }

    init {
        mAmplitude = amplitude
        mFrequency = frequency
    }
}