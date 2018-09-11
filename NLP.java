/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * NLP.java
 *
 * This is the primary NLP class, which contains the main program
 * and interacts via a GUI with the user.
 *
 * FUNCTION:
 * The NLP program loads lexicons by Lexicon.create_from_files();
 * It gets an English sentence from the graphical user interface,
 * and uses StringTokenizer to produce an array of sentence words.
 *
 * NLP calls lexical_analysis_and_report_errors(), to perform a
 * LexicalAnalysis, and showMessageDialog() to give the result.
 *
 * In syntax_analysis_and_display_chart() a parse is performed by
 * class TableDrivenTopDownParse(), and the results displayed by
 * showMessageDialog and shown in graphics form in class ParseChart().
 *
 *
 * @author  Chris Napier
 * @version 1.0
 */

//Java routines


import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


// Natural Language Parse program
class NLP extends JFrame {
    
    //English sentence of nwords to be parsed
    //and original input string
    static String[] In_sentence = null;
    static String Sentence_original = null;
    static int nwords = 0;
    
    //Lexical analysis result
    static LexicalAnalysis lexical = null;
    
    //Parse result, and nr of sentence types to with the sentence matches
    static TableDrivenTopDownParse parse = null;
    static int Matches = 0;
    
    //The graphical parse chart
    static ParseChart parse_chart = null;
    
    //Location of the external dictionary files
    static String directory = null;
    
    //The logfiles used in testing and so on
    static FileWriter logfile1 = null;
    static FileWriter logfile2 = null;
    static FileWriter logfile3 = null;
    
    //The GUI field for the given English Sentence
    private JTextField EnglishSentence;
    
    //The given first sentence word, and
    //the correct spelling which allows us to find it in a lexicon
    static String Given_first_word = null;      //I, A, He, Amsterdam, a
    static String Correct_first_word = null;    //I, a, he, Amsterdam, a
    
    //--------------------------------------------------------------------------
    /** The main program where the program begins */
    public static void main(String[] args)  //args not used
    {
        //Construct an NLP object and add a listener to it, to allow correct
        //program termination.
        NLP application = new NLP();
        application.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) {
                
                try {
                    //Close the logfiles, and exit
                    if (logfile1 != null) logfile1.close();
                    if (logfile2 != null) logfile2.close();
                    if (logfile3 != null) logfile3.close();
                    System.exit(0);
                    
                } catch (Exception e3){System.out.println(e3);};
            }
        }
        );
    }//end main method
    
    //--------------------------------------------------------------------------
    /** Construct an NLP object, to perform the GUI processing, and
     * send sentence to a handler */
    public NLP() {
        super ("Natural Language Parse by  C. Napier,  2015");
        
        try {
            //Find the correct directory for file input/output, as the
            //System defined user directory. This is the the current directory
            directory = System.getProperty("user.dir");
            
            //Create the logfiles
            logfile1 = new FileWriter(new File(directory, "NLP1.log"));
            logfile2 = new FileWriter(new File(directory, "NLP2.log"));
            logfile3 = new FileWriter(new File(directory, "NLP3.log"));
            
            //First, create lexicons by reading words from external files
            //in the user directory to lexicon objects, and log to logfile1
            Lexicon.create_from_files(directory, logfile1);
            
            //Create the wordlist all the available words, for GUI information
            String wordlist = "";
            for (int k = 0; k < Lexicon.Nlex; k++) {
                wordlist += " " + Lexicon.filenames[k].toUpperCase() + "S\n" +
                Lexicon.lex[k].wordlist() +"\n";
            }
            
            //Fonts used
            Font font1 = new Font("Ariel", Font.BOLD, 14);
            Font font2 = new Font("Ariel", Font.BOLD, 16);
            
            //The whole GUI
            Container pane = getContentPane();
            pane.setLayout(new BorderLayout());
            
            //The given English sentence: at the top of the pane
            EnglishSentence = new JTextField(100);
            EnglishSentence.setFont(font2);
            pane.add(EnglishSentence, BorderLayout.NORTH);
            
            //The handler in NLP class to respond to the enterted sentence
            TextFieldHandler handler = new TextFieldHandler();
            EnglishSentence.addActionListener(handler);
            
            
            //list of words in a pink non-edit text area at the bottom of pane
            JTextArea area = new JTextArea(50,40);
            area.setText(wordlist);
            area.setFont(font1);
            area.setBackground(Color.pink);
            area.setEditable(false);
            
            JScrollPane scrolling_wordlist = new JScrollPane(area);
            Box list = Box.createVerticalBox();
            list.add(scrolling_wordlist);
            pane.add(list);
            
            //Show GUI; and wait for an input sentence, and go to handler below
            setSize(400, 600);
            show();
            
        } catch (Exception e) {System.out.println(e);}
        
    }
    
    //--------------------------------------------------------------------------
    /** The handler to receive and parse an English sentence, from a text field */
    private class TextFieldHandler implements ActionListener {
        public void actionPerformed(ActionEvent line) {
            
            //Input: In which the user gives an English sentence, or a filename,
            //and the program responds with its analysis
            try {
                //Get a line from the GUI, holding a sentence 
                // and give it to a new in object.
                Input in = new Input(line.getActionCommand());
                
                //For each given sentence
                while (in.next()) {
                    
                    //Get the sentence
                    Sentence_original = in.getsentence();
                    
                    //Split sentence into its words, allowing many punctuation marks
                    StringTokenizer string = new StringTokenizer(Sentence_original,
                    " \\.,;:!?\"'(){}[]/<>\n\t\r\f");
                    
                    //Get the number of words in the sentence, excluding punctuation
                    nwords = string.countTokens();
                    if (nwords > 0) {
                        //Create and set the sentence words
                        //skipping puncuation marks and end of lines
                        //sentence = new String[nwords];
                        In_sentence = new String[nwords];
                        
                        for (int n = 0; n < nwords; n++) {
                            String w = string.nextToken();
                            In_sentence[n] = w;
                            
                            //Allow and Convert some abbreviations
                            if (w.equals("d"))
                                w = "had";
                            else if (w.equals("m"))
                                w = "am";
                            else if (w.equals("s"))
                                w = "is";
                            else if (w.equals("re"))
                                w = "are";
                            else if (w.equals("t"))
                                w = "not";
                            else if (w.equals("ve"))
                                w = "have";
                            
                            In_sentence[n] = w;
                        }
                    }
                    
                    
                    //If sentence words were found in the given input
                    if (nwords > 0) {
                        
                        //Save given first word
                        Given_first_word = In_sentence[0];
                        Correct_first_word = In_sentence[0]; //Safety
                        
                        //Perform lexical analysis, and report errors
                        if (lexical_analysis_and_report_errors()) {
                            
                            //Parse sentence, if there are no lexical errors
                            //Initialize the parse
                            parse = new TableDrivenTopDownParse(In_sentence, nwords, lexical, logfile3);
                            
                            //The nr of sentences types which matched the given sentence
                            Matches = 0;
                            for (int st = 0; st < Sentence_types.Definitions.length; st++) {
                                if (syntax_analysis_and_display_chart(st)) {
                                    //Count the number of sentence types which match the sentence
                                    Matches++;
                                }
                            }
                            
                            if (Matches == 0) {
                                //If no matches were found: Pop-up an error message
                                //indicating position of the first word which did not
                                //match a sentence pattern
                                String message = Sentence_original + "\n" +
                                "Error at word: " + (parse.Error_at+1) + " or earlier.";
                                
                                JOptionPane.showMessageDialog(null, message,
                                "Syntax Error", JOptionPane.ERROR_MESSAGE);
                            }//matches==0
                        }//lexical analysis
                    }//when input nwords>0
                }
                
            } catch (Exception e2) {System.out.println(e2);}
        }
    }
    
    
    /* Internal class to return the single sentence line, on first call */
    private class Input {
        private String sentence;
        private boolean first;
        
        /** Receive a line from the user GUI */
        Input (String input_line) {
            sentence = input_line;
            first = true;
        }
        
        /** Is there a next sentence */
        public boolean next() {
            return first;
        }
        
        /** Return the next/first sentence only */
        public String getsentence() {
            first = false;
            return sentence;
        }
        
    }
    
    //--------------------------------------------------------------------------
    /** Lexically analyse the sentence of nwords and report errors */
    private static boolean lexical_analysis_and_report_errors()
    { //throws CloneNotSupportedException {
        
        boolean correct = false;
        String errors[] = {""};
        String message = null;
        
        
        //If correctly uppercase first letter
        if (is_correct_first_word()) {
            
            //For now, set to allow recognition in the lexicons
            In_sentence[0] = Correct_first_word;
            
            //Perform lexical analysis of the sentence (and optionally log results)
            lexical = new LexicalAnalysis(In_sentence, nwords, logfile2);
            lexical.look_up();
            //Report any errors to user
            if (!lexical.check(errors)) {
                message = Sentence_original + "\n" + "Spelling?: " + errors[0];
                JOptionPane.showMessageDialog(null, message,
                "Lexical error", JOptionPane.ERROR_MESSAGE);
                
            } else {
                //No lexical errors
                correct = true;
            }
            
        } else {
            message = Sentence_original + "\n" + "Must start with an uppercase letter!";
            JOptionPane.showMessageDialog(null, message,
            "Lexical error", JOptionPane.ERROR_MESSAGE);
        }
        
        return correct;
    }//End of lexical_analysis_and_report_errors()
    
    
    /** Process the first word first character as upper/lowercase */
    private static boolean is_correct_first_word() {
        boolean upper = false;
        
        //Set upper to indicate if first letter of first word is uppercase or not
        String x = null;
        if (Given_first_word.length() == 1) {
            x = Given_first_word;
        } else {
            x = Given_first_word.substring(0,1);
        }
        upper = x.toUpperCase().equals(x);
        
        //Only proceed if the first word starts with an uppercase letter
        if (upper) {
            //Leave "I" as it is
            if (Given_first_word.equals("I")) {
                Correct_first_word = "I";
                
                //Leave Proper nouns like "Amsterdam" and adjectives like "English"
            } else if (Lexicon.findany(Given_first_word) != -1) {
                Correct_first_word = Given_first_word;
                
                //All other first words must be reduced to lowercase start letter
            } else {
                // A -> a, or even a -> a, i -> i
                if (Given_first_word.length() == 1) {
                    Correct_first_word = Given_first_word.toLowerCase();
                } else {
                    //Lowercase on first letter only
                    String y = Given_first_word.substring(0, 1).toLowerCase();
                    Correct_first_word = y + Given_first_word.substring(1);
                }
            }
        }
        
        return upper;
    }
    
    
    //--------------------------------------------------------------------------
    /** Syntactically analyse the sentence as sentence type st, and display result */
    private static boolean syntax_analysis_and_display_chart(int st) {
        
        boolean correct = false;
        
        // **** Perform the Parse of the sentence using given sentence type st
        // (parameter true, means that we are parsing a sentence, not a clause)
        correct = parse.sentence(st, true);
        
        // We have reognised the sentence structure as sentence type st !!!
        if (correct) {
            //Set structs string: The recognised symbolic sentence parts
            
            String structs = Sentence_types.getstructure(st);
            
            //Create parse string such as: Sentence[N[(Noun I)], V[(Verb am)] ...
            //from the parse results
            String chart = parse.toString();
            
            System.out.println(" Type " + st + ": " + chart);
            
            //Show the parse chart strings on a pop-up window
            String message = Sentence_original + "\n" +
            "Structure: " + structs + "\n" + "\n" + chart;
            //To achieve this the program requests the user.dir property from the Java system.
            
            String title = "Sentence Structure " + (Matches+1);
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
            
            //Get the parsed sentence to a local array, to avoid problems
            //with concurrency in paint routine
            int n = parse.stack_size();
            int[] stack = new int[n];
            for (int j = 0; j < n; j++) {
                stack[j] = parse.pop(j);
            }
            
            //Reset to actual given first word, for correct chart display
            In_sentence[0] = Given_first_word;
            
            //Graphically Display the (Matches+1)'th parse chart
            parse_chart = new ParseChart(Matches, Sentence_original, structs,
            In_sentence, nwords, stack, chart);
        }
        return correct;
    }//End of syntax_analysis_and_display_chart()
    
}// end class NLP

