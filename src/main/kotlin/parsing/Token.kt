package parsing

typealias TokenKind = Byte

class Token(val kind: TokenKind, val position: Int, val lexeme: String) {

    val end: Int
        get() = position + lexeme.length

    fun error(message: String): Nothing {
        throw Diagnostic(position, message)
    }

    override fun toString(): String = kind.show()

    fun toInt(range: IntRange): Int {
        try {
            val n = lexeme.toInt()
            if (n in range) return n
        } catch (_: NumberFormatException) {
            // intentional fallthrough
        }
        error("$lexeme out of range $range")
    }
}

val lexemePool = arrayOf(
        "else",
        "false",
        "if",
        "repeat",
        "true",
        "void",
        "while",
        // keywords come first and must be sorted for binary search
        "!", "&&", "(", ")", ";", "{", "||", "}",
        "number", "identifier", "end of file"
)

const val NUM_KEYWORDS = 7

fun TokenKind.show(): String = lexemePool[+this]

const val ELSE: TokenKind = 0
const val FALSE: TokenKind = 1
const val IF: TokenKind = 2
const val REPEAT: TokenKind = 3
const val TRUE: TokenKind = 4
const val VOID: TokenKind = 5
const val WHILE: TokenKind = 6

const val BANG: TokenKind = 7
const val AMPERSAND_AMPERSAND: TokenKind = 8
const val OPENING_PAREN: TokenKind = 9
const val CLOSING_PAREN: TokenKind = 10
const val SEMICOLON: TokenKind = 11
const val OPENING_BRACE: TokenKind = 12
const val BAR_BAR: TokenKind = 13
const val CLOSING_BRACE: TokenKind = 14

const val NUMBER: TokenKind = 15
const val IDENTIFIER: TokenKind = 16
const val END_OF_INPUT: TokenKind = 17
