package parsing

const val HASH_MIXER = -9008037146329382912L
const val HASH_SHIFT = Long.SIZE_BITS - 4

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
            '_' -> identifierOrKeyword(current.toLong() and 31, current.toInt() and 96)

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
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '_' -> identifier()

        else -> token(IDENTIFIER, lexeme().intern())
    }

    private tailrec fun identifierOrKeyword(letters: Long, cases: Int): Token {
        return when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
            -> identifier()

            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword(letters shl 5 or (current.toLong() and 31), cases shl 2 or (current.toInt() and 96))

            else -> {
                val hash = (letters * HASH_MIXER ushr HASH_SHIFT).toInt()
                when (hash) {
                    8 -> if (letters == 2433774962086948L && cases == 134209504) return pooled(BEEPER_AHEAD)
                    10 -> if (letters == 176741L && cases == 8160) return pooled(ELSE)
                    13 -> if (letters == 6337125L && cases == 32736) return pooled(FALSE)
                    11 -> if (letters == 236982836680594482L && cases == 536731616) return pooled(FRONT_IS_CLEAR)
                    6 -> if (letters == 294L && cases == 480) return pooled(IF)
                    3 -> if (letters == 13694015311844402L && cases == 134078432) return pooled(LEFT_IS_CLEAR)
                    0 -> if (letters == 530500993202L && cases == 2064352) return pooled(ON_BEEPER)
                    5 -> if (letters == 609752116L && cases == 131040) return pooled(REPEAT)
                    2 -> if (letters == 658907229700101170L && cases == 536731616) return pooled(RIGHT_IS_CLEAR)
                    12 -> if (letters == 674469L && cases == 8160) return pooled(TRUE)
                    15 -> if (letters == 736548L && cases == 8160) return pooled(VOID)
                    4 -> if (letters == 24388997L && cases == 32736) return pooled(WHILE)
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
            val (_, _, hash) = lettersCasesHash(keyword, mixer)
            val bitmask = 1L shl hash
            if ((used and bitmask) != 0L) continue@radicand
            used = used or bitmask
        }
        println("const val HASH_MIXER = ${mixer}L")
        for (keyword in lexemePool.take(NUM_KEYWORDS)) {
            val (letters, cases, hash) = lettersCasesHash(keyword, mixer)
            System.out.printf("%d -> if (letters == %dL && cases == %d) return pooled(%s)%n",
                    hash, letters, cases, keyword.toUpperCase())
        }
        break@radicand
    }
}

private fun lettersCasesHash(keyword: String, mixer: Long): Triple<Long, Int, Int> {
    val letters = keyword.fold(0L) { temp, ch -> temp shl 5 or (ch.toLong() and 31L) }
    val cases = keyword.fold(0) { temp, ch -> temp shl 2 or (ch.toInt() and 96) }
    val hash = (letters * mixer ushr HASH_SHIFT).toInt()
    return Triple(letters, cases, hash)
}
