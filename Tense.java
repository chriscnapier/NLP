/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Tense.java
 *
 * FUNCTION:
 * Define enumerations and textual representation for the "Tense" of a verb 
 * and the tense of a verb phrase, and participles.
 */


/*import Verb;*/

/**
 *
 * @author   Chris Napier
 * @version 1.0
 */

public class Tense {

    //Lexical (input) verb word tense definitions
    public static final int NONE = 0;               //As in not yet defined
    
    //Word tenses in input lexicon: as bit settings in an integer 
    // A verb may have 3 tenses set ...
    public static final int PRESENT = 1;               
    public static final int PAST = 2;               
    public static final int PASTPART = 4;
    
    public static final int PRESENTPART = 8;           
    
    
    
    //Verb phrase (output) return tenses
    private static final int x = 8;
    
    public static final int Present = x+1;  
    public static final int PresentPerfect = x+2;  

    public static final int Passive = x+3;  
    public static final int Progressive = x+4;  

    public static final int PerfectPassive = x+5;  
    public static final int PerfectProgressive = x+6;
    public static final int Perfect = x+7;  

    public static final int Past = x+8;  
    public static final int PastPerfect = x+9;  

    public static final int PastPassive = x+10;  
    public static final int PastProgressive = x+11;  

    public static final int PastPerfectPassive = x+12;  
    public static final int PastPerfectProgressive = x+13;
     
    public static String toString(int tense) {

        String str = "";
        
        if ((tense & PRESENT) != 0) str += "PRESENT ";               
        if ((tense & PAST) != 0) str += "PAST ";               
        if ((tense & PASTPART) != 0) str += "PASTPART ";               
        if ((tense & PRESENTPART) != 0) str += "PRESENTPART";               

        return str;
    }
    

    /** Is the given verb a present, past, past participle or present participle tense ? */
    public static boolean is_present(Verb v)    {
        return (v != null) && ((v.gettense() & PRESENT) != 0);}
    public static boolean is_past(Verb v)       {
        return (v != null) && ((v.gettense() & PAST) != 0);}
    public static boolean is_pastpart(Verb v)   {
        return (v != null) && ((v.gettense() & PASTPART) != 0);}
    public static boolean is_presentpart(Verb v){
        return (v != null) && ((v.gettense() & PRESENTPART) != 0);}

}
