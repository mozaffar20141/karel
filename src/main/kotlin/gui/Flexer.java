package gui;

import freditor.FlexerState;
import freditor.FlexerStateBuilder;
import freditor.persistent.ChampMap;

import static freditor.FlexerState.EMPTY;
import static freditor.FlexerState.THIS;

public class Flexer extends freditor.Flexer {
    public static final Flexer instance = new Flexer();

    private static final FlexerState END_OF_COMMENT = EMPTY.copy();
    private static final FlexerState SLASH_SLASH = new FlexerState('\n', END_OF_COMMENT).setDefault(THIS);
    private static final FlexerState SLASH_ASTERISK___ASTERISK = new FlexerState('*', THIS, '/', END_OF_COMMENT);
    private static final FlexerState SLASH_ASTERISK = new FlexerState('*', SLASH_ASTERISK___ASTERISK).setDefault(THIS);

    static {
        SLASH_ASTERISK___ASTERISK.setDefault(SLASH_ASTERISK);
    }

    private static final FlexerState NUMBER = new FlexerState("09", THIS);

    private static final FlexerState IDENTIFIER = new FlexerState("09AZ__az", THIS);

    private static final FlexerState START = new FlexerStateBuilder()
            .set('(', OPENING_PAREN)
            .set(')', CLOSING_PAREN)
            .set('{', OPENING_BRACE)
            .set('}', CLOSING_BRACE)
            .set('\n', NEWLINE)
            .set(' ', SPACE)
            .set('/', new FlexerState('*', SLASH_ASTERISK, '/', SLASH_SLASH))
            .set("09", NUMBER)
            .set("AZ__az", IDENTIFIER)
            .build()
            .verbatim(IDENTIFIER, "else", "false", "if", "repeat", "true", "void", "while")
            .verbatim(EMPTY, "!", "&&", ";", "||")
            .setDefault(ERROR);

    @Override
    protected FlexerState start() {
        return START;
    }

    @Override
    public int pickColorForLexeme(FlexerState previousState, FlexerState endState) {
        Integer color = lexemeColors.get(endState);
        return color != null ? color : 0x000000;
    }

    private static final ChampMap<FlexerState, Integer> lexemeColors = ChampMap.of(ERROR, 0x808080)
            .tup(0x808080, START::read, "/", "&", "|")
            .put(SLASH_SLASH, SLASH_ASTERISK, SLASH_ASTERISK___ASTERISK, END_OF_COMMENT, 0x008000)
            .put(NUMBER, 0x6400c8)
            .tup(0x0000ff, START::read, "else", "false", "if", "repeat", "true", "while")
            .tup(0x008080, START::read, "void")
            .tup(0xff0000, START::read, "(", ")", "{", "}")
            .tup(0x804040, START::read, "!", "&&", "||");
}
