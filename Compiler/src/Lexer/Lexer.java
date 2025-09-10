package Lexer;

import java.util.ArrayList;

public class Lexer {
    String fileText;
    int filePointer = -1;

    char nextChar() {
        if (!isFileEnd())
            filePointer += 1;
        return fileText.charAt(filePointer);
    }

    String nextWord() {
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
            if (isFileEnd())
                break;
        }
        return word.toString();
    }

    char backChar() {
        if (filePointer != 0)
            filePointer -= 1;
        return fileText.charAt(filePointer);
    }

    void backWord() {
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

    String fullWord() {
        backWord();
        return nextWord();
    }

    boolean isFileEnd() {
        return (filePointer == fileText.length() - 1);
    }

    ArrayList<Token> process(String file) {
        fileText = file;
        ArrayList<Token> tokens = new ArrayList<>();
        while (true) {
            Token nextToken = nextToken();
            if (nextToken == null)
                break;
            tokens.add(nextToken);
        }
        return tokens;
    }

    Token nextToken() {
        if (isFileEnd())
            return null;
        switch (nextWord()) {
            case "class":
                return Token.tkClass;
            case "is":
                return Token.tkIs;
            case "end":
                return Token.tkEnd;
            case "return":
                return Token.tkReturn;
            case " ":
            case "\n":
                break;
            case "":
                nextChar();
            default:
                break;
        }
        return nextToken();
    }
}
