package Lexer;

import java.util.ArrayList;
import java.util.HashMap;

// Lexic analyzer
public class Lexer {
    // Text of the program
    private String fileText;
    // Pointer in the text
    private int filePointer = -1;
    // Map to reduce number of NamedToken objects
    private final HashMap<String, NamedToken> tokenMap = new HashMap<>();

    // Get next character from program text
    private char nextChar() {
        if (!isFileEnd())
            filePointer += 1;
        return fileText.charAt(filePointer);
    }

    // Construct next word from program text
    private String nextWord() {
        StringBuilder word = new StringBuilder();
        boolean stop = false;
        while (true) {
            char nextChar = nextChar();
            switch (nextChar) {
                // Symbols, that shows word end
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
                    // deal with operations signs
                    if (word.length() == 0) {
                        // Deal with ":" and ":="
                        if (nextChar == ':') {
                            if (!isFileEnd()) {
                                if (nextChar() == '=') {
                                    return ":=";
                                }
                            }
                        }
                        return word.append(nextChar).toString();
                    }
                    // Stop the builder
                    stop = true;
                    backChar();
                    break;
                // Build word
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

    // Make program text pointer go back for 1 character
    private char backChar() {
        if (filePointer != 0)
            filePointer -= 1;
        return fileText.charAt(filePointer);
    }

    // Check if pointer gone through full program text
    private boolean isFileEnd() {
        return (filePointer == fileText.length() - 1);
    }

    // Function to fill Tokens map with predefined key words
    private void fillMap() {
        for (Token t : Token.values()) {
            if (t == Token.tkIdentifier) {
                continue;
            }
            tokenMap.put(t.label, new NamedToken(t));
        }
    }

    // Start Lexer work
    public ArrayList<NamedToken> process(String file) {
        fillMap(); // Fill tokens map
        fileText = file; // Save program text
        ArrayList<NamedToken> tokens = new ArrayList<>();
        // Collect token by token
        while (true) {
            NamedToken nextToken = nextToken();
            if (nextToken == null)
                break;
            tokens.add(nextToken);
        }
        return tokens;
    }

    // Get next token
    NamedToken nextToken() {
        if (isFileEnd())
            return null;
        String word = nextWord();
        switch (word) {
            // Program key words
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
            case "then":
            case "else":
            case "(":
            case ")":
            case "[":
            case "]":
            case "var":
            case ",":
                return tokenMap.get(word);
            // Skip comment
            case "//":
                while (true) {
                    if (nextChar() == '\n') {
                        break;
                    }
                }
                // Skip empty symbols
            case "":
                nextChar();
                // Skip "empty" symbols
            case " ":
            case "\n":
            case "\r":
            case "\t":
                break;
            // Detect identifier
            default:
                if (tokenMap.containsKey(word)) {
                    return tokenMap.get(word);
                }
                tokenMap.put(word, new NamedToken(word));
                return tokenMap.get(word);
        }
        // Return next token, to skip useless "empty" symbols
        return nextToken();
    }
}
