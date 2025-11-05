package Lexer;

import java.util.HashMap;

// Lexic analyzer
public class Lexer {
    // Text of the program
    private String fileText;
    // Pointer in the text
    private int filePointer = -1;
    // Map to reduce number of NamedToken objects
    private final HashMap<String, NamedToken> tokenMap = new HashMap<>();

    public Lexer(String text) {
        fileText = text;
        fillMap();
    }

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
                case '.':
                    if (word.toString().matches("-?\\d+")) {
                        word.append(nextChar);
                        break;
                    }
                case '\n':
                case ' ':
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

    // Start entier Lexer work (for first defense only)
    // public ArrayList<NamedToken> process(String file) {
    //     fillMap(); // Fill tokens map
    //     fileText = file; // Save program text
    //     ArrayList<NamedToken> tokens = new ArrayList<>();
    //     // Collect token by token
    //     while (true) {
    //         NamedToken nextToken = nextToken();
    //         if (nextToken == null)
    //             break;
    //         tokens.add(nextToken);
    //     }
    //     return tokens;
    // }

    // For bison parser
    public int getLineNumber() {
        int counter = 1;
        for (int i = 0; i < filePointer; i++) {
            if (fileText.charAt(i) == '\n') {
                counter++;
            }
        }
        return counter;
    }

    // Produce next token
    public NamedToken nextToken() {
        if (isFileEnd())
            return new NamedToken(Token.tkEOF);
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
            case "List":
            case "Array":
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
                // Check if it's a number
                if (word.matches("-?\\d+")) {
                    return new NamedToken(Token.tkNumber, word);
                }
                // Check if it's a real number
                else if (word.matches("-?\\d+\\.\\d+")) {
                    return new NamedToken(Token.tkRealLiteral, word);
                }
                // Check if it's a boolean
                else if (word.equals("true") || word.equals("false")) {
                    return new NamedToken(Token.tkBooleanLiteral, word);
                }
                // Check if it's a string literal
                else if (word.startsWith("\"") && word.endsWith("\"")) {
                    return new NamedToken(Token.tkStringLiteral, word.substring(1, word.length()-1));
                }
                // Otherwise it's an identifier
                else {
                    if (tokenMap.containsKey(word)) {
                        return tokenMap.get(word);
                    }
                    NamedToken identifier = new NamedToken(Token.tkIdentifier, word);
                    tokenMap.put(word, identifier);
                    return identifier;
                }
        }
        // Return next token, to skip useless "empty" symbols
        return nextToken();
    }
}
