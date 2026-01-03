package com.github.h0tk3y.betterParse.lexer

public expect class RegexToken : Token {
    public constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean = false)
    public constructor(name: String?, regex: Regex, ignored: Boolean = false)

    override fun match(input: CharSequence, fromIndex: Int): Int
}

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    private companion object {
        const val inputStartPrefix = "\\A"
    }

    private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>) =
        if (patternString.startsWith(RegexToken.Companion.inputStartPrefix))
            patternString.toRegex(options)
        else {
            ("${RegexToken.Companion.inputStartPrefix}(?:$patternString)").toRegex(options)
        }

    public actual constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = prependPatternWithInputStart(patternString, emptySet())
    }

    public actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = prependPatternWithInputStart(pattern, emptySet())
    }

    private class RelativeInput(val fromIndex: Int, val input: CharSequence) : CharSequence {
        override val length: Int get() = input.length - fromIndex
        override fun get(index: Int): Char = input[index + fromIndex]
        override fun subSequence(startIndex: Int, endIndex: Int) =
            input.subSequence(startIndex + fromIndex, endIndex + fromIndex)

        override fun toString(): String = error("unsupported operation")
    }

    actual override fun match(input: CharSequence, fromIndex: Int): Int {
        val relativeInput = RelativeInput(fromIndex, input)

        return regex.find(relativeInput)?.range?.let {
            val length = it.last - it.first + 1
            length
        } ?: 0
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}

public actual class RegexToken private constructor(
    name: String?,
    ignored: Boolean,
    private val pattern: String,
    private val regex: Regex
) : Token(name, ignored) {

    private val threadLocalMatcher = object : ThreadLocal<Matcher>() {
        override fun initialValue() = regex.toPattern().matcher("")
    }

    private val matcher: Matcher get() = threadLocalMatcher.get()

    private companion object {
        private const val inputStartPrefix = "\\A"

        private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>) =
            if (patternString.startsWith(inputStartPrefix))
                patternString.toRegex(options)
            else {
                val newlineAfterComments = if (RegexOption.COMMENTS in options) "\n" else ""
                val patternToEmbed = if (RegexOption.LITERAL in options) Regex.escape(patternString) else patternString
                ("${inputStartPrefix}(?:$patternToEmbed$newlineAfterComments)").toRegex(options - RegexOption.LITERAL)
            }

    }

    public actual constructor(
        name: String?,
        @Language("RegExp", "", "") patternString: String,
        ignored: Boolean
    ) : this(
        name,
        ignored,
        patternString,
        RegexToken.Companion.prependPatternWithInputStart(patternString, emptySet())
    )

    public actual constructor(
        name: String?,
        regex: Regex,
        ignored: Boolean
    ) : this(
        name,
        ignored,
        regex.pattern,
        RegexToken.Companion.prependPatternWithInputStart(regex.pattern, regex.options)
    )

    actual override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)

        if (!matcher.find()) {
            return 0
        }

        val end = matcher.end()
        return end - fromIndex
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    /** To ensure that the [regex] will only match its pattern from the index where it is called on with
     * Regex.find(input, startIndex), set the JS RegExp flag 'y', which makes the RegExp 'sticky'.
     * See: https://javascript.info/regexp-sticky */
    private fun preprocessRegex(regex: Regex) {
        val possibleNames = listOf("nativePattern_1", "nativePattern_0", "_nativePattern")
        for(name in possibleNames) {
            val r = regex.asDynamic()[name]
            if(jsTypeOf(r) !== "undefined" && r !== null) {
                val src = r.source as String
                val flags = r.flags as String + if(r.sticky as Boolean) "" else "y"
                regex.asDynamic()[name] = RegExp(src, flags)
                break
            }
        }
    }

    public actual constructor(name: String?, patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = pattern.toRegex()
        preprocessRegex(regex)
    }

    public actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = regex
        preprocessRegex(regex)
    }

    override fun match(input: CharSequence, fromIndex: Int): Int =
        regex.find(input, fromIndex)?.range?.let {
            val length = it.last - it.first + 1
            length
        } ?: 0

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    private companion object {
        const val inputStartPrefix = "\\A"
    }

    private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>) =
        if (patternString.startsWith(RegexToken.Companion.inputStartPrefix))
            patternString.toRegex(options)
        else {
            ("${RegexToken.Companion.inputStartPrefix}(?:$patternString)").toRegex(options)
        }

    public actual constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = prependPatternWithInputStart(patternString, emptySet())
    }

    public actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = prependPatternWithInputStart(pattern, emptySet())
    }

    private class RelativeInput(val fromIndex: Int, val input: CharSequence) : CharSequence {
        override val length: Int get() = input.length - fromIndex
        override fun get(index: Int): Char = input[index + fromIndex]
        override fun subSequence(startIndex: Int, endIndex: Int) =
            input.subSequence(startIndex + fromIndex, endIndex + fromIndex)

        override fun toString(): String = error("unsupported operation")
    }

    actual override fun match(input: CharSequence, fromIndex: Int): Int {
        val relativeInput = RelativeInput(fromIndex, input)

        return regex.find(relativeInput)?.range?.let {
            val length = it.last - it.first + 1
            length
        } ?: 0
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}