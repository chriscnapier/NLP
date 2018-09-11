/**
 * Parts.java
 * Sentence parts comparison for person matching and tense matching
 *
 * @author  Chris Napier
 * @version 1.0
 */
/** Compare person, number and tense of the sentence parts */
public class Parts extends Object {
    
    // Nr of saved parts, and their stored attributes
    private int nparts = 0;
    private int[] sentence_part = null;
    private int[] person = null;
    private int[] auxilliary = null;

    // Marked parts index/count. Capacity set at construction
    private int mark_nparts = 0;
    private int capacity = 0;
    
    
    /** Creates new Class of given capacity */
    public Parts(int capacity_in) {
        if (capacity_in <= 0)
            capacity = 100;
        else
            capacity = capacity_in;
        
        nparts = 0;
        
        sentence_part = new int[capacity];
        person = new int[capacity];
        
    }
    
    /** Receive and check a sentence part such as NP or VP */
    public boolean push(int sentence_part_in, int person_in) {
        boolean ok = false;
        
        //If not exceeding stack size
        if (nparts < person.length) {
           
            sentence_part[nparts] = sentence_part_in;
            person[nparts] = person_in;
            nparts++;
            
            //Check NP and VP, or check To infinitive tense
            if (nparts == 2) {
                int i = nparts-2;
                if (sentence_part[i] == Sentence_types.N &&
                sentence_part[i+1] == Sentence_types.V)
                    ok = check_person();
                
                else if (sentence_part[i] == Sentence_types.V &&
                sentence_part[i+1] == Sentence_types.N)
                    ok = true; //check_person();
                    
                else if (sentence_part[i] == Sentence_types.To_Clause &&
                sentence_part[i+1] == Sentence_types.V)
                    ok = check_infinitive();
                
            } else {
                ok = true;
            }
        }
        return ok;
    }
    
    /** Nr of stored parts */
    public int length() { return nparts; }
    
    /** Check that the person corresponds in the last to stored parts */
    private boolean check_person() {
        boolean correct = true;
        
        //For the first 2 stored N or V sentence parts, only
        int i = nparts-2;
        // If the sentence parts are either:
        // N directly followed by V or V directly followed by N
        
        //If persons are defined but "differ" in bits, then its incorrect
        if (person[i] != 0 && person[i+1] != 0) {
            if ((person[i] & person[i+1]) == 0) {
                correct = false;
            }
        }
        return correct;
    }
    
    /** Check that the tense is infinitive after a To */
    private boolean check_infinitive() {
        boolean correct = false;
        
        //For the "to" and verb
        int i = nparts-2;
        
        //The verb must be infinitive (S1 is used for Lexical base form)
        if ((person[i+1] & Person.INF) != 0)
            correct = true;
        
        //System.out.println("Parts To: i+1 " +(i+1)+ " person: " + person[i+1]);
        
        return correct;
    }
    
    /** Mark the current nr of stored parts, for a later reset */
    public void mark() {
        mark_nparts = nparts;
    }
    
    /** Remove all saved parts, and clear the mark */
    public void clear() {
        nparts = 0;
        mark_nparts = 0;
    }
    
    /** Reset saved parts index/count to the previously Marked position */
    public void reset() {
        nparts = mark_nparts;
    }
    
}
