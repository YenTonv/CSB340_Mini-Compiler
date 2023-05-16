import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.testng.AssertJUnit.assertEquals;

public class LexerTest {

    @Test
    public void printTokenWith99Bottles() {
        String result = " ";
        try {
            File f = new File("src/main/resources/99bottles.c");
            Scanner s = new Scanner(f);
            String source = "";
            while (s.hasNext()) {
                source += s.nextLine() + "\n";
            }
            Lexer l = new Lexer(source);
            result = l.printTokens();
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        String expected = "";
        try {
            File f = new File("src/main/resources/99bottles_expected.lex");
            Scanner s = new Scanner(f);
            expected += s.nextLine();
            while (s.hasNext()) {
                expected += "\n" + s.nextLine();
            }
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        assertEquals(expected, result);
    }

    @Test
    public void printTokensWithPrime() {
        String result = " ";
        try {
            File f = new File("src/main/resources/prime.c");
            Scanner s = new Scanner(f);
            String source = "";
            while (s.hasNext()) {
                source += s.nextLine() + "\n";
            }
            Lexer l = new Lexer(source);
            result = l.printTokens();
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        String expected = "";
        try {
            File f = new File("src/main/resources/prime_expected.lex");
            Scanner s = new Scanner(f);
            expected += s.nextLine();
            while (s.hasNext()) {
                expected += "\n" + s.nextLine();
            }
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        assertEquals(expected, result);
    }

    @Test
    public void printTokensWithFizzbuzz() {
        String result = " ";
        try {
            File f = new File("src/main/resources/fizzbuzz.c");
            Scanner s = new Scanner(f);
            String source = "";
            while (s.hasNext()) {
                source += s.nextLine() + "\n";
            }
            Lexer l = new Lexer(source);
            result = l.printTokens();
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        String expected = "";
        try {
            File f = new File("src/main/resources/fizzbuzz_expected.lex");
            Scanner s = new Scanner(f);
            expected += s.nextLine();
            while (s.hasNext()) {
                expected += "\n" + s.nextLine();
            }
        } catch(FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        assertEquals(expected, result);
    }

}
