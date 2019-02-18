package parsing

const val GOLDEN_RATIO = -7046029254386353131

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
                val hash = (letters * GOLDEN_RATIO ushr 60).toInt()
                // System.out.printf("%3d -> when (letters) { %dL -> if (cases == %d) return pooled(%s) }%n",
                //         hash, letters, cases, lexeme().toUpperCase())
                // return binarySearch(lexeme())
                when (hash) {
                    0 -> when (letters) {
                        658907229700101170L -> if (cases == 536731616) return pooled(RIGHT_IS_CLEAR)
                    }
                    1 -> when (letters) {
                        24388997L -> if (cases == 32736) return pooled(WHILE)
                    }
                    3 -> when (letters) {
                        236982836680594482L -> if (cases == 536731616) return pooled(FRONT_IS_CLEAR)
                    }
                    6 -> when (letters) {
                        530500993202L -> if (cases == 2064352) return pooled(ON_BEEPER)
                        609752116L -> if (cases == 131040) return pooled(REPEAT)
                    }
                    9 -> when (letters) {
                        2433774962086948L -> if (cases == 134209504) return pooled(BEEPER_AHEAD)
                    }
                    10 -> when (letters) {
                        6337125L -> if (cases == 32736) return pooled(FALSE)
                    }
                    11 -> when (letters) {
                        294L -> if (cases == 480) return pooled(IF)
                        736548L -> if (cases == 8160) return pooled(VOID)
                    }
                    12 -> when (letters) {
                        674469L -> if (cases == 8160) return pooled(TRUE)
                    }
                    14 -> when (letters) {
                        13694015311844402L -> if (cases == 134078432) return pooled(LEFT_IS_CLEAR)
                    }
                    15 -> when (letters) {
                        176741L -> if (cases == 8160) return pooled(ELSE)
                    }
                }
                token(IDENTIFIER, lexeme().intern())
            }
        }
    }

    fun binarySearch(lexeme: String): Token {
        val keyword = lexemePool.binarySearch(lexeme, 0, NUM_KEYWORDS)
        return when {
            keyword >= 0 -> pooled(keyword.toByte())
            else -> token(IDENTIFIER, lexeme.intern())
        }
    }
}
