/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Sentence_types.java
 *
 * FUNCTION:
 *
 * To define in a table the sentence types to which the parse shall try to
 * match the given English sentence.
 * To enumerate the Sentence parts, recognised by the program,
 * To give a text representation for each sentence part.
 *
 * Also enumerate tense, number and person
 *
 *
 */


/*import Tense;
import Number;
import Person;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */
public class Sentence_types extends Object {
    
    //Sentence parts
    public static final int
    NONE = 0,
    N=1, V=2, VN=3, Adjective=4, Adverbial=5, Prepositional=6,
    That_Clause=7, Wh_To_Clause=8, Wh_Clause=9, To_Clause=10,
    Wh_Clause_question = 11, Verb=12, Verbing=13, Pastparticiple=14,
    
    Sentence=15, Main_clause=16, Main_sub_clause=17, Conj_Clause=18,
    
    //Verb Phrase tense
    Present=19, PresentPerfect=20, Passive=21, Progressive=22,
    PerfectPassive=23, PerfectProgressive=24, Perfect=25,
    
    Past=26, PastPerfect =27, PastPassive =28, PastProgressive =29,
    PastPerfectPassive =30, PastPerfectProgressive =31,
    
    //Number
    Singular =32, Plural =33, SingularPlural =34,
    
    //Person
    NoPerson=35, S1=36, S2=37, S3=38, P1=39, P2=40, P3=41, INF=42,
    MorePerson=43;
    
    
    //Senence Parts: Textual representation in order of the above
    public static final String[] Part = {
        "",
        "NP", "VP", "VNP", "AdjP", "AdvP", "PP",
        "That-Clause", "Wh-To-Clause", "Wh-Clause", "Inf-Clause",
        "Wh-clause?", "Verb", "PresPart", "PastPart",
        
        "Sentence", "Main-Clause", "Main-Clause", "Subord-Clause",
        
        //Verb Phrase tense
        "Pres", "PresPerf", "Passiv", "Progres",
        "PerfPassiv", "PerfProgres", "Perf",
        
        "Past", "PastPerf", "PastPassiv", "PastProgres",
        "PastPerfPass", "PastPerfProg",
        
        //Noun Phrase numbers
        "S", "P", "SP",
        
        //Persons
        "-", "1S", "2S", "3S", "1P", "2P", "3P", "INF", "+"};
        
        
        public static int toPart(int phrasetense) {
            int part = NONE;
            switch(phrasetense) {
                case Tense.Present: part = Present; break;
                case Tense.PresentPerfect: part = PresentPerfect; break;
                case Tense.Passive: part = Passive; break;
                case Tense.Progressive: part = Progressive; break;
                case Tense.PerfectPassive: part = PerfectPassive; break;
                case Tense.PerfectProgressive: part = PerfectProgressive; break;
                case Tense.Perfect: part = Perfect; break;
                
                case Tense.Past: part = Past; break;
                case Tense.PastPerfect: part = PastPerfect; break;
                case Tense.PastPassive: part = PastPassive; break;
                case Tense.PastProgressive: part = PastProgressive; break;
                case Tense.PastPerfectPassive: part = PastPerfectPassive; break;
                case Tense.PastPerfectProgressive: part = PastPerfectProgressive; break;
                
                
                default: part = NONE; break;
            }
            return part;
        }
        
        
        public static int toNumber(int num) {
            int number = NONE;
            switch (num) {
                
                case Number.S: number = Singular; break;
                case Number.P: number = Plural; break;
                case Number.SP: number = SingularPlural; break;
                
                default: number = NONE; break;
            }
            return number;
        }
        
        public static int toPerson(int per) {
            int person = NONE;
            switch (per) {
                case Person.NONE: person = NoPerson; break;
                case Person.S1: person = S1; break;
                case Person.S2: person = S2; break;
                case Person.S3: person = S3; break;
                
                case Person.P1: person = P1; break;
                case Person.P2: person = P2; break;
                case Person.P3: person = P3; break;
                case Person.INF: person = INF; break;
                
                default: person = MorePerson; break;
            }
            
            
            
            return person;
        }
        
        /** Return structure of requested sentence type, as a string */
        public static String getstructure(int st) {
            String structs = "";
            for (int k = 0; k < Definitions[st].length; k++) {
                String part = Sentence_types.Part[Sentence_types.Definitions[st][k]];
                structs += " " + part;
            }
            return structs;
        }
        
        /** Return sentence part k, as a string */
        public static String toString(int k) {
            String p;
            if (k >= 0 && k < Part.length) {
                p = Part[k];
            } else {
                p = "";
            }
            return p;
        }
        
        //These sentence structure definitons drive the parse:
        //They use the above sentence parts,
        //which are used to match words in the given English sentence.
        //The sentence types are used one by one independently to match the
        // sentence and also to match nested clauses within the sentence.
        
        public static final int Verbs_from = 0;
        public static final int Verbs_to = 24;
        
        public static final int Nouns_from = 24;
        public static final int Nouns_to = 46;
        
        public static final int Definitions[][] = {
            
            { V, Adjective },       //0
            { V, Adverbial },
            { VN, Adverbial },
            { V, That_Clause },
            { V, Wh_Clause },
            { V, Wh_To_Clause },
            { V, To_Clause},
            { V, Verbing },
            { V, Pastparticiple },
            { V, N, Adjective },
            { V, N, Adverbial },    //10
            { V, N, That_Clause },
            { V, N, Wh_Clause },
            { V, N, To_Clause },
            { V, N, Verbing },
            { V, N, Verbing, N }, //15
            { V, N, Verbing, To_Clause },
            { V, N, Pastparticiple },
            { V, N, N },
            { V, N, N, Wh_Clause },
            { V, N, N, To_Clause },  //20
            { V, N},
            { V },
            { VN },                 //23
            
            { N, V, Adjective },    //24
            { N, V, Adverbial},     //25
            { N, V, That_Clause },  //26
            { N, V, Wh_Clause },    //27
            { N, V, To_Clause },    //28
            { N, V, Verbing },      //29
            { N, V, Verbing, N },   //30
            { N, V, Verbing, To_Clause },
            { N, V, Pastparticiple },
            { N, V, N, N },
            { N, V, N, Adjective },
            { N, V, N, Adverbial }, //I shot (an elephant in pyjamas): N V N
            { N, VN, Adverbial },   //I (shot an elephant) in pyjamas: N VN Adverbial
            { N, V, N, That_Clause },
            { N, V, N, Wh_To_Clause },
            { N, V, N, Wh_Clause },
            { N, V, N, To_Clause }, //40
            { N, V, N, Verbing },   //What book are you reading
            { N, V, N, Pastparticiple },    //Is this child loved ?
            { N, V, N},
            { N, V },
            { N, Wh_Clause},        //45
            { Wh_Clause},
            { To_Clause}            //47
            //{ Wh_Clause_question}//,              //What is your name. Who are you.
            
        };
}
