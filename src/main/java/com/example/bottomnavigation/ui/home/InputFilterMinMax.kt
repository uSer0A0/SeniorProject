package com.example.bottomnavigation.ui.home

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax(min: String, max: String) : InputFilter {
    private val min: Int
    private val max: Int


    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        /*source: 今入力されていて、確定していない文字列
　　　　 つまり、一般的に下線がでている変換前の文字列

        start : 今入力されていて、確定していない文字列の先頭位置
　　　　 常に 0 になる（みたいだ）

        end　 : 今入力されていて、確定していない文字列の終端位置
　　　　 常に確定していない文字列の長さになる（みたいだ）

        dest : 今EditTextに入っている文字列、確定していない文字列も含む

        dstart : 今入力されていて確定していない文字列の、 EditText
　　　　　に入っている文字列上での先頭位置
　　　　　
        dend : 今入力されていて確定していない文字列の、 EditText に
　　　　入っている文字列上での終端位置
        */
        try {
            val input = (dest.toString() + source.toString()).toInt()
            val array = dest.toString().split(',')
            if(array[0]=="0")return ""
            if (isInRange(min, max, input)) return null

        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(mina: Int, maxb: Int, inputc: Int): Boolean {
        return if (maxb > mina) inputc >= mina && inputc <= maxb else inputc >= maxb && inputc <= mina
    }

    init {
        this.min = min.toInt()
        this.max = max.toInt()
    }
}