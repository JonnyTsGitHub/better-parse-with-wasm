@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.github.h0tk3y.betterParse.lexer

/** No-op Language annotation (which is only used by the IDE for syntax highlighting) */
actual annotation class Language actual constructor(
    actual val value: String,
    actual val prefix: String,
    actual val suffix: String
)