/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Lexicon.java
 *
 * FUNCTION:
 * The program creates a number of lexicons (dictionaries) from words read from external files.
 * It shall do this on start-up. There shall be a separate lexicon for each part-of-speech
 * namely, noun, verb, adjective, adverb, preposition, conjunction, determiner, pronoun, and
 * proper noun. The reason for a number of lexicons is that the same word spelling can be
 * different parts-of-speech, such as “book” which is a noun and a verb. A single lexicon
 * would have to have multiple entries, which adds some complication to the lexical look-up
 * and analysis.
 *
 *
 * @author  Chris Napier
 * @version 1.0
 */


import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Properties; //For definition of current directory "user.dir"

import java.io.*;

import javax.swing.*;

/*import Noun;
import Verb;
import Adjective;
import Adverb;
import Preposition;
import Conjunction;
import Determiner;
import Pronoun;
import Proper_noun;
import Tense;
import Person;
*/

public class Lexicon extends Object {
    
    //Java HashMap is used for a fast look lexicon
    public HashMap lexicon;
    
    //The filenames extension
    private static String extension = ".lex";
    
    //Indexes to the Nlex static lexicons located here
    public static final int Nlex = 9;
    public static final int noun=0;
    public static final int verb=1;
    public static final int adjective=2;
    public static final int adverb=3;
    public static final int preposition=4;
    public static final int conjunction=5;
    public static final int determiner=6;
    public static final int pronoun=7;
    public static final int proper_noun=8;
    
    //Filenames of external lexicon files
    public static final String[] filenames = {
        "noun", "verb", "adjective", "adverb", "preposition",
        "conjunction", "determiner", "pronoun", "proper_noun"
    };
    
    // Actual static lexicons (dictionaries), one for each part of speech
    public static Lexicon[] lex = new Lexicon[Nlex];
    
    /*noun, verb, adjective, adverb, preposition, conjunction,
    determiner, pronoun, proper_noun;
     */
    
    //Construct one lexicon as a HashMap to initially hold n words
    public Lexicon(int n) {
        lexicon = new HashMap(n);
    }
    
    //The wordlist and their number in the returned list
    private int nwordlist = 0;
    private String the_wordlist = "";
    
    /** Get the given word in the lexicon into a cloned Word object.
     * After a Word is received from a lexicon look-up,
     * no reference into the lexicon is allowed, since we actually change the
     * received word spelling and number. Thus this change happens only in
     * the cloned object and NOT in the lexicon. */
    
    public Word get(String spelling) {
        Word copy = null;
        if (spelling != null) {
            if (lexicon.containsKey(spelling)) {
                Word reference_into_lexicon = (Word) lexicon.get(spelling);
                copy = (Word) reference_into_lexicon.clone();
            }
        }
        return copy;
    }
    
    /** Find spelling  in any lexicon; return lex number or -1 */
    public static int findany(String spelling) {
        int found = -1;
        if (spelling != null) {
            for (int k = 0; k < Nlex; k++) {
                if (lex[k].lexicon.containsKey(spelling)) {
                    found = k;
                    break;
                }
            }
        }
        return found;
    }
    
    /** Store one word in lexicon, given its spelling and a Word object */
    public void put(String str, Word word) {
        lexicon.put(str, word);
    }
    
    
    /** Create all lexicons and store words in them, read from external text files */
    public static void create_from_files(String directory, FileWriter logfile)
    throws FileNotFoundException, IOException {
        
        //Create empty lexicons, located (static) in this class
        for (int k = 0; k < Nlex; k++)
            lex[k] = new Lexicon(200);
        
        System.out.println("Reading Lexicons from directory: " + directory);
        
        //For each lexicon: read words
        int words_read = 0;
        boolean errors = false;
        for (int k = 0; k < filenames.length; k++) {
            
            //Set the File f, and see if we can read it
            File f = new File(directory, filenames[k] + extension);
            if (f.canRead()) {
                //Open k'th input file
                FileReader fr = new FileReader(f);
                
                //from each line of the file, read a word followed by attribute(s)
                //number, tense and person, until end of file
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {
                    String line = br.readLine();
                    
                    //Split the read line into its items, separated by blank and tab
                    StringTokenizer tokens = new StringTokenizer(line, " \t");
                    
                    int number = Number.S;      //Default to singular
                    int person = Person.NONE;   //No person
                    
                    //Get the number of items on the line, and their spelling values
                    int nitems = tokens.countTokens();
                    if (nitems >= 1) {
                        String spell_1 = null;
                        String spell_2 = null;
                        String spell_3 = null;
                        
                        //Word spelling
                        spell_1 = tokens.nextToken();
                        
                        // Get the noun number, pronoun number and person, or
                        // the verb spellings on the one line
                        if (nitems >= 2)
                            spell_2 = tokens.nextToken();
                        if (nitems >= 3)
                            spell_3 = tokens.nextToken();
                        
                        words_read++;   //count the number of words read in
                        
                        //place word(s) into current lexicon
                        switch (k) {
                            case 0: //Noun + number
                                // Get the noun "number", including abstract
                                number = Number.NONE;
                                if (spell_2 != null)
                                    number |= Number.fromString(spell_2);
                                if (spell_3 != null)
                                    number |= Number.fromString(spell_3);
                                
                                //"Add" singular, if neither S or P or SP given
                                if (Number.is_singular(number)) {
                                } else if(Number.is_plural(number)) {
                                } else {
                                    number |= Number.S;
                                }
                                
                                lex[noun].put(spell_1, new Noun(spell_1, number));
                                break;
                                
                            case 1: //Verb spellings for present, past, pastpart
                                put_verb_spellings(spell_1, spell_2, spell_3);
                                
                                //Count the second and third verb words, if any
                                if (nitems > 1)
                                    words_read += nitems-1;
                                
                                break;
                                
                                //Simple spelling stored
                            case 2: lex[adjective].put(spell_1, new Adjective(spell_1)); break;
                            case 3: lex[adverb].put(spell_1, new Adverb(spell_1)); break;
                            case 4: lex[preposition].put(spell_1, new Preposition(spell_1)); break;
                            case 5: lex[conjunction].put(spell_1, new Conjunction(spell_1)); break;
                            
                            case 6: //Determiner + number
                                if (spell_2 != null)
                                    number = Number.fromString(spell_2);
                                
                                lex[determiner].put(spell_1, new Determiner(spell_1, number));
                                break;
                                
                            case 7: //Pronoun + number + person
                                if (spell_2 != null)
                                    number = Number.fromString(spell_2);
                                if (spell_3 != null)
                                    person = Person.fromString(number, spell_3);
                                
                                lex[pronoun].put(spell_1, new Pronoun(spell_1, number, person));
                                break;
                                
                            case 8: //Proper noun spelling
                                lex[proper_noun].put(spell_1, new Proper_noun(spell_1));
                                break;
                        }
                    }
                }
                
                //Close the finished input lexicon file
                fr.close();
            } else {
                System.out.println("Can't read Lexicon file: " + f.getPath());
                errors = true;
            }
        } //Next input lexicon file
        
        //Stop right here, or indicate the number of words which were read
        if (errors)
            System.exit(1);
        else
            System.out.println("Stored " + words_read + " words");
        
        //Log the contents if requested to do so
        if (logfile != null)
            log(logfile);
    }
    
    
    /** Put the given spellings into the verb lexicon, for present, past and pastpart */
    private static void put_verb_spellings(
    String present_spelling, String past_spelling, String pastpart_spelling) {
        
        //We assume that if the past is not given, then the pastpart is also not given
        //if past and pastpart not given, form them as -d or -ed
        if (past_spelling == null) {
            lex[verb].put(present_spelling, new Verb(present_spelling, Tense.PRESENT, Person.INF));
            past_spelling = form_past(present_spelling);
            lex[verb].put(past_spelling, new Verb(past_spelling, Tense.PAST+Tense.PASTPART, Person.ALL));
            
            //if past given but not pastpart, use past as pastpart !
        } else if (pastpart_spelling == null) {
            lex[verb].put(present_spelling, new Verb(present_spelling, Tense.PRESENT, Person.INF));
            lex[verb].put(past_spelling, new Verb(past_spelling, Tense.PAST+Tense.PASTPART, Person.ALL));
            
        } else {
            //Three given verb forms
            //But we combine the same-spelling verb forms since
            //lexicon has no duplicate entries
            
            //Present spelling same as Past
            if (present_spelling.equals(past_spelling)) {
                //All three same: read read read
                if (present_spelling.equals(pastpart_spelling)) {
                    //store the one word as all tenses
                    lex[verb].put(present_spelling, new Verb(present_spelling,
                    Tense.PRESENT+Tense.PAST+Tense.PASTPART, Person.INF));
                    
                    //Pastpart different spelling
                } else {
                    lex[verb].put(present_spelling,
                    new Verb(present_spelling, Tense.PRESENT+Tense.PAST, Person.INF));
                    lex[verb].put(pastpart_spelling,
                    new Verb(pastpart_spelling, Tense.PASTPART,Person.ALL));
                }
                
                //Present same as pastparticiple
            } else if (present_spelling.equals(pastpart_spelling)) {
                lex[verb].put(present_spelling,
                new Verb(present_spelling, Tense.PRESENT+Tense.PASTPART, Person.INF));
                lex[verb].put(past_spelling, new Verb(past_spelling, Tense.PAST, Person.ALL));
                
                //Past and pastpart same
            } else if (past_spelling.equals(pastpart_spelling)) {
                lex[verb].put(present_spelling, new Verb(present_spelling, Tense.PRESENT,Person.ALL));
                lex[verb].put(past_spelling, new Verb(past_spelling, Tense.PAST+Tense.PASTPART,Person.ALL));
                
                //All different spellings
            } else {
                lex[verb].put(present_spelling, new Verb(present_spelling, Tense.PRESENT, Person.INF));
                lex[verb].put(past_spelling, new Verb(past_spelling, Tense.PAST, Person.ALL));
                lex[verb].put(pastpart_spelling, new Verb(pastpart_spelling, Tense.PASTPART, Person.ALL));
            }
        }
        
    }
    
    /** Form the past and pastpart from present for regular verbs */
    private static String form_past(String str1) {
        String str2 = null;
        
        if (str1 != null) {
            
            int len = str1.length();
            
            if (str1.endsWith("e"))
                str2 = str1 + "d";
            
            //ly -> lied: reply -> replied,
            //ry -> ried: try -> tried,
            //      but play -> played
            else if (str1.endsWith("ly"))
                str2 = str1.substring(0,len-1) + "ied";
            
            else if (str1.endsWith("ry"))
                str2 = str1.substring(0,len-1) + "ied";
            
            else
                str2 = str1 + "ed";
        }
        
        return str2;
    }
    
    
    /** Print the key and value entries in all the lexicons */
    public static void log(FileWriter file) {
        int nr = 0;
        
        try {
            for (int k = 0; k < Nlex; k++) {
                file.write("\n\n"+ filenames[k].toUpperCase() +"S\n-----");
                nr += lex[k].print(file);
            }
            
            file.write("\n\nPrinted "+ nr +" words");
            
        } catch (IOException e) {System.out.println(e);}
    }
    
    /* Print the key and value entries in the lexicon, sorted; return nr printed */
    public int print(FileWriter file) {
        int n = 0;
        String str = null;
        try {
            
            //Get the set of the currently stored keys, and sort them, an use an iterator
            Set lexkeys_set = lexicon.keySet();
            TreeSet lexkeys_sorted = new TreeSet(lexkeys_set);
            Iterator lexkeys = lexkeys_sorted.iterator();
            
            //Iterate through the sorted keys, and print them and the value in lexicon
            while (lexkeys.hasNext()) {
                String spelling = (String) lexkeys.next();
                Word word = (Word) lexicon.get(spelling);
                String value = word.toString();
                file.write("\nKey (" + spelling + ")  " + value);
                n++;
            }
            
        } catch (IOException e) {System.out.println(e);}
        
        //Return number of printed words
        return n;
        
    }
    
    /** Display a wordlist from one lexicon */
    /*public void display(String name) {
        JTextArea a = new JTextArea(20,30);
        JScrollPane sp = new JScrollPane(a);
        a.setText(wordlist());
     
     
        JOptionPane.showMessageDialog(null, sp, "Lexicon " + name,
        JOptionPane.INFORMATION_MESSAGE);
    }*/
    
    /* Return a list of the value entries in a lexicon, sorted; Set number of */
    public String wordlist() {
        nwordlist = 0;
        the_wordlist = "";
        
        //Get the set of the currently stored keys, and sort them, an use an iterator
        Set lexkeys_set = lexicon.keySet();
        TreeSet lexkeys_sorted = new TreeSet(lexkeys_set);
        Iterator lexkeys = lexkeys_sorted.iterator();
        
        //Iterate through the sorted keys, and add to wordlist and count
        while (lexkeys.hasNext()) {
            String spelling = (String) lexkeys.next();
            Word word = (Word) lexicon.get(spelling);
            String value = word.toString(); //acutually calls verb.toString for example
            the_wordlist += " " + value + "\n";
            nwordlist ++;
        }
        
        //Return list of words
        return the_wordlist;
    }
    
    /* Return the number of words in the wordlist - NOT used here */
    public int getnwordlist() {
        return nwordlist;
    }
    
} //end Lexicon class
