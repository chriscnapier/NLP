/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Part_of_speech.java
 *
 * FUNCTION:
 *
 * To enumerate the parts-of-speech, recognised by the program,
 * and to give a text representation for each.
 *
 * @author   Chris Napier
 * @version 1.0
 */

public class Part_of_speech {
    public static final int NONE = 0;
    public static final int NOUN = 1;
    public static final int VERB = 2;
    public static final int ADJECTIVE = 3;
    public static final int ADVERB = 4;
    public static final int PREPOSITION = 5;
    public static final int CONJUNCTION = 6;
    public static final int DETERMINER = 7;
    public static final int PRONOUN = 8;
    public static final int PROPER_NOUN = 9;
    
    public static final int ABSTRACT_NOUN = 10; //Not a separate lexicon (yet)
    
    
    //Extra special single word "to", and "not"
    public static final int INFINITIVE = 11;
    public static final int NEGATIVE = 12;
    

    public static final int SLANG = 13;
    
    //Verb tenses
    public static final int VERB_PRESENT = 14;
    public static final int VERB_PRESENTPART = 15;
    public static final int VERB_PAST = 16;
    public static final int VERB_PASTPART = 17;

    
    // Return the string representing the given part of speech integer
    public static String toString(int pos) {
        String s;
        switch (pos) {
            case NONE: s = "Zero??"; break;
            case NOUN: s = "Noun"; break; 
            case VERB: s = "Verb"; break; 
            case ADJECTIVE: s = "Adj"; break; 
            case ADVERB: s = "Adv"; break; 
            case PREPOSITION: s = "Prep"; break; 
            case CONJUNCTION: s = "Conj"; break; 
            case DETERMINER: s = "Det"; break; 
            case PRONOUN: s = "Pron"; break; 
            case PROPER_NOUN: s = "PropNoun"; break; 
            case ABSTRACT_NOUN: s = "AbsNoun"; break; 
            
            case INFINITIVE: s = "Inf"; break;
            case NEGATIVE: s = "Neg"; break;
            
            case SLANG: s = "Slang!"; break;

            //Verb tenses
            case VERB_PRESENT: s = "Present"; break;
            case VERB_PRESENTPART: s = "PresPart"; break;

            case VERB_PAST: s = "Past"; break;
            case VERB_PASTPART: s = "PastPart"; break;
            
            default: s = "??"; break;
        }
        return s;
    }
}

