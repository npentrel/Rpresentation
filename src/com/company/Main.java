package com.company;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.*;
import java.util.*;

import static org.apache.commons.io.FileUtils.copyFile;


public class Main {

    public static final boolean DEBUG = false;
    public static final boolean DEBUG_SLIDE = false;
    public static final boolean DEBUG_RELATION = true;
    public static final boolean DEBUG_PATHS = false;
    public static final boolean DEBUG_IMAGES = false;
    public static final boolean RELATION_PARSE_ERRORS = false;
    public static final String MATHHUB_PATH = "/Users/Naomi/localmh/MathHub";

    public static Hashtable<String, String[]> dependencies = new Hashtable<String, String[]>();
    public static Hashtable<String, String> next_topics = new Hashtable<String, String>();

    // Reads all relations from the relations directory and adds them to a local table.
    public static void getRelations(String file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        List<String> current_includes = new ArrayList<String>();

        List<String> list = new ArrayList<String>();
        while((str = in.readLine()) != null){
            list.add(str);
        }

        String[] stringArr = list.toArray(new String[0]);

        for (int i = 0; i < stringArr.length; i++) {
            if (stringArr[i].contains("theory ")) {
                int includes_num = 0;
                for (int j = i+1; j < stringArr.length && stringArr[j].contains("Includes "); j++) {
                    includes_num++;
                }
                System.out.println("includesnum: " + includes_num + " " + stringArr[i]);
                String[] element_includes = new String[includes_num];
                for (int j = i+1; j < i+1+includes_num && stringArr[j].contains("Includes "); j++) {
                    element_includes[j - i - 1] = stringArr[j].replaceAll("(Includes )([^\\ ]*)( http:\\/*)(([^\\/]+)(\\/))", "");
                }

//                for (int x = 0; x < element_includes.length; x++) {
//                    System.out.println(element_includes[x]);
//                }
                String new_key = stringArr[i].replaceAll("(theory http:\\/*)(([^\\/]+)\\/)", "").replaceAll("(\\.)(.*)", "");
                if (!dependencies.containsKey(new_key)) {
                    dependencies.put(new_key, element_includes);
                } else {
                    String[] values = dependencies.get(new_key);
                    int combined_size = values.length + element_includes.length;
                    String[] merged_values = new String[combined_size];
                    for (int m = 0; m < values.length; m++) {
                        merged_values[m] = values[m];
                    }
                    for (int n = values.length; n < combined_size; n++) {
                        merged_values[n] = element_includes[n-values.length];
                    }
                    dependencies.put(new_key, merged_values);
                }

                current_includes.clear();
                i += includes_num;
            } else {
                if (RELATION_PARSE_ERRORS) {
                    System.out.println("ERROR: " + stringArr[i]);
                }
            }
        }
    }

    // Takes a list of strings and returns them without the beginning path (s).
    public static List stripFiles(Collection c, String s) {
        List<String> stripped = new ArrayList<String>();

        for (Object f : c) {
            String stripped_filename = f.toString().replace(s,"");
            stripped.add(stripped_filename);
        }

        return stripped;
    }

    public static void copyImagesIntoFolder(String sourcePath) {
        File sourceDir = new File(sourcePath);
        Collection pictureFiles = FileUtils.listFiles(
                sourceDir,
                new RegexFileFilter(".*.(png|jpg|jpeg)"),
                DirectoryFileFilter.DIRECTORY
        );
        for (Object o : pictureFiles) {
            if (DEBUG_IMAGES) {
                System.out.println(o);
            }
            File image_src = new File(o.toString());
            String dest_filename = o.toString().replaceAll(".*/", "");
            File image_dest = new File(dest_filename);
            if (DEBUG_IMAGES) {
                System.out.println(dest_filename);
            }
            try {
                copyFile(image_src, image_dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setupPresentation(String presentation_name, String sourcePath) throws FileNotFoundException, UnsupportedEncodingException {
        copyImagesIntoFolder(sourcePath);
        presentation_name = presentation_name.concat(".html");
        PrintWriter writer = new PrintWriter(presentation_name , "UTF-8");

        writer.println("<html><head><title> hello </title>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html \n" +
                "charset=UTF-8\"> <link href=\"styles.css\" rel=\"stylesheet\" /> </head>\n" +
                "<body> <div id=\"impress\"> <div class=\"no-support-message\"> Sorry! Your \n" +
                "browser is unable to load this Impress presentation. Please update \n" +
                "your browser. </div>\n");
        writer.close();
    }


    public static void addSlide(String presentation_name, String slide, int x_offset, int y_offset) throws IOException {
        presentation_name = presentation_name.concat(".html");
        FileWriter writer = new FileWriter(presentation_name, true);
        writer.write("<div class=\"step\" data-x=\"" + x_offset + "\" data-y=\"" + y_offset + "\">");
        writer.write("\n");
        writer.write(slide);
        writer.write("\n");

        // Open the file

        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(slide);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;
        boolean metadata = true;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
            if (metadata) {
                if (strLine.contains("</omdoc:metadata>")) {
                    metadata = false;
                }
            } else {

                if (DEBUG || DEBUG_SLIDE) {
                    System.out.println(strLine);
                }

                if (strLine.contains("</omdoc:omgroup>") || strLine.contains("</body>")) {
                    break;
                }
                writer.write(strLine);
            }
        }

        writer.write("\n");
        //Close the input stream
        br.close();

        writer.write("</div>");
        writer.write("\n");

        writer.close();
        addIncludesSlides(slide);
    }

    public static String getTheorySlidePath(String slide) {
        String slide_short = slide.replaceAll("(/)(((([^/\n]*)(/)){9}))","").replaceAll(".html", "");
        String path_short = slide.replaceAll(".*(localmh/MathHub/)","").replaceAll("(export).*", "");
        String theorySlidePath = path_short + slide_short;
        System.out.println("4 slide: " + theorySlidePath);
        return theorySlidePath;
    }

    public static void addIncludesSlides(String slide) {
        String theorySlidePath = getTheorySlidePath(slide);

    };

    public static void endPresentation(String presentation_name) {
        presentation_name = presentation_name.concat(".html");
        FileWriter writer = null;
        try {
            writer = new FileWriter(presentation_name, true);
            writer.write("</div></div><script type=\"text/javascript\" src=\"impress.js\">" +
                    "</script></body></html>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

//        List<String> modules = new ArrayList<String>();

        // Setup paths for the directory with relational data and the export html files
        String dirPath = MATHHUB_PATH + "/MiKoMH/pythagoreantheorem/";
        String sourcePath = dirPath + "source";
        String relationsDirPath = dirPath + "relational";
        File relationsDir = new File(relationsDirPath);

        Collection relationFiles = FileUtils.listFiles(
                relationsDir,
                new RegexFileFilter("^(.*?)"),
                DirectoryFileFilter.DIRECTORY
        );

        String htmlDirPath = "/Users/Naomi/localmh/MathHub/MiKoMH/pythagoreantheorem/export/planetary/narration";
//        String htmlDirPath = "/Users/Naomi/localmh/MathHub/MiKoMH/pythagoreantheorem/export/planetary/content/http..mathhub.info/MiKoMH/pythagoreantheorem";
        File htmlDir = new File(htmlDirPath);

        Collection htmlFiles = FileUtils.listFiles(
                htmlDir,
                new RegexFileFilter("^(.*?)"),
                DirectoryFileFilter.DIRECTORY
        );


        // Extract relational information

        if (DEBUG || DEBUG_PATHS) {
            System.out.println("\nPATHS: -----------------------------------------------------------------------------------\n");
            for (Object f : stripFiles(relationFiles, relationsDirPath)) {
                System.out.println(f);
            }
        }

        if (DEBUG || DEBUG_PATHS) {
            System.out.println("\nRELATION: -----------------------------------------------------------------------------------\n");
        }
        for (Object f : relationFiles) {
            if (DEBUG || DEBUG_PATHS) {
                System.out.println(f);
            }
            getRelations(f.toString());
        }

        // Start presentation

        setupPresentation("testpres", sourcePath);

        // Extract html information

        if (DEBUG || DEBUG_PATHS) {
            System.out.println("\nFILE PATHS: -----------------------------------------------------------------------------------\n");
        }
        for (Object f : stripFiles(htmlFiles, htmlDirPath)) {
            if (DEBUG || DEBUG_PATHS) {
                System.out.println(f);
            }
        }

        int x = 1000;

        for (Object slide : htmlFiles) {
            // if not ".html" file
            if (!(slide.toString().replaceAll(".*/", "").equals(".html") || slide.toString().replaceAll(".*/", "").equals(".DS_Store"))) {
                addSlide("testpres", slide.toString(), x, 0);
                x += 1500;
            }
        }

        // End presentation
        endPresentation("testpres");

        Enumeration<String> it = dependencies.keys();

        System.out.println(dependencies.size());
        while (it.hasMoreElements()) {
            String key = it.nextElement();
            String[] values = dependencies.get(key);
            if (DEBUG_RELATION) {
                System.out.println("Theory path: " + key);
                for (int i = 0; i < values.length; i++) {
                    System.out.println("Include path: " + values[i]);
                }
            }
        }


//        Relation r = new Relation("parent", "/file1", "/file2")


    }
}
