/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Pronoun.java
 *
 */
/*
import Word;
import Part_of_speech;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */
public class Pronoun extends Word {

    /** Create new Pronoun with given spelling */
    public Pronoun(String spell, int num, int per) {
        super(Part_of_speech.PRONOUN, spell, num, per);
    }
}
