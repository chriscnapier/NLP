/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * LexicalAnalysis.java
 *
 * FUNCTION:
 *
 * The program creates a number of lexicons (dictionaries) from words read from external files.
 * During lexical analysis with multiple lexicons, each word in the English sentence is looked-up
 * in every lexicon, and the results stored as separate entries in Word arrays.
 * If the word is not found in a lexicon, then further attempt(s) are made in the same lexicon,
 * with variations of the word found in the English sentence, in order to find plurals, and so on.
   For example in the verb lexicon, the “es” of the word “goes”, found in the given English sentence,
    shall be removed and the word “go” shall be looked-up. This should result in marking the found word
    as third person singular. Similarly, noun plurals are found by removing the terminating “s”, or “es”.
    To recognise the word “flies”, as the plural of “fly”, the “y” to ”i” plural shall also be processed.
 *
 */

import java.io.FileWriter;
import java.io.IOException;

/*
import Lexicon;
import Noun;
import Verb;
import Adjective;
import Adverb;
import Preposition;
import Conjunction;
import Determiner;
import Pronoun;
import Proper_noun;

import Tense;
import Person; */

/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class LexicalAnalysis extends Object {
    
    // The words of the given sentence which is to be analysed
    private String[] Sentence = null;
    private int nwords = 0;
    private FileWriter logfile = null;
    
    //The result of the search of all the words of the given sentence
    //in all the lexicons
    public static Noun[] nouns;
    public static Verb[] verbs;
    public static Adjective[] adjectives;
    public static Adverb[] adverbs;
    public static Preposition[] prepositions;
    public static Conjunction[] conjunctions;
    public static Determiner[] determiners;
    public static Pronoun[] pronouns;
    public static Proper_noun[] proper_nouns;
    
    /** Constructor: Receive and store the sentence words which is to be analysed, and
     * create the word look-up arrays */
    public LexicalAnalysis(String[] words, int n, FileWriter logfile_in) {
        
        //Store the given sentence in local Sentence array of length n
        nwords = n;
        
        if (nwords > 0) {
            Sentence = new String[nwords];
            for (int i = 0; i < nwords; i++) {
                Sentence[i] = words[i];
            }
            
            //Create arrays which will hold the result of dictionary look ups
            //of the nwords sentence words
            nouns = new Noun[nwords];
            verbs = new Verb[nwords];
            adjectives = new Adjective[nwords] ;
            adverbs = new Adverb[nwords] ;
            prepositions = new Preposition[nwords];
            conjunctions = new Conjunction[nwords];
            determiners = new Determiner[nwords];
            pronouns = new Pronoun[nwords] ;
            proper_nouns = new Proper_noun[nwords];
        }
        
        //Remember the logfile
        if (logfile_in != null)
            logfile = logfile_in;
    }

    
        /** Look up the given sentence words, in all lexicons (dictionaries),
         * allowing for plurals and capitals */
    public void look_up() {
        
        //Find each word of the sentence, in each dictionary !
        // Store results in arrays for later use in syntax analysis
        for (int k = 0; k < nwords; k++) {
            
            //Get the spelling of the sentence word at index k
            String spelling = Sentence[k];
            
            //find spelling in noun dictionary, and return a
            //cloned copy of the object from the lexicon
            nouns[k] = (Noun) Lexicon.lex[Lexicon.noun].get(spelling);
            
            //Try plural spellings
            if (nouns[k] == null) {
                nouns[k] = plural_noun_spellings(spelling);
            }
            
            //Find word at index k in the verb dictionary, into a cloned copy of
            //the lexicon item (with its tense from the dictionary)
            verbs[k] = (Verb) Lexicon.lex[Lexicon.verb].get(spelling);
            
            //set the possible person(s) of verb based on its tense
            if (verbs[k] != null) {
                //Set Person in present
                //But if same spelling in more than one tense it is wrong
                if (Tense.is_present(verbs[k])) {
                    //set the Presnt person to all but third singular,
                    //unless the past spelling is same as present spelling
                    if (Tense.is_past(verbs[k]))
                        verbs[k].putperson(Person.INF+Person.ALL); //he read
                    else
                        verbs[k].putperson(
                        Person.INF+Person.S1+Person.S2+Person.P1+Person.P2+Person.P3);
                    
                } else if (Tense.is_past(verbs[k])) {
                    //set the Presnt person to all but third singular
                    verbs[k].putperson(Person.ALL);
                    
                } else if (Tense.is_pastpart(verbs[k])) {
                    //set the Presnt person to all but third singular
                    verbs[k].putperson(Person.ALL);
                }
            } else {
                //Make verbs ending with s, es, ies, ing
                verbs[k] = alternative_verb_spellings(spelling);
            }
            
            //Find word at index k in the adjective, adverb, ... dictionaries
            //do not consider other spellings
            adjectives[k] = (Adjective) Lexicon.lex[Lexicon.adjective].get(spelling);
            adverbs[k] = (Adverb) Lexicon.lex[Lexicon.adverb].get(spelling);
            prepositions[k] = (Preposition) Lexicon.lex[Lexicon.preposition].get(spelling);
            conjunctions[k] = (Conjunction) Lexicon.lex[Lexicon.conjunction].get(spelling);
            determiners[k] = (Determiner) Lexicon.lex[Lexicon.determiner].get(spelling);
            pronouns[k] = (Pronoun) Lexicon.lex[Lexicon.pronoun].get(spelling);
            proper_nouns[k] = (Proper_noun) Lexicon.lex[Lexicon.proper_noun].get(spelling);
        }
        
        //Log the Lex analysis results
        if (logfile != null)
            log();
    }
    
    
    /** Look for plural noun spellings, and return new noun spelling & number! */
    private Noun plural_noun_spellings(String spelling) {
        
        Noun n = null;
      
        //Nouns also with trailing s (except -lys) or es (-ie -> y) are allowed
        int len = spelling.length();
        if (len > 2 && spelling.endsWith("s") && !spelling.endsWith("lys")) {
            String sub = spelling.substring(0, len-1);
            n = (Noun) Lexicon.lex[Lexicon.noun].get(sub);
            //Here and below the changes made in noun[k] are in acloned
            //object not in the lexicon.
            if (n != null) {
                n.putnumber(Number.P);
            }
        }
        if (n == null && len > 3 && spelling.endsWith("es")) {
            String sub = spelling.substring(0, len-2);
            n = (Noun) Lexicon.lex[Lexicon.noun].get(sub);
            if (n != null) {
                n.putnumber(Number.P);
                
                // "flies" -> "fly"
                // (If noun "flies" is in given sentence lookup "fly")
            } else {
                if (sub.endsWith("i")) {
                    String sub_y = sub.substring(0, len-3) + "y";
                    n = (Noun) Lexicon.lex[Lexicon.noun].get(sub_y);
                    if (n != null) {
                        n.putnumber(Number.P);
                    }
                }
            }
        }
        // Finally, put the new plural spelling into the array object
        if (n != null) {
            n.putspelling(spelling);
        }
        
        return n;
    }
    
    
    /** Look for alternative verb spellings: return new verb spelling, tense, person */
    private Verb alternative_verb_spellings(String spelling) {
        Verb v = null;
        
        int len = spelling.length();
        
        if (spelling.equals("am")) {
            v = (Verb) Lexicon.lex[Lexicon.verb].get("be");
            //set the person to first singular
            v.putperson(Person.S1);
            
            
        } else if (spelling.equals("is")) {
            v = (Verb) Lexicon.lex[Lexicon.verb].get("be");
            //set the person to third singular
            v.putperson(Person.S3);
            
        } else if (spelling.equals("are")) {
            v = (Verb) Lexicon.lex[Lexicon.verb].get("be");
            //set the person to third singular
            v.putperson(Person.S2+Person.P1+Person.P2+Person.P3);
            
        } else if (spelling.equals("were")) {
            v = (Verb) Lexicon.lex[Lexicon.verb].get("was");
            //set the person: you were, we 
            v.putperson(Person.S2+Person.P1+Person.P2+Person.P3);
            
        } else if (spelling.equals("has")) {
            v = (Verb) Lexicon.lex[Lexicon.verb].get("have");
            //set the person to third singular
            v.putperson(Person.S3);
        }
        
        
        //Try to find spelling without the s, in the lexicon, as present only
        if (v == null) {
            if (len >= 3 && spelling.endsWith("s") && !spelling.endsWith("lys")) {
                
                v = (Verb) Lexicon.lex[Lexicon.verb].get(spelling.substring(0, len-1));
                
                if (v != null) {
                    if (Tense.is_present(v)) {
                        //set the person to third singular
                        v.putperson(Person.S3);
                        
                    } else {
                        //If we got a verb, but it is not presnet then
                        // this is not a valid verb
                        //Do not allow: He wrote-s
                        v = null;
                    }
                }
            }
            
        }
        
        //If we didn't find spelling without the s: try -es, -ies and -ing
        if (v == null) {
            //goes, does
            if (len >= 4 && spelling.endsWith("es")) {
                String sub = spelling.substring(0, len-2);
                if (spelling.equals("does")) {
                    v = (Verb) Lexicon.lex[Lexicon.verb].get("do");
                } else if (spelling.equals("goes")) {
                    v = (Verb) Lexicon.lex[Lexicon.verb].get("go");
                    
                    // "flies" -> "fly"
                    // (If verb "flies" is in given sentence lookup "fly")
                } else if (sub.endsWith("i")) {
                    String sub_y = sub.substring(0, len-3) + "y";
                    v = (Verb) Lexicon.lex[Lexicon.verb].get(sub_y);
                }
                
                if (v != null) {
                    if (Tense.is_present(v)) {
                        //set the person to third singular
                        v.putperson(Person.S3);
                        
                    } else {
                        //If we got a verb, but it is not presnet then
                        // this is not a valid verb
                        //Do not allow: He wrote-s
                        v = null;
                    }
                }
                
                // Verb-ing
            } else if (len >= 5 && spelling.endsWith("ing")) {
                
                String sub = spelling.substring(0, len-3); //walking -> walk;
                v = (Verb) Lexicon.lex[Lexicon.verb].get(sub);
                
                //But if the stem ends with e its wrong, except be, -ee
                if (v != null) { 
                    String gs = v.getspelling();
                    if (gs.endsWith("e") && !gs.equals("be") && !gs.endsWith("ee")) {
                            v = null;
                    }
                }
                
                //living -> live
                if (v == null) {
                    sub = sub + "e";                //living -> liv -> live
                    v = (Verb) Lexicon.lex[Lexicon.verb].get(sub);
                }
                
                //running -> run; sitting -> sit; bidding -> bid
                if (v == null) {
                    if (spelling.endsWith("ning") ||
                        spelling.endsWith("ting")) {
                        sub = spelling.substring(0, len-4);
                        v = (Verb) Lexicon.lex[Lexicon.verb].get(sub);
                    }
                }
                
                //If we got a verb, but it is not present then
                // this is not a valid present participle, so remove it
                if (!Tense.is_present(v)) {
                    v = null;
                }
                
                
                //Present participle generated from present tense, with all persons
                if (v != null) {
                    v.puttense(Tense.PRESENTPART);
                    v.putperson(Person.ALL);
                }
            }
        }
        
        // Finally, put the new spelling into the object which shall be returned
        if (v != null) {
            v.putspelling(spelling);
        }
        return v;
    }
    
    
    /** Check that all given English sentence words have correct spelling */
    public boolean check(String[] errors) {
        boolean correct = true;
        
        String message = new String();
        boolean first_error = true;
        
        //For each sentence word
        for (int k = 0; k < nwords; k++) {
            //if its spelling was not found in any lexicon
            if (nouns[k] == null &&
            verbs[k] == null &&
            adjectives[k] == null &&
            adverbs[k] == null &&
            prepositions[k] == null &&
            conjunctions[k] == null &&
            determiners[k] == null &&
            pronouns[k] == null &&
            proper_nouns[k] == null) {
                
                //Word at index k is not in any dictionary
                //Make an error message string. 
                correct = false;
                if (!first_error) {
                    message += ",";     //Comma after each word, except first
                }
                message += " " + Sentence[k];
                
                first_error = false;
            }
        }
        //If there were incorrect spellings return the message string to
        //the caller by the string array parameter
        if (!correct) {
            errors[0] = message;
        }
        return correct;
    }
    
    
    /** Log all the results of the look-up to logfile */
    private void log() {
        try {
            logfile.write("\n\n\nLexical Analysis Results\n");
            logfile.write("------------------------");
            
            //Get the spelling of the sentence word at index k
            logfile.write("\n");
            for (int k = 0; k < nwords; k++)
                logfile.write(Sentence[k]+" ");
            
            logfile.write("\n");
            
            for (int k = 0; k < nwords; k++) {
                logfile.write("\n----> " + (k+1));
                
                if (nouns[k] != null) logfile.write("\n" + nouns[k].toString());
                if (verbs[k] != null) logfile.write("\n" + verbs[k].toString());
                if (adjectives[k] != null) logfile.write("\n" + adjectives[k].toString());
                if (adverbs[k] != null) logfile.write("\n" + adverbs[k].toString());
                if (prepositions[k] != null) logfile.write("\n" + prepositions[k].toString());
                if (conjunctions[k] != null) logfile.write("\n" + conjunctions[k].toString());
                if (determiners[k] != null) logfile.write("\n" + determiners[k].toString());
                if (pronouns[k] != null) logfile.write("\n" + pronouns[k].toString());
                if (proper_nouns[k] != null) logfile.write("\n" + proper_nouns[k].toString());
                
            }
            
        } catch (IOException e) {System.out.println(e);}
        
    }
}
