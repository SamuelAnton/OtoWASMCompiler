package Lexer;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    String fileText;
    int filePointer = -1;
    final HashMap<String, NamedToken> tokenMap = new HashMap<>();

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
                case '\n':
                case '\r':
                case '\t':
                case '(':
                case ')':
                case '[':
                case ']':
                case ':':
                case ',':
                    if (word.isEmpty()) {
                        if (nextChar == ':') {
                            if (!isFileEnd()) {
                                if (nextChar() == '=') {
                                    return ":=";
                                }
                            }
                        }
                        return word.append(nextChar).toString();
                    }
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

    void fillMap() {
        for (Token t : Token.values()) {
            if (t == Token.tkIdentifier) {
                continue;
            }
            tokenMap.put(t.label, new NamedToken(t));
        }
    }

    ArrayList<NamedToken> process(String file) {
        fillMap();
        fileText = file;
        ArrayList<NamedToken> tokens = new ArrayList<>();
        while (true) {
            NamedToken nextToken = nextToken();
            if (nextToken == null)
                break;
            tokens.add(nextToken);
        }
        return tokens;
    }

    NamedToken nextToken() {
        if (isFileEnd())
            return null;
        String word = nextWord();
        switch (word) {
            case "class":
            case "is":
            case "end":
            case "return":
            case "this":
            case "method":
            case "=>":
            case "extends":
            case ":":
            case ".":
            case ":=":
            case "while":
            case "loop":
            case "if":
            case "else":
            case "(":
            case ")":
            case "[":
            case "]":
            case "var":
            case ",":
                return tokenMap.get(word);
            case "//":
                while (true) {
                    if (nextChar() == '\n') {
                        break;
                    }
                }
            case "":
                nextChar();
            case " ":
            case "\n":
            case "\r":
            case "\t":
                break;
            default:
                if (tokenMap.containsKey(word)) {
                    return tokenMap.get(word);
                }
                tokenMap.put(word, new NamedToken(word));
                return tokenMap.get(word);
        }
        return nextToken();
    }
}
