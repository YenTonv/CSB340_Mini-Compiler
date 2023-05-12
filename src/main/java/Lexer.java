import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Lexer {
    private int line;
    private int pos;
    private int position;
    private char chr;
    private String s;

    Map<String, TokenType> keywords = new HashMap<>();

    static class Token {
        public TokenType tokentype;
        public String value;
        public int line;
        public int pos;
        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token; this.value = value; this.line = line; this.pos = pos;
        }
        @Override
        public String toString() {
            String result = String.format("%5d  %5d %-15s", this.line, this.pos, this.tokentype);
            switch (this.tokentype) {
                case Integer:
                    result += String.format("  %4s", value);
                    break;
                case Identifier:
                    result += String.format(" %s", value);
                    break;
                case String:
                    result += String.format(" \"%s\"", value);
                    break;
            }
            return result;
        }
    }

    static enum TokenType {
        End_of_input, Op_multiply,  Op_divide, Op_mod, Op_add, Op_subtract,
        Op_negate, Op_not, Op_less, Op_lessequal, Op_greater, Op_greaterequal,
        Op_equal, Op_notequal, Op_assign, Op_and, Op_or, Keyword_if,
        Keyword_else, Keyword_while, Keyword_print, Keyword_putc, LeftParen, RightParen,
        LeftBrace, RightBrace, Semicolon, Comma, Identifier, Integer, String
    }

    static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }

    Lexer(String source) {
        this.line = 1;
        this.pos = 0;
        this.position = 0;
        this.s = source;
        this.chr = this.s.charAt(0);
        this.keywords.put("if", TokenType.Keyword_if);
        this.keywords.put("else", TokenType.Keyword_else);
        this.keywords.put("print", TokenType.Keyword_print);
        this.keywords.put("putc", TokenType.Keyword_putc);
        this.keywords.put("while", TokenType.Keyword_while);

    }

    /**
     * Used for situations we need to check characters ahead.
     * For example, to distinguish between > and >=.
     */
    Token follow(char expect, TokenType ifyes, TokenType ifno, int line, int pos) {
        if (getNextChar() == expect) {
            getNextChar();
            return new Token(ifyes, "", line, pos);
        }
        if (ifno == TokenType.End_of_input) {
            error(line, pos, String.format("follow: unrecognized character: (%d) '%c'", (int)this.chr, this.chr));
        }
        return new Token(ifno, "", line, pos);
    }

    /**
     *
     * @param line
     * @param pos
     * @return
     */
    Token char_lit(int line, int pos) { // handle character literals
        //System.out.println("char_lit:" + " line " + this.line + "pos " + this.pos + " this.chr " + this.chr);

        char c = getNextChar(); // skip opening quote
        int n = (int)c;
        if (c == '\\') { // check for escape sequence
            c = getNextChar();
            if (c == 'n') {
                n = 10; // newline
            } else if (c == 't') {
                n = 9; // tab
            } else if (c == 'r') {
                n = 13; // carriage return
            } else {
                error(line, pos, "unrecognized escape sequence: \\" + c);
                return getToken();
            }
        } else if (c == '\'') { // empty character literal
            error(line, pos, "empty character literal");
            return getToken();
        } else if (c == '\u0000') { // unclosed character literal
            error(line, pos, "unclosed character literal");
            return new Token(TokenType.End_of_input, "", line, pos);
        } else {
            c = getNextChar();
            if (c != '\'') { // check for closing quote
                error(line, pos, String.format("char_lit: fail to close character's quote with: : %c", c));
                return getToken();
            }
        }

        getNextChar(); // skip the closing single quote
        return new Token(TokenType.Integer, "" + n, line, pos);
    }

    /**
     *
     * @param line
     * @param pos
     * @return
     */
    Token string_lit(int line, int pos) { // handle string literals
        //System.out.println("string_lit:" + " line " + this.line + "pos " + this.pos + " this.chr " + this.chr);
        String result = "";
        char c = getNextChar(); // skip opening quote

        while (c != '"') {
            //System.out.println(c);
            if (c == '\u0000') {
                error(line, pos, "unterminated string");
                return new Token(TokenType.End_of_input, "", line, pos);
            }
            result += c;
            c = getNextChar();
        }
        getNextChar(); // skip closing double quote
        return new Token(TokenType.String, result, line, pos);
    }

    /**
     * Use when lexer encounters a '/'.
     * If it's part of a comment, skip the comment content. Otherwise it's a division operator.
     */
    Token div_or_comment(int line, int pos) { // handle division or comments
        // code here
        //System.out.println("div_or_comment:" + " line " + this.line + "pos " + this.pos + " this.chr " + this.chr);
        this.chr = this.s.charAt(this.position);

        char nextChr = getNextChar();
        if (nextChr == '/') {
            // single-line comment. Skip till
            while (this.chr != '\n' && this.chr != '\u0000') {
                this.chr = getNextChar();
            }
        } else if (nextChr == '*') {
            // multi-line comment
            while (true) {
                this.chr = getNextChar();
                if (this.chr == '\u0000') {
                    break;
                } else if (this.chr == '*' && getNextChar() == '/') {
                    getNextChar();
                    break;
                }
            }
        } else {
            // '/' is a divide operator.
            return new Token(TokenType.Op_divide, "", line, pos);
        }
        return getToken();
    }

    /**
     *
     * @param start
     * @param line
     * @param pos
     * @return
     */
    Token identifier_or_integer(char start, int line, int pos) { // handle identifiers and integers
        //System.out.println("identifier_or_integer:" + " line " + this.line + " pos " + this.pos + " this.chr " + this.chr);
        String text = "";
        char c = start;

        // If the first character is a digit, then parse it as an integer
        if (Character.isDigit(c)) {
            while (Character.isDigit(c)) {
                text += c;
                c = getNextChar();
            }
            return new Token(TokenType.Integer, text, line, pos);
        }

        // If the first character is a letter or underscore, then parse it as an identifier
        if (Character.isLetter(c) || c == '_') {
            while (Character.isLetter(c) || c == '_' || Character.isDigit(c)) {
                text += c;
                c = getNextChar();
            }
        }

        // Check if variable is a keyword
        if (this.keywords.containsKey(text)) {
            return new Token(this.keywords.get(text), text, line, pos);
        }

        // If not a keyword, return as a identifier
        return new Token(TokenType.Identifier, text, line, pos);
    }

    Token subtract_or_negate(int line, int pos) {
        System.out.println("subtract_or_negate:" + " line " + this.line + " pos " + this.pos + " this.chr " + this.chr);
        boolean is_negate = true;
        String text = "";
        getNextChar();

        // code here


        return new Token(TokenType.Op_subtract, text, line, pos);
    }

    Token getToken() {
        int line, pos;
        while (Character.isWhitespace(this.chr)) {
            getNextChar();
        }
        line = this.line;
        pos = this.pos;

        // switch statement on character for all forms of tokens with return to follow.... one example left for you
        //System.out.println("Before switch: this.chr " + this.chr);

        switch (this.chr) {
            case '\u0000': return new Token(TokenType.End_of_input, "", this.line, this.pos);
            // remaining case statements
            // Symbols
            case '(': getNextChar(); return new Token(TokenType.LeftParen, "", line, pos);
            case ')': getNextChar(); return new Token(TokenType.RightParen, "", line, pos);
            case '{': getNextChar(); return new Token(TokenType.LeftBrace, "", line, pos);
            case '}': getNextChar(); return new Token(TokenType.RightBrace, "", line, pos);
            case ';': getNextChar(); return new Token(TokenType.Semicolon, "", line, pos);
            case ',': getNextChar(); return new Token(TokenType.Comma, "", line, pos);
            // Operators
            case '*': getNextChar(); return new Token(TokenType.Op_multiply, "", line, pos);
            case '/': return div_or_comment(line, pos);
            case '%': getNextChar(); return new Token(TokenType.Op_mod, "", line, pos);
            case '+': getNextChar(); return new Token(TokenType.Op_add, "", line, pos);
            case '-': return subtract_or_negate(line, pos);
            case '<': return follow('=', TokenType.Op_lessequal, TokenType.Op_less, line, pos);
            case '>': return follow('=', TokenType.Op_greaterequal, TokenType.Op_greater, line, pos);
            case '=': return follow('=', TokenType.Op_equal, TokenType.Op_assign, line, pos);
            case '!': return follow('=', TokenType.Op_notequal, TokenType.Op_not, line, pos);
            case '&': return follow('&', TokenType.Op_and, TokenType.Identifier, line, pos);
            case '|': return follow('|', TokenType.Op_or, TokenType.Identifier, line, pos);
            case '"' : return string_lit(line, pos);
            case '\'' : return char_lit(line, pos);
            default: return identifier_or_integer(this.chr, line, pos);
        }
    }
    
    char getNextChar() {
        this.pos++;
        this.position++;
        if (this.position >= this.s.length()) {
            this.chr = '\u0000';
            return this.chr;
        }
        this.chr = this.s.charAt(this.position);
        if (this.chr == '\n') {
            this.line++;
            this.pos = 0;
        }
        return this.chr;
    }

    String printTokens() {
        Token t;
        StringBuilder sb = new StringBuilder();
        while ((t = getToken()).tokentype != TokenType.End_of_input) {
            sb.append(t);
            sb.append("\n");
            System.out.println(t);
        }
        sb.append(t);
        System.out.println(t);
        return sb.toString();
    }

    static void outputToFile(String result) {
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/prime.lex");
            myWriter.write(result);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (1==1) {
            try {

                File f = new File("src/main/resources/prime.c");
                Scanner s = new Scanner(f);
                String source = " ";
                String result = " ";
                while (s.hasNext()) {
                    source += s.nextLine() + "\n";
                }
                System.out.println("source: " + source);

                Lexer l = new Lexer(source);
                result = l.printTokens();

                outputToFile(result);

            } catch(FileNotFoundException e) {
                error(-1, -1, "Exception: " + e.getMessage());
            }
        } else {
            error(-1, -1, "No args");
        }
    }
}
