package com.example.countdown.data

object VipCodes {
    private const val COUNT = 500

    // 500 个按年 VIP 兑换码，格式：VIP + 三位序号 + 两位校验字母
    private val yearCodes: List<String> by lazy {
        (1..COUNT).map { i ->
            val num = String.format("%03d", i)
            val c1 = ('A' + ((i * 31) % 26))
            val c2 = ('A' + ((i * 17 + 7) % 26))
            "VIP$num$c1$c2"
        }
    }

    private val yearCodeSet: Set<String> by lazy { yearCodes.toSet() }

    // 永久码 = 按年码的倒序，映射：永久码 -> 对应按年码
    private val permanentCodeMap: Map<String, String> by lazy {
        yearCodes.associateBy { it.reversed() }
    }

    fun isYearCode(code: String): Boolean = code in yearCodeSet

    fun isPermanentCode(code: String): Boolean = code in permanentCodeMap

    // 便于调试/后续对接公众号后台，可导出全部
    fun allYearCodes(): List<String> = yearCodes
}