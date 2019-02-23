package parsing

const val HASH_MIXER = -3161912963448107008L
const val HASH_SHIFT = Long.SIZE_BITS - 3

class Lexer(input: String) : LexerBase(input) {

    tailrec fun nextToken(): Token {
        startAtIndex()
        return when (current) {
            ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> {
                next()
                nextToken()
            }

            '/' -> when (next()) {
                '/' -> {
                    continueAfter('\n')
                    nextToken()
                }
                '*' -> {
                    continueAfter('*', '/')
                    nextToken()
                }
                else -> error("comments start with // or /*")
            }

            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()

            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword(current.toLong() and 31)

            '(' -> nextPooled(OPENING_PAREN)
            ')' -> nextPooled(CLOSING_PAREN)
            ';' -> nextPooled(SEMICOLON)
            '{' -> nextPooled(OPENING_BRACE)
            '}' -> nextPooled(CLOSING_BRACE)

            '!' -> nextPooled(BANG)

            '&' -> {
                if (next() != '&') error("logical and is &&")
                nextPooled(AMPERSAND_AMPERSAND)
            }

            '|' -> {
                if (next() != '|') error("logical or is ||")
                nextPooled(BAR_BAR)
            }

            EOF -> pooled(END_OF_INPUT)

            else -> error("illegal character $current")
        }
    }

    private tailrec fun number(): Token = when (next()) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()

        else -> token(NUMBER)
    }

    private tailrec fun identifier(): Token = when (next()) {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> identifier()

        else -> token(IDENTIFIER, lexeme().intern())
    }

    private tailrec fun identifierOrKeyword(letters: Long): Token {
        return when (next()) {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> identifier()

            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword(letters shl 5 or (current.toLong() and 31))

            else -> {
                val hash = (letters * HASH_MIXER ushr HASH_SHIFT).toInt()
                when (hash) {
                    1 -> if (letters == 176741L) return pooled(ELSE)
                    2 -> if (letters == 6337125L) return pooled(FALSE)
                    4 -> if (letters == 294L) return pooled(IF)
                    5 -> if (letters == 609752116L) return pooled(REPEAT)
                    6 -> if (letters == 674469L) return pooled(TRUE)
                    0 -> if (letters == 736548L) return pooled(VOID)
                    3 -> if (letters == 24388997L) return pooled(WHILE)
                }
                token(IDENTIFIER, lexeme().intern())
            }
        }
    }
}

// determine the perfect hash mixer by trying out the mantissa of a million square roots
fun main() {
    radicand@
    for (radicand in 2..1_000_000) {
        val mixer = java.lang.Double.doubleToLongBits(Math.sqrt(radicand.toDouble())) shl 12
        var used = 0L
        for (keyword in lexemePool.take(NUM_KEYWORDS)) {
            val (_, hash) = lettersAndHash(keyword, mixer)
            val bitmask = 1L shl hash
            if ((used and bitmask) != 0L) continue@radicand
            used = used or bitmask
        }
        println("const val HASH_MIXER = ${mixer}L")
        for (keyword in lexemePool.take(NUM_KEYWORDS)) {
            val (letters, hash) = lettersAndHash(keyword, mixer)
            System.out.printf("%d -> if (letters == %dL) return pooled(%s)%n",
                    hash, letters, keyword.toUpperCase())
        }
        break@radicand
    }
}

private fun lettersAndHash(keyword: String, mixer: Long): Pair<Long, Int> {
    val letters = keyword.fold(0L) { temp, ch -> temp shl 5 or (ch.toLong() and 31L) }
    val hash = (letters * mixer ushr HASH_SHIFT).toInt()
    return Pair(letters, hash)
}
