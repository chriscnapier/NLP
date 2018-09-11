/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Number.java
 *
 * FUNCTION:
 * Define enumerations and textual representation for the "Number" of 
 * a part-of-speech, such as noun or verb.
 *
 */


/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class Number {
    public static final int NONE = 0;   //As in not defined
    public static final int S = 1;      //As in "man" or "book"
    public static final int P = 2;      //As in noun "men" or "books"
    public static final int SP = S+P;   //The spelling is used for both: 
                                        //e.g. "sheep", "you" (not "yous")
    
    public static final int A = 4;     //Abstract noun 

    public static final String[] Numbers = { 
        "NONE", 
        "S", "P", "SP", "A"
    };
    
    /** Does num represent a singular number */
    public static boolean is_singular(int num) {
        boolean is = false;
        if ( (num & S) != 0)
            is = true;
        
        return is;
    }

    /** Does num represent a plural number */
    public static boolean is_plural(int num) {
        boolean is = false;
        if ( (num & P) != 0)
            is = true;
        
        return is;
    }

    /** Does num represent an abstract number */
    public static boolean is_abstract(int num) {
        boolean is = false;
        if ( (num & A) != 0)
            is = true;
        
        return is;
    }
    
    /** If det_number and noun_number correspond, return corresponding number */
    public static int correspond(int det_number, int noun_number) {
        //Corresponding S, P bits only
        int num = (det_number & noun_number) & SP;
        return num;
    }

    
    /** Return textual representation of the "Number" */
    public static String toString(int number) {
        String str = "";
        
        if (is_singular(number)) str += "Sng ";
        if (is_plural(number)) str += "Plural ";
        if (is_abstract(number)) str += "Abstact";
        
        return str;
    }

    /** Recognise the textual representation, and convert to the correct 
        enumeration, or to NONE (=0) if not recognised */
    public static int fromString(String text) {
        String text_upper = text.toUpperCase();
        for (int i = 0; i < Numbers.length; i++) {
            if (text_upper.equals(Numbers[i])) {
                return i;
            }
        }
        //Invalid given textual representation of "Number"
        return NONE;
    }
    
}
