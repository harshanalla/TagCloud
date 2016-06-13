import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a TagCloud from the given text file. Uses only Java Collection
 * Frameworks.
 *
 * @author Kumar Dhital and Harsha Nalla
 *
 */
public final class TagCloud {
    /**
     * Creating various variables that are used frequently in the entire program
     */
    private static final String SEPARATORS = " 1234567890\t\n\r`~!@#$%^&*()-_+=[]{}\\|:;'\",.<>/?";
    private static final int maxSize = 42;
    private static final int minSize = 11;
    public static int maxFreq = 0;
    public static int minFreq = Integer.MAX_VALUE;
    public static int numberOfWords = 0;
    /*
     * Creating the printWriter and BufferedReader here so that the entire
     * program can use the same printWriter and BufferedReader
     */
    private static PrintWriter out;
    private static BufferedReader input;

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        //BufferedReader to get Data from the user
        BufferedReader inputName = new BufferedReader(new InputStreamReader(System.in));
        //Map to store the data.
        Map<String, Integer> map = new HashMap<String, Integer>();

        //Get Data from the user
        String inputFileName, strOfNumberOfWords, outputFileName;
        try {
            System.out.print("Enter the name for the input file: ");
            inputFileName = inputName.readLine();
            //Get the scanner to the desired location.
            input = new BufferedReader(new FileReader(inputFileName));

            System.out.print("Enter the name for the output file: ");
            outputFileName = inputName.readLine();
            //initiate the printWriter to the desired file
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
            System.out.println("Number of words to be included in the generated tag cloud: ");
            strOfNumberOfWords = inputName.readLine();
            //initiate numberOfWords
            numberOfWords = Integer.parseInt(strOfNumberOfWords);

            //Get Words and calculate their frequency from the file.
            getWordsAndWordCount(map);
            if (map.size() < numberOfWords) {
                System.err.println("Error: Number of words " + numberOfWords + " is greater than"
                        + " the total unique words: " + map.size());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            return;
        } catch (IOException e) {
            System.err.println("Error while reading the input data. ");
            return;
        } catch (NumberFormatException e) {
            System.err.println("Error: Enter an integer for the Number Of Words");
            return;
        }

        //Print the header of the html file.
        outputHeader(inputFileName);
        List<Map.Entry<String, Integer>> mapInList = new ArrayList<Map.Entry<String, Integer>>();
        mapInList.addAll(map.entrySet());
        map.clear();//Clearing the memory for faster run time

        //Arrange the list in descending order by frequency
        Collections.sort(mapInList, arrangeFrequencyInDescendingOrder);

        //Removes all the unnecessary Map.Entry
        int length = mapInList.size();
        for (int i = numberOfWords; i < length; i++) {
            mapInList.remove(numberOfWords);
        }
        //Arrange the list in alphabetical order.
        Collections.sort(mapInList, alphabetize);

        //Print the arrayList in the desired model.
        while (mapInList.size() > 0) {
            OutputWordsAndFooter(mapInList.remove(0));
        }

        //Close the streams and printWriter.
        out.close();
        try {
            inputName.close();
            input.close();
        } catch (IOException e) {
            System.err.println("Error while closing the streams.");
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures
     *
     *          <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     *          </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";
        String result;
        int i = position;
        if (!SEPARATORS.contains(String.valueOf(text.charAt(position)))) {
            while (i < text.length() && !SEPARATORS.contains(String.valueOf(text.charAt(i)))) {
                i++;
            }
            result = text.substring(position, i);
        } else {
            while (i < text.length() && SEPARATORS.contains(String.valueOf(text.charAt(i)))) {
                i++;
            }
            result = text.substring(position, i);
        }
        return result;
    }
    //edit-1/2
    
    /**
     *
     * @param map
     *            to store the words and frequency
     * @updates map
     *
     */
    private static void getWordsAndWordCount(Map<String, Integer> map) {
        assert input != null : "Violation of: out is not null";
        try {
            String line = input.readLine();
            while (line != null) {
                for (int i = 0; i < line.length(); i++) {
                    String word = nextWordOrSeparator(line, i);
                    word = word.toLowerCase();
                    i = i + word.length() - 1;
                    boolean goIn = true;
                    for (int j = 0; j < word.length(); j++) {
                        if (SEPARATORS.contains((String.valueOf(word.charAt(j))))) {
                            goIn = false;
                        }
                    }
                    if (goIn) {
                        incrementCountOrAddNewWordAndUpdateBorderFreq(word, map);

                    }
                }
                line = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading data from the file. ");
        }
    }

    /**
     * Updates the minFreq and maxFreq of all the words Adds a new word to the
     * map along with a count of 1 if it is the first occurrence of the word
     * else updates the map
     *
     * @param word
     * @param map
     */
    private static void incrementCountOrAddNewWordAndUpdateBorderFreq(String word, Map<String, Integer> map) {
        if (!map.containsKey(word)) {
            map.put(word, 1);
            minFreq = 1;
            if (1 > minFreq) {
                maxFreq = 1;
            }

        } else {
            int count = map.get(word) + 1;
            ((Object) map).replace(word, map.get(word), count);
            if (count > maxFreq) {
                maxFreq = count;
            }
            if (count < minFreq) {
                minFreq = count;
            }
        }
    }

    /**
     * prints the header into the html file
     *
     * @param inputFileName
     */
    private static void outputHeader(String inputFileName) {
        assert out != null : "Violation of: out is not null";
        out.println("<html>");
        out.println("<head>");
        out.print("<title>");
        out.print("The top " + numberOfWords + " words in " + inputFileName);
        out.println("</title>");

        out.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.print("</head>");
        out.println("<body>");
        out.print("<h3>The top " + numberOfWords + " words in " + inputFileName + "</h3>");
        out.println("<hr />");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");
    }

    /**
     *
     * @param count
     * @return the calculatedFontSize
     */
    private static int calculateFontSize(int count) {
        if (count == minFreq) {
            return minSize;
        } else if (count == maxFreq) {
            return maxSize;
        } else {
            double i = 0.0;
            int returnValue = 0;
            double diffOfFreq = maxFreq - minFreq + 0.0;
            i = ((count - minFreq) / diffOfFreq) * maxSize;
            returnValue = (int) i + minSize;
            return returnValue;
        }
    }

    /**
     * Outputs the a table containing all the words in the generated HTML file.
     *
     * @param pair
     *            a pair containing a word and the its frequency
     */
    private static void OutputWordsAndFooter(Map.Entry<String, Integer> pair) {
        assert out != null : "Violation of: out is not null";
        out.println("<span style=\"cursor:default\" class=\"f" + calculateFontSize(pair.getValue())
                + "\" title=\"count: " + pair.getValue() + "\">" + pair.getKey() + "</span>");
        out.println("</body>");
        out.println("</html>");

    }

    /*
     * Comparator to arrange the map's values in descending order
     */
    private static Comparator<Map.Entry<String, Integer>> arrangeFrequencyInDescendingOrder = new Comparator<Map.Entry<String, Integer>>() {
        @Override
        public int compare(Map.Entry<String, Integer> p1, Map.Entry<String, Integer> p2) {

            return p2.getValue() - p1.getValue();
        }

    };
    /*
     * Comparator to alphabetize the map's keys.
     */
    private static Comparator<Map.Entry<String, Integer>> alphabetize = new Comparator<Map.Entry<String, Integer>>() {
        @Override
        public int compare(Map.Entry<String, Integer> p1, Map.Entry<String, Integer> p2) {
            return p1.getKey().compareTo(p2.getKey());
        }
    };

}
