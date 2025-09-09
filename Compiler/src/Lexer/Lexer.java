package Lexer;

public class Lexer {
    static String fileText;
    static int filePointer = -1;

    public static void main(String[] args) {
        // TODO: Args check
    }

    static char nextChar() {
        filePointer += 1;
        return fileText.charAt(filePointer);
    }

    static String nextWord() {
        StringBuilder word = new StringBuilder();
        boolean stop = false;
        while (true) {
            char nextChar = nextChar();
            switch (nextChar) {
                case ' ':
                case '.':
                    stop = true;
                    backChar();
                    break;
                default:
                    word.append(nextChar);
                    break;
            }
            if (stop) {
                break;
            }
        }
        return word.toString();
    }

    static char backChar() {
        if (filePointer != 0)
            filePointer -= 1;
        return fileText.charAt(filePointer);
    }

    static void backWord() {
        boolean stop = false;
        while (true) {
            if (filePointer == 0) {
                filePointer = -1;
                break;
            }
            switch (backChar()) {
                case ' ':
                case '.':
                    stop = true;
                    nextChar();
                    break;
            }
            if (stop) {
                break;
            }
        }
    }

    static String fullWord() {
        backWord();
        return nextWord();
    }

    void process() {
        // TODO: Cycle to process file
    }

    Token nexToken() {
        // TODO: FSM like switch to construct token
        return null;
    }
}
