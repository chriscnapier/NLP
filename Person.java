/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Person.java
 * Represewnting the three persons, singular and plural, of nouns and verbs
 *
 * @author   Chris Napier
 * @version 1.0
 */

/*import Number;
*/
/** Represewnting the three persons, singular and plural, of nouns and verbs
*/
public class Person {
    public static final int NONE = 0;
    public static final int S1 = 1;
    public static final int S2 = 2;
    public static final int S3 = 4;
    public static final int P1 = 8;
    public static final int P2 = 16;
    public static final int P3 = 32;

    public static final int INF = 64;
    public static final int ALL = S1+S2+S3+P1+P2+P3;
    
    /** Return textual representation of the "Person" */
    public static String toString(int person) {
        String p = "";
        if (person == 0) {
            p = "NONE";
        } else if ((person & INF) != 0) {
            p = "INF";
        } else if ((person & ALL) == ALL) {
            p = "ALL";
        } else {
            p = "";
            if ((person & S1) != 0) p+="1S"; 
            if ((person & S2) != 0) p+="2S"; 
            if ((person & S3) != 0) p+="3S"; 
            if ((person & P1) != 0) p+="1P"; 
            if ((person & P2) != 0) p+="2P"; 
            if ((person & P3) != 0) p+="3P"; 
        }
        
        return p;
        
    }

    /** Recognise the textual representation, and convert to the correct 
        enumeration, or to NONE (=0) if not recognised */
    public static int fromString(int number, String text) {
        int person = NONE;
        
        if (text.equals("1")) {
            switch (number) {
                case Number.S:  person = S1; break;
                case Number.P:  person = P1; break;
                case Number.SP: person = S1+P1; break;
            }
            
        } else if (text.equals("2")) {
            switch (number) {
                case Number.S:  person = S2; break;
                case Number.P:  person = P2; break;
                case Number.SP: person = S2+P2; break;
            }
            
        } else if (text.equals("3")) {
            switch (number) {
                case Number.S:  person = S3; break;
                case Number.P:  person = P3; break;
                case Number.SP: person = S3+P3; break;
            }
            
        }
        
        return person;
    }
}
