/**
 * Natural Language Parse program by Napier Software in 2015
 * 
 * Word.java
 *
 * Base class representing Parts-of-speech objects, as spelling and attributes
 */

/*import NLP.Part_of_speech;
import NLP.Number;
import NLP.Person;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */

/** Base class representing Parts-of-speech objects as spelling and attributes */
public class Word extends Object implements Cloneable {
    
    private int part_of_speech; //Part of Speech (See above interfaces)
    private String spelling;    //Spelling of the word
    private int number; // singular or plural or both
    private int person;
 
    /** To construct an empty word */
    Word() {
        part_of_speech = Part_of_speech.NONE;
        spelling = "";
        number = Number.NONE;
        person = Person.NONE;
    }
    /** To construct a real word */
    Word(int part_of_speech_in, String spelling_in, int number_in, int person_in) {
        part_of_speech = part_of_speech_in;
        spelling = spelling_in;
        number =  number_in;
        person = person_in;
    } 
    
    /** To make a full copy of a Word object.
        We must return Object, since this clone method extends Object's clone method.
        This is used after a Word is received from a lexicon look-up, so that
        no reference into the lexicon is allowed, since we actually change the
        received word spelling and number. Thus this change happens only in 
        the cloned object and NOT in the lexicon. */
    public Object clone() {
        Object w = null;
        try {
            //Use the Object class clone method, which is sufficient
            w = super.clone();
            
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone error: " + e);
            System.exit(1);
        }
        return w;
    }
    
    //get and put of all Word attribute fields 
    public int getpart_of_speech() { return part_of_speech; }
    public String getspelling() { return spelling; }
    public int getnumber() { return number; }
    public int getperson() { return person; }
    
    public void putpart_of_speech(int pos) { part_of_speech = pos; }
    public void putspelling(String spell) { spelling = spell; }
    public void putnumber(int num) { number = num; }
    public void putperson(int per) { person = per; }
    
    /** Return the attributes of a Word object in text form */
    public String toString() {
        String str = spelling + 
        " [" + Part_of_speech.toString(part_of_speech) + "]";
        
        if (number != Number.NONE)
            str += " Number: " + Number.toString(number);
        if (person != Person.NONE)
            str += " Person: " + Person.toString(person);
        
        return str;
    }
}
