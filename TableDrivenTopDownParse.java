/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * TableDrivenTopDownParse.java
 *
 * FUNCTION:
 * A table driven top down parse shall be used to analyse the English sentence.
 * That is, the valid sentence structures shall be separately defined in a table
 * as combinations of defined sentence parts.
 *ad
 * These sentence parts were already given in the separate Introduction document,
 * and are extended in Sentence_types.java
 *
 * Productions are followed by a top down (recursive) parse.
 * Backtracking is allowed, and is simply implemented by saving and
 * restoring the current word count and the output stack pointer.
 *
 * The found productions and parts of speech, as enumerations, are pushed onto
 * a stack, which after a successful parse, represents the sentence structure.
 *
 * Method toString() creates a string of all the sentence parts and parts
 * of speech as found during a successful parse, from the stack.
 * For example toString returns the string:
 *     Sentence[NP[(Pron I)], VP[(Verb know)], Wh[(Pron who)],
 *     Clause[NP[(Pron you)], VP[(Verb are)]]]
 *
 *
 */

/*import Part_of_speech;
import Lexicon;
import LexicalAnalysis;

import Word;
import Noun;
import Verb;
import Determiner;
import Preposition;
import Tense;
import Person;

import Sentence_types;
*/

import java.io.FileWriter;
import java.io.IOException;

/*import Parts;*/

/**
 *
 * @author  Chris Napier
 * @version 1.0
 */

/** Parse a given sentence using sentence type definitions */
public class TableDrivenTopDownParse extends Object {
    
    //The words of the sentence which is to be parsed
    private String[] words;
    
    //The number of the current word from 1 to words.length, or
    //words.length+1 when the end of the sentence is found
    private int current_word_count;
    
    //The index of the highest successfully scanned word
    public int Error_at = 0;
    
    //The spelling of the word in the sentence which is
    // currently being scanned, or "" at the end of the sentence
    private String current_word = null;
    
    private Word current_word_lexical = null;
    
    private int Noun_phrase_person = Person.NONE;
    private int Noun_phrase_number = Number.NONE;   //Bits
    
    private int Verb_phrase_person = Person.NONE;
    private int Verb_phrase_number = Number.NONE;
    
    private int Verb_phrase_tense = Tense.NONE;
    
    //The result of a previous lexical analusis of the English sentence words
    private LexicalAnalysis lexical;
    
    //The log file object
    private FileWriter logfile;
    
    //The storage of certain sentence parts for person/tense checks
    private Parts parts = null;
    
    //Flags of clause begin word number and first, used for clause arcs drawing
    private int main_clause_begin = 0;
    private boolean first_main_clause = true;
    
    //For error message
    int saved_sentence_type;
    
    //Noun phrase temporary flag: "and" found between NP parts
    boolean noun_phrase_and_found = false;
    
  
    //--------------------------------------------------------------------------
    /** Constructor: Receive the English sentence words which is to be parsed,
     * and receive the previous performed lexical analysis of that sentence */
    public TableDrivenTopDownParse(String[] sentence, int nwords, LexicalAnalysis lexical_in,
    FileWriter logfile_in) {
        
        //Store the given sentence in words and in local words arrays
        words = new String[nwords];
        
        
        for (int i = 0; i < words.length; i++) {
            words[i] = sentence[i];
        }
        
        //Store given lexical analysis results
        lexical = lexical_in;
        
        //Remember the given file
        logfile = logfile_in;
        
        //Initialize local flags:t
        //syntax error at word number Error_at
        Error_at = 0;
        
        //The semantic check class
        parts = new Parts(3*nwords);
        
        //Log the sentence words
        try{
            logfile.write("\n--------------------------------------------------------");
            logfile.write("\nSentence: ");
            for (int k = 0; k < words.length; k++)
                logfile.write(" " + words[k]);
            
        } catch (IOException e) {System.out.println(e);}
    }
    
    //--------------------------------------------------------------------------
    // Productions
    // ===========
    
    /** Production: The highest production representing a compound sentence or a clause */
    public boolean sentence(int sentence_type, boolean is_sentence_production) {
        boolean correct = false;
        
        try{
            // Optionally start the parse to the first word of the sentence,
            // and to the initial sentence/clause level, except when parsing
            // a clause when the parse continues from the current word.
            if (is_sentence_production) {
                current_word_count = 0;     //Start before first word of sentence
                stack_count = 0;            //Reset stack of found parts (of speech, of sentence)
                
                //Log the sentence type number and structure
                logfile.write("\n\nType "+sentence_type+": " +
                Sentence_types.getstructure(sentence_type));
                
                push_production_start(Sentence_types.Sentence); //Mark the start on stack
                next_word();                //Get the first word of the sentence
                
                //Mark start of next (main) clause
                main_clause_begin = current_word_count;
                first_main_clause = true;
                
                //Remove all parts and reset mark
                parts.clear();
                
            } else {
                
                //Log the clause type number and structure on a new line.
                logfile.write("\n\n\tClause Type "+sentence_type+": " +
                Sentence_types.getstructure(sentence_type) +"\n\t");
                
                //Reset the stored sentence parts (of person info) to the marked point
                parts.reset();
            }
            
            //If a correct single sentence, driven from the table for this type, is found
            if ( sentence_1(sentence_type, is_sentence_production) ) {
                
                //If the whole sentence is consumed (the count is after the end of
                //the sentence, we are finished.
                if (current_word_count == words.length + 1) {
                    correct = true;
                    
                    //If there is more English sentence, then
                    //allow sentence continuation after a conjunction followed by a clause
                } else if (conj_clause(true)) {
                    correct = true;
                    
                    
                }
            }
            
            if (correct) {
                //Only for a sentence production (not a clause)
                if (is_sentence_production) {
                    push_production_end(Sentence_types.Sentence, current_word_count - 1);
                }
            }
            
        } catch (IOException e) {System.out.println(e);}
        
        return correct;
    }
    
    /** Production: The fundamental production representing a single sentence */
    public boolean sentence_1 (int sentence_type, boolean is_sentence_production) {
        //This sentence type does not match the actual sentence (yet)
        boolean match = false;
        
        //Get the number of sentence parts in this sentence_type
        int nparts = Sentence_types.Definitions[sentence_type].length;
        
        //For each required sentence part in this sentence type
        for (int part = 0; part < nparts; part++) {
            match = false;
            
            //What is the required part
            int required = Sentence_types.Definitions[sentence_type][part];
            switch (required) {
                
                //Scan for the required part starting from the current position
                //in the sentence, indicating if a match is found.
                //(true) parameter given to noun_phrase means allow following
                //prepositional phrases
                case Sentence_types.N: match = noun_phrase(true); break;
                case Sentence_types.V: match = verb_phrase();
                
                //Error if there is one verb part, which is not infinitive form
                //in a sentence production (not a clause)
                //if (is_sentence_production) {
                    //if (match && (nparts == 1) && (Verb_phrase_person & Person.INF) == 0)
                        //match = false;
                //}
                break;
                
                case Sentence_types.VN: match = verb_noun_phrase();
                
                //Error if one verb part, which is not infinitive form
                //in a sentence production (not a clause)
                //if (is_sentence_production) {
                    //if (match && (part == 0) && (Verb_phrase_person & Person.INF) == 0)
                        //match = false;
                //}
                break;
                
                case Sentence_types.Adjective: match = adjectives(); break;
                case Sentence_types.Adverbial: match = adverbial_phrase(); break;
                case Sentence_types.That_Clause: match = that_clause(); break;
                case Sentence_types.Wh_Clause: match = wh_clause(false); break;
                case Sentence_types.Wh_Clause_question: match = wh_clause(true); break;
                case Sentence_types.To_Clause: match = to_clause(true); break;
                case Sentence_types.Wh_To_Clause: match = wh_to_clause(); break;
                case Sentence_types.Verbing: match = verbing(true); break;
                case Sentence_types.Pastparticiple: match = pastparticiple(); break;
                default: match = false;
            }
            
            //Finish early if the required part of speech does not match
            // the current words of the sentence
            if (!match) {
                break;
            }
        } //for each part of a sentence
        
        //Have all the parts matched (up to the current word in the English sentence)
        return match;
    }
    
    /** Production: Parse a Clause as a sentence within a sentence */
    private boolean clause(int st1, int st2) {
        boolean match = false;
        
        // Remember the current word, etc., to allow reset on no match
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        // Try only suitable sentence types, based on given clause type
        //check the given static ranges are valid, and set to valid.
        if (st1 <= 0)
            st1 = 0;
        if (st2 <=0)
            st2 = Sentence_types.Definitions.length;
        
        if (st1 > Sentence_types.Definitions.length)
            st1 = Sentence_types.Definitions.length;
        if (st2 > Sentence_types.Definitions.length)
            st2 = Sentence_types.Definitions.length;
        
        //Clause loop: For each sentence/clause type
        for (int st = st1; st < st2; st++) {
            //Stack Clause begin
            //NOT push_production_start(Sentence_types.Clause);
            
            //Starting at the current word, try to match the remaining
            //sentence words to a sentence of type st
            // (We assume that clause is the same as a sentence)
            match = sentence(st, false);
            
            // Do not look further when a clause type matches
            if (match) {
               /* If this is to be improved, then we must continue here
                *  instead of break,
                * and then we need to use a tree instead of a stack.
                * And when we get say two clause matches, we link to two
                * branches. Nested clauses lead to nested branching in the tree
                */
                
                //Set structs string: The recognised symbolic sentence parts
                System.out.println(" Clause type (" + st + "): " +
                Sentence_types.getstructure(st));
                break;
                
            } else {
                reset(save, save1, save2);
                
            }
        }
        if (match) {
            
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume the current word(s) if they match a noun phrase */
    public boolean noun_phrase(boolean pp_allowed) {
        boolean match = false;
        //Noun phrase is determiner, [adjectives], noun|pronoun|proper_noun or
        //Noun phrase is [adjectives], noun|pronoun|proper_noun
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.N);
        
        Noun_phrase_number = Number.NONE;   //We set bits into this
        Noun_phrase_person = Person.NONE;   //We set bits into this
        
        //Allow multiple parts, separated by conj:  A man and his dog
        boolean more = true;
        boolean determiner_found = false;
        
        noun_phrase_and_found = false;
        
        //allow negation
        if (is_spelling("not")) {
            push(Part_of_speech.NEGATIVE);
            next_word();
        }
        //allow adjective use of "only"
        if (is_spelling("only")) {
            push(Part_of_speech.ADJECTIVE);
            next_word();
        }
        
        //Find determiner form of noun:  (true->First part)
        if (np_1(true)) {
            determiner_found = true;
            match = true;
            
            //Find plural or Abstract or continuing form (true->First part)
        } else if (np_2(true, determiner_found)) {
            match = true;
        }
        
        
        //If NP part found
        if (match) {
            //Continuation allowed
            while (np_0(determiner_found)) {}
        }
        
        
        //For proper noun phrase, save and check its "person"
        //except for a wh-: For   "Who am I":   "Who" is skipped here
        if (match) {
            match = parts.push(Sentence_types.N, Noun_phrase_person);
        }
        
        if (match) {
            //save because prep phrase can overwrite number
            int nps = Noun_phrase_number;
            int npp = Noun_phrase_person;
            
            //Optional prepositonal phrase(s) if allowed
            if (pp_allowed) {
                while (prepositional_phrase()) {}
            }
            //Restore n.._p.._s..
            Noun_phrase_number = nps;
            Noun_phrase_person = npp;
            
            push_production_end(Sentence_types.toPerson(Noun_phrase_person),
            current_word_count - save);
            //push_production_end(Sentence_types.toNumber(Noun_phrase_number),
            //current_word_count - save);
            push_production_end(Sentence_types.N, current_word_count - save);
        } else {
            reset(save, save1, save2);
        }
        return match;
    }
    
    /** Production: Continuation of noun list */
    public boolean np_0(boolean determiner_found) {
        
        boolean match = false;
        //Noun phrase is determiner, [adjectives], noun|pronoun|proper_noun or
        //Noun phrase is [adjectives], noun|pronoun|proper_noun
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        
        //Allow multiple parts, separated by conj:  A man and his dog
        boolean more = false;
        
        //a conjunction, implies continuation of NP
        if (is_spelling("and")) {
            push(Part_of_speech.CONJUNCTION);
            next_word();
            
            //Look ahead !!
            //Count the "and's" to indicate plural
            //but "and not" doesnt count as plural
            if (!is_spelling("not") && !is_spelling("only")) {
                noun_phrase_and_found = true;
            }
            
            more = true;
            
        } else if (is_spelling("or")) {
            push(Part_of_speech.CONJUNCTION);
            next_word();
            more = true;
            
        } else if (is_spelling("but")) {
            push(Part_of_speech.CONJUNCTION);
            next_word();
            more = true;
            
            
        }
        
        
        if (more) {
            //allow negation
            if (is_spelling("not")) {
                push(Part_of_speech.NEGATIVE);
                next_word();
            }
            //allow adjective use of "only" and "also"
            if (is_spelling("only")) {
                push(Part_of_speech.ADJECTIVE);
                next_word();
            } else if (is_spelling("also")) {
                push(Part_of_speech.ADVERB);
                next_word();
            }
            
            //Find determiner form: (false means second or later part)
            if (np_1(false)) {
                match = true;
                
                //Find plural or Abstract or continuing form: (false means second or later part)
            } else if (np_2(false, determiner_found)) {
                match = true;
                
                //Syntax does not match a NP
            } else {
                match = false;
            }
            
            
        } else {
            //No conjuncton found, so end of NP
            
            //Finally:
            //Make it a plural, if there is an "and" but not "and not"
            //between the noun parts
            if (noun_phrase_and_found) {
                Noun_phrase_number = Number.P;  //Make plural
                Noun_phrase_person = Person.P3; //Make plural third person
            }
            
        }
        
        if (match) {
        } else {
            reset(save, save1, save2);
        }
        return match;
    }
    
    
    /** Determiner noun phrase */
    public boolean np_1(boolean first_part) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Noun_phrase_person = Person.NONE;
        //push_production_start(Sentence_types.N);
        
        //Must have a determiner
        if (is_word(Part_of_speech.DETERMINER)) {
            
            boolean is_a = is_spelling("a");
            boolean is_an = is_spelling("an");
            
            int det_number = current_word_lexical.getnumber();
            
            //If the determiner is a or an, compare it with the
            //first letter of the spelling of the next word
            next_word();
            
            boolean ok = false;
            if (is_a) {
                // "a" followed by non-vowel is ok
                ok = !is_vowel_begin();
                
            } else if (is_an) {
                // "an" followed by vowel is ok
                ok = is_vowel_begin();
                
            } else {
                //neither "a" nor "an" is ok
                ok = true;
            }
            
            //Only proceed when the a/an/other is correct
            if (ok) {
                
                //Allow preceeding adjectives
                if (adjectives()) {}
                
                // Noun
                if (is_noun()) {
                    //It must correspond in Number to the determiner
                    match = noun_correspond(first_part, det_number);
                }
            }
            
        }
        
        if (match) {
            //push_production_end(Sentence_types.toPerson(Noun_phrase_person),
            //                    current_word_count - save);
            //push_production_end(Sentence_types.N, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Does noun correspond to determiner */
    private boolean noun_correspond(boolean first_part, int det_number) {
        boolean match = false;
        
        //Number and person of this noun phrase part
        int number = Number.NONE;
        int person = Person.NONE;
        
        //Number is taken from noun if the determiner is SP
        int noun_number = current_word_lexical.getnumber();
        
        //There must one or more corresponding bits, to indicate matching
        number = Number.correspond(det_number, noun_number); 
        if (number != Number.NONE) {
            match = true;
            next_word();
        }
        
        //If the determiner and the noun correspond in number
        if (match) {
            //Set the person as S3 and/or P3
            person = Person.NONE;
            
            if (Number.is_singular(number))
                person |= Person.S3;
            
            if (Number.is_plural(number))
                person |= Person.P3;
            
            //If this is the first noun part, set the phrase values
            //(the second part is combined later)
            if (first_part) {
                Noun_phrase_number |= number;
                Noun_phrase_person |= person;
            }
            
        }
        return match;
    }
    
    /** Non-determiner noun phrase */
    public boolean np_2(boolean first_part, boolean determiner_found) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Number and person of this noun phrase part
        int number = Number.NONE;
        int person = Person.NONE;
        
        //push_production_start(Sentence_types.N);
        //Noun_phrase_person = Person.NONE;
        
        //Allow preceeding adjectives, and record in adj_found
        //if there were any, for later pronoun check
        boolean adj_found = adjectives();
        
        // Noun, Pronoun or Proper noun
        if (is_noun()) {
            //A noun without a determiner must be plural
            int noun_number = current_word_lexical.getnumber();
            if (Number.is_plural(noun_number)) {
                number = Number.P;
                person = Person.P3;
                match = true;
                next_word();
                
                //but Abstract nouns can be singiular without determiner.
                //We  can have missing determiner (before girl) in forms like:
                // The boy and girl
            } else if (Number.is_abstract(noun_number) || determiner_found) {
                number = Number.S;
                person = Person.S3;
                match = true;
                next_word();
            }
            
        } else if (is_proper_noun()) {
            number = Number.S;
            person = Person.S3;
            //proper_noun does not need a determiner, and can have adjectives
            match = true;
            next_word();
            
            //Pronoun except wh- which is separately recognised
        } else if (is_pronoun()) {   //&& (is_wh() == Part_of_speech.NONE)) {
            
            //neither determiner nor adjectives are allowed before a pronoun
            if (!adj_found) {
                number = current_word_lexical.getnumber();
                person = current_word_lexical.getperson();
                if (is_wh() == Part_of_speech.PRONOUN)
                    match = true;  //Who, whom, what, which, whose
                else 
                    match = true;
                
                next_word();
            }
            
            
        }
        
        if (match) {
            
            if (first_part) {
                Noun_phrase_number |= number;
                Noun_phrase_person |= person;
            }
            
            //push_production_end(Sentence_types.toPerson(Noun_phrase_person),
            //                    current_word_count - save);
            //push_production_end(Sentence_types.N, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }

    /** Production: Consume the current word(s) if they match a verb phrase */
    private boolean verb_phrase() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        int vpn = 0;
        int vpp = 0;
        int vpt = 0;
        
        push_production_start(Sentence_types.V);
        
        if (verb()) {
            
            match = parts.push(Sentence_types.V, Verb_phrase_person);
       
            if (match) {
                
                //Hold those numbers
                vpn = Verb_phrase_number;
                vpp = Verb_phrase_person;
                vpt = Verb_phrase_tense;
                
                //Optional adverbial phrases, after the verb
                while (adverbial_phrase()) {
                }
            }
        }
        
        if (match) {
            Verb_phrase_number = vpn;
            Verb_phrase_person = vpp;
            Verb_phrase_tense = vpt;
            
            push_production_end(Sentence_types.toPerson(Verb_phrase_person),
                current_word_count - save);
            //push_production_end(Sentence_types.toNumber(Verb_phrase_number),
            //current_word_count - save);
            push_production_end(Sentence_types.toPart(Verb_phrase_tense),
            current_word_count - save);
            push_production_end(Sentence_types.V, current_word_count - save);
        } else {
            
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    
    
    /** Production: Consume the current word(s) if they match a verb_noun phrase */
    private boolean verb_noun_phrase() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        int vpn = 0;
        int vpp = 0;
        int vpt = 0;
        
        push_production_start(Sentence_types.VN);
        
        if (verb()) {
            
            //Save person info on the scanned verb phrase, for later person check
            //For proper verb phrase, save it and possibly check its "person"
            if (parts.push(Sentence_types.V, Verb_phrase_person)) {
                //Hold those numbers
                vpn = Verb_phrase_number;
                vpp = Verb_phrase_person;
                vpt = Verb_phrase_tense;
                
                //Optional adverbial phrase(s)
                while (adverbial_phrase()) {
                }
                
                //Noun phrase, without prepositional phrases
                if (noun_phrase(false)) {
                    match = true;
                }
            }
        }
        if (match) {
            Verb_phrase_number = vpn;
            Verb_phrase_person = vpp;
            Verb_phrase_tense = vpt;
            
            push_production_end(Sentence_types.toPerson(Verb_phrase_person),
            current_word_count - save);
            //push_production_end(Sentence_types.toNumber(Verb_phrase_number),
            //current_word_count - save);
            push_production_end(Sentence_types.toPart(Verb_phrase_tense),
            current_word_count - save);
            push_production_end(Sentence_types.VN, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume a verb word */
    private boolean verb() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        

        //Optional adverbs
        while (is_adverb_except_wh()) {
            next_word();
        }
        
        if (is_word(Part_of_speech.VERB)) {
            
            //For example: have walked
            if (vb_1()) {
                match = true;
                
                //For example: have
            } else if (vb_2()) {
                match = true;
                //We have found an auxilliary verb (used in parts object)
            }
            
        }
        
        if (match) {
            
        } else {
            reset(save, save1, save2);
        }
        return match;
    }
    
    /** First Verb form:
     * present |
     * past |
     * am/are/is present_participle |
     * have/has been present_participle |
     * have/has past_participle */
    private boolean vb_1() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Present
        if (current_word.equals("am") || current_word.equals("are") ||
        current_word.equals("is")) {
            if (current_word.equals("am")) {
                Verb_phrase_number = Number.S;
                Verb_phrase_person = Person.S1;
            } else if (current_word.equals("is")) {
                Verb_phrase_number = Number.S;
                Verb_phrase_person = Person.S3;
            } else if (current_word.equals("are")) {
                Verb_phrase_number = Number.S+Number.P;
                Verb_phrase_person = Person.S2+Person.P1+Person.P2+Person.P3;
            }
            
            next_word();
            
            //Optional adverbs except wh-
            while (is_adverb_except_wh()) {
                next_word();
            }
            
            if (is_word(Part_of_speech.VERB)) {
                //Progressive
                if (is_tense(Tense.PRESENTPART)) {
                    
                    Verb_phrase_tense = Tense.Progressive;
                    next_word();
                    match = true;
                    
                    //Passive
                } else if (is_tense(Tense.PASTPART)) {
                    
                    Verb_phrase_tense = Tense.Passive;
                    next_word();
                    match = true;
                }
            }
            
            //Past
        } else if (current_word.equals("was") || current_word.equals("were")) {
            if (current_word.equals("was")) {
                Verb_phrase_number = Number.S;
                Verb_phrase_person = Person.S1+Person.S3;
            } else if (current_word.equals("were")) {
                Verb_phrase_number = Number.S+Number.P;
                Verb_phrase_person = Person.S2+Person.P1+Person.P2+Person.P3;
            }
            
            next_word();
            
            //Optional adverbs except wh-
            while (is_adverb_except_wh()) {
                next_word();
            }
            
            if (is_word(Part_of_speech.VERB)) {
                //Progressive
                if (is_tense(Tense.PRESENTPART)) {
                    
                    Verb_phrase_tense = Tense.PastProgressive;
                    next_word();
                    match = true;
                    
                    //Passive
                } else if (is_tense(Tense.PASTPART)) {
                    
                    Verb_phrase_tense = Tense.PastPassive;
                    next_word();
                    match = true;
                }
            }
            
            //Present Perfect:
        } else if (current_word.equals("have") || current_word.equals("has")) {
            
            if (current_word.equals("have")) {
                Verb_phrase_number = Number.S+Number.P;
                Verb_phrase_person = Person.S1+Person.S2+Person.P1+Person.P2+Person.P3;
            }  else if (current_word.equals("has")) {
                Verb_phrase_number = Number.S;
                Verb_phrase_person = Person.S3;
            }
            next_word();
            
            //Optional adverbs
            while (is_adverb_except_wh()) {
                next_word();
            }
            
            if (is_spelling("been")) {
                push(Part_of_speech.VERB_PASTPART);
                Verb_phrase_tense = Tense.Perfect;
                next_word();
                
                //Optional adverbs
                while (is_adverb_except_wh()) {
                    next_word();
                }
                
                if (is_word(Part_of_speech.VERB)) {
                    //Perfect progressive
                    if (is_tense(Tense.PRESENTPART)) {
                        Verb_phrase_tense = Tense.PerfectProgressive;
                        
                        next_word();
                        match = true;
                        
                        //Perfect Passive
                    } else if (is_tense(Tense.PASTPART)) {
                        Verb_phrase_tense = Tense.PerfectPassive;
                        next_word();
                        match = true;
                    }
                } else {
                    // Match to mothing more, as in: I have been (often) !!
                    match = true;
                }
                
            } else {
                //Present Perfect
                if (is_word(Part_of_speech.VERB)) {
                    //Verb form of Past and Pastpart are same usually
                    if (is_tense(Tense.PASTPART)) {
                        Verb_phrase_tense = Tense.PresentPerfect;
                        next_word();
                        match = true;
                        
                    }
                }
            }
            
            //Past Perfect:
        } else if (current_word.equals("had")) {
            
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.NONE;
            
            next_word();
            
            //Optional adverbs
            while (is_adverb_except_wh()) {
                next_word();
            }
            
            if (is_spelling("been")) {
                push(Part_of_speech.VERB_PASTPART);
                Verb_phrase_tense = Tense.Perfect;
                next_word();
                
                //Optional adverbs
                while (is_adverb_except_wh()) {
                    next_word();
                }
                
                if (is_word(Part_of_speech.VERB)) {
                    //Perfect progressive
                    if (is_tense(Tense.PRESENTPART)) {
                        Verb_phrase_tense = Tense.PastPerfectProgressive;
                        
                        next_word();
                        match = true;
                        
                        //Perfect Passive
                    } else if (is_tense(Tense.PASTPART)) {
                        Verb_phrase_tense = Tense.PastPerfectPassive;
                        next_word();
                        match = true;
                    }
                } else {
                    // Match to mothing more, as in: I have been (often) !!
                    match = true;
                }
                
            } else {
                //Present Perfect
                if (is_word(Part_of_speech.VERB)) {
                    //Verb form of Past and Pastpart are same usually
                    if (is_tense(Tense.PASTPART)) {
                        Verb_phrase_tense = Tense.PastPerfect;
                        next_word();
                        match = true;
                        
                    }
                }
            }
            
            //Simply a verb word
        } else {
            
            //Verb form has Present tense
            if (is_tense(Tense.PRESENT)) {
                
                Verb_phrase_tense = Tense.Present;
                Verb_phrase_number = current_word_lexical.getnumber();
                //This is the spelling for all but third person singullar
                //unless it is already set to S3
                
                Verb_phrase_person = current_word_lexical.getperson();
                if (Verb_phrase_person == 0) {
                    System.out.println("Zero person during parse: "+current_word);
                    Verb_phrase_person = Person.S1+Person.S2+Person.P1+Person.P2+Person.P3;
                }
                
                next_word();
                match = true;
                
                //Past tense
            } else if (is_tense(Tense.PAST)) {
                Verb_phrase_tense = Tense.Past;
                Verb_phrase_number = current_word_lexical.getnumber();
                Verb_phrase_person = current_word_lexical.getperson();
                next_word();
                match = true;
                
            }
        }
        if (match) {
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Auxilliary Verb only */
    private boolean vb_2() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        
        if (current_word.equals("am")) {
            match = true;
            Verb_phrase_tense = Tense.Present;
            Verb_phrase_number = Number.S;
            Verb_phrase_person = Person.S1;
        } else if (current_word.equals("is")) {
            match = true;
            Verb_phrase_tense = Tense.Present;
            Verb_phrase_number = Number.S;
            Verb_phrase_person = Person.S3;
        } else if (current_word.equals("are")) {
            match = true;
            Verb_phrase_tense = Tense.Present;
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.S2+Person.P1+Person.P2+Person.P3;
            
        } else if (current_word.equals("was")) {
            match = true;
            Verb_phrase_tense = Tense.Past;
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.S1+Person.S3;
            
        } else if (current_word.equals("were")) {
            match = true;
            Verb_phrase_tense = Tense.Past;
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.S2+Person.P1+Person.P2+Person.P3;
            
        } else if (current_word.equals("have")) {
            match = true;
            Verb_phrase_tense = Tense.Present;
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.S1+Person.S2+Person.P1+Person.P2+Person.P3;
        } else if (current_word.equals("has")) {
            match = true;
            Verb_phrase_tense = Tense.Present;
            Verb_phrase_number = Number.S;
            Verb_phrase_person = Person.S3;
            
        } else if (current_word.equals("had")) {
            match = true;
            Verb_phrase_tense = Tense.Past;
            Verb_phrase_number = Number.S+Number.P;
            Verb_phrase_person = Person.ALL;
        }
        
        if (match) {
            next_word();
            
        } else {
            reset(save, save1, save2);
        }
        
        return match;
    }
    
    
    /** Production: Consume the current word(s) if they match an adverb phrase */
    private boolean adverbial_phrase() {
        boolean match = false;
        
        // Remember the current word, to allow reset on no match
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Adverbial);
        
        //We need either an adverb (except wh- words) or ...
        if (is_adverb_except_wh()) {
            
            match = true;
            
            //Consume the adverb
            next_word();
            
            // or we need preposition phrase(s)
        } else {
            while (prepositional_phrase()) {
                match = true;
            }
            
        }
        
        // Backtrack if not matched: we must reset the current word because
        // we may have consumed the preposition but not consumed the
        // noun phrase
        if (match) {
            push_production_end(Sentence_types.Adverbial, current_word_count - save);
        } else {
            reset(save, save1, save2);
        }
        return match;
    }
    
    
    
    
    /** Production: */
    private boolean pastparticiple() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Pastparticiple);
        if (is_word(Part_of_speech.VERB)) {
            if (is_tense(Tense.PASTPART)) {
                match = true;
                next_word();
                
                //Optional adverbial phrases, after the verb
                while (adverbial_phrase()) {
                }
                
            }
        }
        
        if (match) {
            push_production_end(Sentence_types.Pastparticiple, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    
    /** Production: Consume the current word if it is the ing form of a verb */
    private boolean verbing(boolean ap) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Verbing);
        
        if (is_word(Part_of_speech.VERB)) {
            
            if (current_word.endsWith("ing")) {
                
                match = true;
                next_word();
                
                //Optional adverbial phrases, after the verb
                if (ap)
                    while (adverbial_phrase()) {}
            }
        }
        if (match) {
            push_production_end(Sentence_types.Verbing, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
        /** Production: Consume the current word(s) if they match a
         * prepositional phrase */
    private boolean prepositional_phrase() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Prepositional);
        
        //we need a preposition followed by a noun
        if (is_word(Part_of_speech.PREPOSITION)) {
            
            //Consume the preposition
            next_word();
            
            //Now the noun part without further recursion
            //(true) here would lead to endless nexting
            if (noun_phrase(false) ) {
                match = true;
            }
        }
        
        if (match) {
            push_production_end(Sentence_types.Prepositional, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        
        return match;
    }
    
    
    /** Production: Consume the current word if it is "that" and consume clause */
    private boolean that_clause() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Mark start/end of previous/next clause
        mark_main_clause();
        
        push_production_start(Sentence_types.That_Clause);
        if (is_spelling("that")) {
            push(Part_of_speech.CONJUNCTION);
            
            next_word();
            
            //Clear and Mark the reset point in parts
            parts.clear();
            
            //Allow NP sentence type to match the clause
            if (clause(Sentence_types.Nouns_from, Sentence_types.Nouns_to)) {
                match = true;
            }
        }
        //If we took it, store its part of speech on stack of recognised
        //parts of sentence
        if (match) {
            push_production_end(Sentence_types.That_Clause, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume the current word if it is a wh-word
     * (see wh-words in the book:
     * An A-Z of English Grammar & Usage by G Leech) */
    private boolean wh() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Is the current word spelled wh-
        int part_of_speech = is_wh();
        if (part_of_speech != Part_of_speech.NONE) {
            push(part_of_speech);
            match = true;
            next_word();
            
        } else {
            reset(save, save1, save2);
        }
        return match;
    }
    
    /** Production: Consume a Wh- clause */
    private boolean wh_clause(boolean question) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        
        //Mark start/end of previous/next clause
        mark_main_clause();
        
        push_production_start(Sentence_types.Wh_Clause);
        if (wh()) {
            //Slang
            if (slang()) {}
            
            //Mark the reset point in parts
            parts.clear();
            
            if (question) {
                if (clause(Sentence_types.Verbs_from, Sentence_types.Verbs_to)) {
                    match = true;
                }
            } else {
                if (clause(0, 0)) {
                    match = true;
                }
            }
        }
        //If we took it, store its part of speech on stack of recognised
        //parts of sentence
        if (match) {
            push_production_end(Sentence_types.Wh_Clause, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume the current word if it is "that" */
    private boolean to_clause(boolean push) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.To_Clause);
        
        if (push) {
            //Mark start/end of previous/next clause
            mark_main_clause();
        }
        
        if (is_spelling("not")) {
            push(Part_of_speech.NEGATIVE);
            next_word();
        }
        
        if (is_spelling("to")) {
            push(Part_of_speech.INFINITIVE);
            
            //Clear and Mark the reset point in parts, to start before "to"
            parts.clear();
            
            //Allow the check of the following verb after "to"
            parts.push(Sentence_types.To_Clause, 0);
            
            //reset point, so that "to" stays on parts stack 
            parts.mark();
            
            next_word();
            
            if (clause(Sentence_types.Verbs_from, Sentence_types.Verbs_to)) {
                match = true;
            }
        }
        //If we took it, store its part of speech on stack of recognised
        //parts of sentence
        if (match) {
            //if (push)
            push_production_end(Sentence_types.To_Clause, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    /** Production: Consume the current word if it is "that" */
    private boolean wh_to_clause() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //Mark start/end of previous/next clause
        mark_main_clause();
        
        push_production_start(Sentence_types.Wh_To_Clause);
        if (wh()) {
            //Slang
            if (slang()) {}
            
            if (to_clause(false)) {
                match = true;
            }
        }
        //If we took it, store its part of speech on stack of recognised
        //parts of sentence
        if (match) {
            push_production_end(Sentence_types.Wh_To_Clause, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume the current word if it is "that" */
    private boolean conj_clause(boolean push) {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Conj_Clause);
        
        if (push) {
            //Mark start/end of previous/next clause
            mark_main_clause();
        }
        
        //Except wh- start clause with another conjunction
        
        if (is_wh() == Part_of_speech.NONE) {
            if (is_word(Part_of_speech.CONJUNCTION)) {
                next_word();
                //}
                //}
                //Clear and Mark this reset point
                parts.clear();
                
                if (clause(0, 0)) {
                    match = true;
                }
                
            }
        }
        //If we took it, store its part of speech on stack of recognised
        //parts of sentence
        if (match) {
            
            push_production_end(Sentence_types.Conj_Clause, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    /** Production: Consume the current word(s) if they are adjective(s) */
    private boolean adjectives() {
        boolean match = false;
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        push_production_start(Sentence_types.Adjective);
        
        if (is_word(Part_of_speech.ADJECTIVE)) {
            match = true;
            next_word();
            
            // Consume any further [and/or adjective] or [adjective]
            while (adjectives_1()) {}
            
            //Optional
            while (prepositional_phrase()) {}
        }
        
        if (match) {
            push_production_end(Sentence_types.Adjective, current_word_count - save);
        } else {
            reset(save, save1, save2);
            
        }
        
        return match;
    }
    
    /** Production: Consume the current word(s) if they are adjective(s) */
    private boolean adjectives_1() {
        boolean match = false;
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        //(fat) and ugly
        if (is_word(Part_of_speech.CONJUNCTION)) {
            next_word();
            
            if (is_word(Part_of_speech.ADJECTIVE)) {
                next_word();
                match = true;
            }
        //(fat) ugly    
        } else if (is_word(Part_of_speech.ADJECTIVE)) {
            next_word();
            match = true;
        }
        
        if (match) {
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    private boolean slang() {
        boolean match = false;
        
        int save = current_word_count;
        int save1 = stack_count;
        int save2 = main_clause_begin;
        
        if (is_spelling("the")) {
            push(Part_of_speech.SLANG);
            next_word();
            if (is_spelling("heck") || is_spelling("hell")) {
                push(Part_of_speech.SLANG);
                next_word();
                match = true;
            }
        }
        
        if (match) {
        } else {
            reset(save, save1, save2);
            
        }
        return match;
    }
    
    
    //End of the productions
    //--------------------------------------------------------------------------
    //Helper Methods
    //
    
    /** Simply compare the current sentence word
     * with the given value */
    private boolean is_spelling(String s) {
        
        boolean match = false;
        if (current_word != null && current_word.equals(s)) {
            match = true;
        }
        return match;
    }
    /** Does the current word begin with a vowel */
    private boolean is_vowel_begin() {
        boolean match = false;
        String cw = current_word.toLowerCase();
        if(cw.startsWith("a") || cw.startsWith("e") ||
        cw.startsWith("i") || cw.startsWith("o") ||
        cw.startsWith("u")) {
            match = true;
        }
        return match;
    }
    
    /** Is the current word a noun ? */
    private boolean is_noun() {
        boolean match = false;
        
        if (is_word(Part_of_speech.NOUN)) {
            match = true;
        }
        return match;
    }
    
    /** Is the current word a pronoun ? */
    private boolean is_pronoun() {
        boolean match = false;
        if (is_word(Part_of_speech.PRONOUN)) {
            match = true;
        }
        return match;
    }
    
    /** Is the current word a proper noun ? */
    private boolean is_proper_noun() {
        boolean match = false;
        if (is_word(Part_of_speech.PROPER_NOUN)) {
            match = true;
        }
        return match;
    }
    
    /** Is the current word a verb, of given tense ? */
    private boolean is_tense(int tense_in) {
        boolean match = false;
        
        //current_word_lexical was set as returned value by Lexical look-up
        Verb verb = (Verb) current_word_lexical;
        
        //What tense(s) are set by the lexicon look-up
        int tense = verb.gettense();
        
        //The tenses are the three lower bit settings in the integer "tense"
        //is it the required tense bit set ?
        if ((tense_in & tense) != 0) {
            match = true;
        }
        return match;
    }
    
    /** Is the spelling of current word  a wh- word spelling; Return part-of-speech */
    private int is_wh() {
        
        int part_of_speech = Part_of_speech.NONE;
        
        if (current_word == null) {
            part_of_speech = Part_of_speech.NONE;
        } else if (current_word.equals("who")) {
            part_of_speech = Part_of_speech.PRONOUN;
        } else if (current_word.equals("whom")) {
            part_of_speech = Part_of_speech.PRONOUN;
        } else if (current_word.equals("what")) {
            part_of_speech = Part_of_speech.PRONOUN;
        } else if (current_word.equals("which")) {
            part_of_speech = Part_of_speech.PRONOUN;
        } else if (current_word.equals("whose")) {
            part_of_speech = Part_of_speech.PRONOUN;
        } else if (current_word.equals("how")) {
            part_of_speech = Part_of_speech.ADVERB;
        } else if (current_word.equals("why")) {
            part_of_speech = Part_of_speech.ADVERB;
        } else if (current_word.equals("where")) {
            part_of_speech = Part_of_speech.ADVERB;
        } else if (current_word.equals("when")) {
            part_of_speech = Part_of_speech.ADVERB;
        } else if (current_word.equals("whether")) {
            part_of_speech = Part_of_speech.CONJUNCTION;
        }
        return part_of_speech;
    }
    //--------------------------------------------------------------------------
    //Lexical interface routine
    
    private boolean is_adverb_except_wh() {
        boolean match = false;
        if (is_word(Part_of_speech.ADVERB)) {
            
            if (is_wh() == Part_of_speech.ADVERB) {
                
            } else {
                match = true;
            }
        }
        return match;
    }
    
    /** Is the current word the part of speech p, using Lexical Analysis results */
    private boolean is_word(int p) {
        boolean match = false;
        
        //k is index in the lexical word arrays corresponding to the current word
        int k = current_word_count-1;
        if ( k < words.length && k >= 0) {
            
            switch (p) {
                case Part_of_speech.NOUN:
                    current_word_lexical = lexical.nouns[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.VERB:
                    current_word_lexical = lexical.verbs[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.ADJECTIVE:
                    current_word_lexical = lexical.adjectives[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.ADVERB:
                    current_word_lexical = lexical.adverbs[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.PREPOSITION:
                    current_word_lexical = lexical.prepositions[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.CONJUNCTION:
                    current_word_lexical = lexical.conjunctions[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.DETERMINER:
                    current_word_lexical = lexical.determiners[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.PRONOUN:
                    current_word_lexical = lexical.pronouns[k];
                    match = current_word_lexical != null;
                    break;
                case Part_of_speech.PROPER_NOUN:
                    current_word_lexical = lexical.proper_nouns[k];
                    match = current_word_lexical != null;
                    break;
                    
            }
        }
        
        //Mark that part of speech p found, or verb phrase tense
        if (match) {
            //if (false) {
            if (p == Part_of_speech.VERB) {
                Verb verb = (Verb) current_word_lexical;
                int tense = verb.gettense();
                switch (tense) {
                    case Tense.PRESENT:
                        push(Part_of_speech.VERB_PRESENT); break;
                    case Tense.PRESENTPART:
                        push(Part_of_speech.VERB_PRESENTPART); break;
                    case Tense.PAST:
                        push(Part_of_speech.VERB_PAST); break;
                    case Tense.PASTPART:
                        push(Part_of_speech.VERB_PASTPART); break;
                        
                        default:
                            push(Part_of_speech.VERB); break;
                }
                
                
                //Noun: Abstract
            } else if (p == Part_of_speech.NOUN) {
                Noun noun = (Noun) current_word_lexical;
                int number = noun.getnumber();
                if (Number.is_abstract(number)) {
                    push(Part_of_speech.ABSTRACT_NOUN);
                } else {
                    push(p);
                }
                
                //Not verb or noun
            } else {
                push(p);
            }
        }
        return match;
    }
    
    //--------------------------------------------------------------------------
    //
    // The next_word method
    
    /** Get the next word of the sentence into the current_word and
     * indicate when all words are consumed, and also clear the current_word */
    private void next_word() {
        if (current_word_count < words.length) {
            //Record the highest count reached during any parse
            //to indicate the possible error word
            if (current_word_count > Error_at) {
                Error_at = current_word_count;
            }
            
            //Not yet beyond the end of the sentence
            //all_words_consumed = false;
            
            //Set the current word
            current_word = words[current_word_count++];
            
        } else {
            //end of sentence reached
            current_word_count = words.length + 1;
            current_word = "";
        }
    }
    
    /** Backtrack, reset current_word_count, current_word, stack and clause_begin */
    private void reset(int save, int save1, int save2) {
        current_word_count = save;
        if (current_word_count-1 < words.length) {
            //!!! ResSet the current word
            current_word = words[current_word_count-1];
        } else {
            current_word = "";
        }
        
        stack_count = save1;
        main_clause_begin = save2;
    }
    
    /** Special mark of main clause for drawing and labelling a parsechart arc */
    private void mark_main_clause() {
        if (first_main_clause) {
            if ((current_word_count - main_clause_begin) > 1)
                push_production_end(Sentence_types.Main_clause,
                                    current_word_count - main_clause_begin);
        } else {
            if ((current_word_count - main_clause_begin) > 1)
                push_production_end(Sentence_types.Main_sub_clause,
                                    current_word_count - main_clause_begin);
        }
        first_main_clause = false;
        
        //Mark start of next (main) clause
        main_clause_begin = current_word_count;
    }
    
    //--------------------------------------------------------------------------
    //STACK begin
    //
    //Stacked production and word matches as:
    //  Starts and ends of productions (NP, VP, ...) and
    //  matched parts of speech (Noun,Verb,...)
    //  for the current sentence type. There is one stack, available
    //  after method "sentence" has finished its valid parse.
    //  Also stacked phrase number, person and tense at the end of NP and VP.
    //
    //
    final int STACK_CAPACITY = 100;     //Sets a fixed limit
    private int stack_count = 0;        //Current number of items on the stack
    //that is at stack[0..stack_count-1]
    private int parse_stack[] = new int[STACK_CAPACITY];
    
    //Log thing
    private int log_line_length = 0;
    private int MAX_log_line_length = 50;
    
    
   /** Push the part-of-speech p onto the stack */
    private void push(int p) {
        if (stack_count < STACK_CAPACITY && stack_count >= 0) {
            
            //For safety: check p value
            if ( p <= 0 || p >= 100) {
                System.out.println("Error: sentence part: " +p+ " at current_word_count: " +
                current_word_count);
            } else {
                parse_stack[stack_count++] = p;
                
                //Log the spelling of the current word
                try {
                    log_line_length++;
                    if (log_line_length == MAX_log_line_length) {
                        log_line_length = 0;
                        logfile.write("\n");
                    }
                    logfile.write("<="+current_word+"=>");
                } catch (IOException e) {System.out.println(e);}
            }
        }
    }
    
    /** Return the number of items on the stack */
    public int stack_size() {
        return stack_count;
    }
    /**Return the i'th item from the stack (part-of-speech, production start or
     * production end */
    public int pop(int i) {
        int p = 0;
        if (i < stack_count && i >= 0) {
            p = parse_stack[i];
        }
        return p;
    }
    
    /** Push start of sentence part p on stack */
    private void push_production_start(int p) {
        if (stack_count < STACK_CAPACITY && stack_count >= 0) {
            parse_stack[stack_count++] = 100 + p;
            
            //Log the start of a production
            try {
                if (p == Sentence_types.Sentence) {
                    logfile.write("\n");
                    log_line_length = 0;
                } else if (p >= Sentence_types.That_Clause &&
                p <= Sentence_types.Wh_Clause_question) {
                    logfile.write("\n\t");
                    log_line_length = 0;
                } else if (p == Sentence_types.Conj_Clause) {
                    logfile.write("\n\t\t");
                    log_line_length = 0;
                }
                logfile.write(Sentence_types.Part[p]+"[");
            } catch (IOException e) {System.out.println(e);}
        }
    }
    /** Push end of sentence part p on stack, with given word span.
     *  The word span means the number of sentence words spanned or covered
     *  by this production */
    private void push_production_end(int p, int word_span) {
        if (stack_count < STACK_CAPACITY && stack_count >= 0) {
            if (word_span > 0 && p > 0) {
                parse_stack[stack_count++] = 1000*word_span + 200 + p;
                
                //Log the end of a production
                try {
                    //Log Person and number and tense
                    if ( p > Sentence_types.Conj_Clause) {
                        //logfile.write("/"+Sentence_types.Part[p]+"/");
                    } else {
                        //Log part
                        logfile.write("]"+Sentence_types.Part[p]+" ");
                        if (p == Sentence_types.Sentence) {
                            logfile.write("\n<<<< Match\n\n");
                        }
                    }
                    log_line_length++;
                    if (log_line_length == MAX_log_line_length) {
                        log_line_length = 0;
                        logfile.write("\n");
                    }
                } catch (IOException e) {System.out.println(e);}
                
                //Zero word_span is allowed when the sentence begins with Wh-
                //error on negative span
            } else if (word_span < 0) {
                System.out.println("Error: part: "+p+", word_span: "+word_span);
                //System.out.println("Sentence type: "+saved_sentence_type+
                //" Word count: "+current_word_count);
            }
        }
    }
    
    //STACK end-----------------------------------------------------------------
    
    
    /** Create string of structure of the sentence parts and parts of speech in chart order,
     * as found during the parse, as store in the stack.
     * For example:
     * Sentence[NP[(Pron I)], VP[(Verb know)], WH[(Pron who)], CLAUSE[NP[(Pron you)],
     * VP[(Verb are)]]] */
    public String toString() {
        
        String chart = new String();
        
        int word_nr = 0;
        int previous_part = 0;
        int nr_of_words = 0;
        //Number of Sentence words (parts-of-speech) per line
        //Set to give a nice parse chart.
        final int WORDS_PER_LINE = 4;
        
        final int part_of_speech = 1;
        final int sentence_part_start = 2;
        final int sentence_part_end = 3;
        final int truncate_part_of_speech_at = 4;
        
        //For each stacked part (part of speech or part of sentence)
        for (int i = 0; i < stack_size(); i++) {
            int part = pop(i);
            
            //Print part of speech as (keyword spelling) such as (Noun man)
            if (part <= 0) {
                System.out.println("Error: Syntax:ToString: Part?: " + part);
                
            } else if (part < 100) {

                if (previous_part == part_of_speech || previous_part == sentence_part_end) {
                    chart += ",";
                    //New line after every so many words
                    if (++nr_of_words == WORDS_PER_LINE) {
                        nr_of_words = 0;
                        chart += "\n";
                    }
                }
                
                String s = Part_of_speech.toString(part);
                
                //(And avoid any index error, which should not happen)
                if (word_nr < words.length)
                    chart += "(" + s + " " + words[word_nr++] + ")";
                
                previous_part = part_of_speech;
                
                //Start of a sentence part such as S[, or VP[ or CLAU[
            } else if (part < 200) {
                
                if (part-100 <= Sentence_types.Sentence) {
                    
                    if (previous_part == part_of_speech || previous_part == sentence_part_end) {
                        chart += ", ";
                        //New line after every so many words
                        if (++nr_of_words == WORDS_PER_LINE) {
                            nr_of_words = 0;
                            chart += "\n";
                        }
                    }
                    
                    
                    //Print Sentence part name and print opening bracket
                    chart += Sentence_types.toString(part-100)+"[";
                    
                    previous_part = sentence_part_start;
                }
                
                //Print the sentence part closing bracket, as "]"
            } else {
                int prod = part - part/100*100;
                
                //Skip the additional tense, number, person information
                if (prod <= Sentence_types.Sentence) {
                    chart += "]";
                    
                    previous_part = sentence_part_end;
                }
            }
        }
        
        // Give back the sentence sgtructure string
        return chart;
    }
}
