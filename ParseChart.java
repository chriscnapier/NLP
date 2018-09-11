/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * ParseChart.java
 *
 * FUNCTION:
 * Produce Graphical Output
 * A chart shall be used to present the graphical view of the sentence structure.
 * The words of the sentence shall appear at the bottom of the chart, with
 * buttons between, and with arcs above.  Each arc shall be labelled with the
 * part-of-speech, as text such as Verb, Noun, Prep.
 
 * There shall be larger arcs to show each sentence part, whereby the arc spans
 * the words, which form the part. The arc shall be labelled with the text given
 * in the sentence_types class, such as NP, VP, Clause. There will be an overall
 * arc labelled Sentence.
 *
 * When more than one arc spans the same word or words the labelling text shall
 * be combined with the separating symbol, ->.   For example: the single
 * word sentence Go. Shall have the label: Sentence-> VP and Verb over a single
 * arc.
 *
 * The parts-of-speech labels shall not be combined with any other labels.
 *
 * The text-based output such as Sentence[NP[(Verb Go)]] shall also be shown
 * on the graphical chart.
 *
 */


import java.io.*;
import java.awt.Graphics;
import java.awt.Font;
import java.util.*;

import javax.swing.JFrame;

/*import Sentence_types;
import Part_of_speech;*/

/**
 *
 * @author  Chris Napier
 * @version 1.0
 */

public class ParseChart extends JFrame {
    
    private String sentence_original;
    private String structs;
    private String sentence[] = null;
    private int nwords = 0;
    private int[] sentence_parse = null;
    private int n_sentence_parse = 0;
    private String structure_chart = "";
    
    //Some pixel sizes on the graph:
    private int xscale = 100;    //Nr of pixels per word across page
    private int x_margin = 30;    //Left/right margin in pixels
    private int graph_height = 710;   //Nr of pixels vertically on whole screen
    private int oval_size = 8;   //diameter of dots between words, in pixels
    
    private int scale = 1;
    
    //The Constructor is finished
    boolean constructed = false;
    
    
    /** Creates new ParseChart */
    public ParseChart(int matches, String sentence_original_in, String structs_in,
    String[] sentence_in, int nw, int[] sentence_parse_in, String sc) {
        
        //Set the header text and initialize the graphics display
        super("Natural Language Parse Chart " + (matches+1));
        
        //Bring in all the given data
        sentence_original = sentence_original_in;
        structs = structs_in;
        nwords = nw;
        
        //Set the height of the chart
        if (nwords >= 4 && nwords <= 7)
            graph_height =  100*nwords +10;
        else if (nwords < 4)
            graph_height = 400;
        else
            graph_height = 710;
        
        //Adjust the x margin
        if (nwords == 1)
            x_margin = 60;
        else
            x_margin = 10;
        
        
        sentence = new String[nwords];
        for (int i = 0; i < nw; i++) {
            sentence[i] = sentence_in[i];
        }
        
        n_sentence_parse = sentence_parse_in.length;
        sentence_parse = new int[n_sentence_parse];
        
        //NOT sentence_parse = sentence_parse_in;
        //since the external object may be modified
        
        for (int i = 0; i < n_sentence_parse; i++) {
            sentence_parse[i] = sentence_parse_in[i];
        }
        
        
        structure_chart = sc;
        
        //Needed by paint, since super above calls paint too soon !!
        constructed = true;
        
        //Size of graphics display and visibility
        //Set NN, the as-if-number-of-words 
        int NN = nwords;
        if (NN > 15) 
            NN = 15;
        else if (NN < 3) 
            NN = 3; 
        
        setSize(150+150*NN+2*x_margin, graph_height+20);
        setVisible(true);
    }
    
    
    /** Function used in arc construction - a sine curve */
    private int f(double x, double arc_height, double factor) {
        if (scale > 0)
            return (int)-(Math.sin((x/factor)*Math.PI )* arc_height) + graph_height;
        else
            return 0;
    }
    
    
    
    /** To paint the parse chart on the screen g */
    public void paint(Graphics g) {
        //Wait for the constructor to be finished
        while (!constructed) {}
        
        super.paint(g);
        
        int word_nr = 0;
        String word = "";
        
        int nr_of_words = 0;
        
        final int part_of_speech = 1;
        final int sentence_part_start = 2;
        final int sentence_part_end = 3;
        final int truncate_part_of_speech_at = 4;
        
        final String font_string = "Arial Black";
        
        final int font_style0 = Font.BOLD;
        final int font_style1 = Font.BOLD;
        final int font_style2 = Font.PLAIN;
        
        final Font font0 = new Font(font_string, font_style0, 18);
        final Font font1 = new Font(font_string, font_style1, 16);
        final Font font2 = new Font(font_string, font_style2, 18);
        
        //For each stacked part (part of speech or part of sentence)
        word_nr = 0;
        int from = 0, to = 0;
        int part = 0;
        
        //Show the sentence, structure string and structure chart string above the graph
        g.setFont(font0);
        g.drawString(
        "Natural Language Parse by Napier Software, 2015", x_margin, 50);
        
        g.setFont(font1);
        g.drawString(sentence_original, x_margin, 80);
        g.drawString("Structure: " + structs, x_margin,110);
        
        //Print line(s) of structure chart split into lines at the newline characters
        int ya = 140;
        int k = 0;
        int j = 0;
        int len = structure_chart.length();
        //Set j to index of next newline or set j to end of line
        j = structure_chart.indexOf("\n", k);
        if (j == -1) j = len;
        while (j >= k) {
            if (j > k)  //Do not print the newline character so not (k, j+1) below
                g.drawString(structure_chart.substring(k, j), x_margin, ya);
            ya += 25;
            k = j+1;
            //If we have not already hit the end of the string
            if (k < len) {
                //Set j to index of next newline or set j to end of line
                j = structure_chart.indexOf("\n", k);
                if (j == -1) j = len;
            } else {
                //all l
                break;
            }
        }
        
        //Set another font for the graph
        g.setFont(font2);
        
        //Combine production names and arcs which span the same words
        int next_part = 0;
        String full_name = "";
        
        //For each stacked part
        for (int i = 0; i < n_sentence_parse; i++) {
            part = sentence_parse[i];
            if (i+1 < n_sentence_parse)
                next_part = sentence_parse[i+1];
            else
                next_part = 0;
            
            //The parts-of-speech are enumerated as 1 ..99, and represent 
            //each sentence word
            if (part > 0 && part < 100) {
                String pos = Part_of_speech.toString(part);
                
                //if (pos.length() > truncate_part_of_speech_at)
                //    pos = pos.substring(0, truncate_part_of_speech_at);
                
                // Draw the words of the sentence and the dots between (as ovals)
                //Draw (and avoid any index error)
                if (word_nr < sentence.length)
                    word = sentence[word_nr];
                else
                    word = "";
                
                int count = word.length();
                
                from = word_nr * xscale + x_margin;
                to = from + xscale;
                
                // The dots between the sentence word
                if (word_nr == 0) {
                    g.fillOval(from, graph_height, oval_size, oval_size);
                }
                g.fillOval(to, graph_height, oval_size, oval_size);
                
                //The actual sentence word spelling
                g.drawString(word, (from + xscale/2 - (count*10/2)), graph_height);
                
                //draw an arc in x range from..from+xscale, and ...
                for (int x = 0 ; x < xscale; x++) {
                    g.drawLine(x + from,
                    f(x, xscale/2, xscale), x + 1 + from,
                    f(x + 1, xscale/2, xscale));
                }
                /* draw the part-of-speech name at the top of the arc */
                String name = Part_of_speech.toString(part);
                //if (name.length() > 4)
                //    name = name.substring(0, 4);
                g.drawString(name,
                (from + xscale/2-5*name.length()),
                graph_height - (xscale/2 + 10));
                
                //We do NOT remember full_name for next part
                full_name = "";
                
                //Count this sentence word
                word_nr++;
                
            } else if (part >  200) {
                //The terminating "]" of each sentence part also holds info about its span
                //and its production.
                //That is,  for example:
                //part==3205 means span=3 words, type=2 (not used here), production=05
                
                //Get the production and span from the part number
                int production = part - part/100*100;
                
                
                //if (production <= Sentence_types.Main_sub_clause) {
                int span = part/1000;
                
                //Look forward to the next part's span
                int next_span = next_part/1000;
                
                //Set the start display position as word number
                int position = word_nr - span;
                
                //set actual start and end pixel numbers
                from = position * xscale + x_margin;
                to = from + span*xscale;
                
                //decide the sentence part name(s) which will appear on the arc
                String name = Sentence_types.Part[production];
                //If there are arcs at the same position then combine them:
                if (full_name.equals("")) {
                    full_name = name;
                } else {
                    full_name = name + ":" + full_name; //Combine the labels
                }
                
                //That is, do not draw the current arc if the next one is the same,
                //but instaed remember the name of the current part, to be added
                //to the next name
                if (span == next_span) {
                    //We remember full_name for next part
                } else {
                    //Avoid the arc of part-of-speech of each word
                    if (span == 1) {
                        //The Sentence part name
                        g.drawString(full_name,
                        from + span*xscale/2 - 5*full_name.length(),
                        graph_height - (int)(1.5*span * xscale/2 + 10));
                        
                    } else {
                        
                        //The arc line over span words
                        for (int x = 0 ; x < span*xscale; x++) {
                            g.drawLine(x + from,
                            f(x, span*xscale/2, span*xscale), x + 1 + from,
                            f(x + 1, span*xscale/2, span*xscale ));
                        }
                        //the sentence part name
                        g.drawString(full_name,
                        (from + span*xscale/2) - 5*full_name.length(),
                        graph_height - (span * xscale/2 + 10));
                    }
                    //We do NOT remember full_name for next part
                    full_name = "";
                }
            }
        }
        
    }
    
    /** Receive the next ParseChart */
    //public void next(String structure) {
    //    structure_chart = structure;
    //}
    
}
