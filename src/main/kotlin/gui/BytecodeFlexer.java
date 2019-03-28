package gui;

import freditor.FlexerState;
import freditor.FlexerStateBuilder;
import freditor.persistent.ChampMap;

import static freditor.FlexerState.THIS;

public class BytecodeFlexer extends freditor.Flexer {
    public static final BytecodeFlexer instance = new BytecodeFlexer();

    private static final FlexerState NUMBER = new FlexerState("09af", THIS);

    private static final FlexerState START = new FlexerStateBuilder()
            .set('\n', NEWLINE)
            .set(' ', SPACE)
            .set("09af", NUMBER)
            .build()
            .verbatim(FlexerState.EMPTY, "@", "CODE", "MNEMONIC",
                    "RET",
                    "MOVE", "TRNL", "TRNA", "TRNR", "PICK", "DROP",
                    "BEEP", "HEAD", "LCLR", "FCLR", "RCLR",
                    "NOT", "AND", "OR", "XOR",
                    "PUSH", "LOOP", "CALL", "JUMP", "J0MP", "J1MP")
            .setDefault(ERROR);

    @Override
    protected FlexerState start() {
        return START;
    }

    @Override
    public int pickColorForLexeme(FlexerState previousState, FlexerState endState) {
        Integer color = (previousState == NEWLINE ? afterNewline : lexemeColors).get(endState);
        return color != null ? color : 0x000000;
    }

    private static final ChampMap<FlexerState, Integer> lexemeColors = ChampMap.of(ERROR, 0x808080)
            .put(NUMBER, 0x6400c8)
            .tup(0x808080, START::read, "@", "CODE", "MNEMONIC")
            .tup(0x000080, START::read, "BEEP", "HEAD", "LCLR", "FCLR", "RCLR", "NOT", "AND", "OR", "XOR", "PUSH")
            .tup(0x400000, START::read, "RET", "LOOP", "CALL", "JUMP", "J0MP", "J1MP");

    private static final ChampMap<FlexerState, Integer> afterNewline = lexemeColors
            .put(NUMBER, 0x808080);
}
