package com.company;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Main {

    public static int DEBUG = 0;
    public static int DEBUG_SLIDE = 0;

    public static List getRelations(String file) {

        List<Relation> rel_list = new ArrayList<Relation>();
        FileInputStream inputStream = null;

        // Open the file
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

        //Read File Line By Line
        try {
            while ((strLine = br.readLine()) != null)   {
                // Print the content on the console
                System.out.println (strLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Close the input stream
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rel_list;

    }

    public static List stripFiles(Collection c, String s) {
        List<String> stripped = new ArrayList<String>();;

        for (Object f : c) {
            String stripped_filename = f.toString().replace(s,"");
            stripped.add(stripped_filename);
        }

        return stripped;
    }

    public static void setupPresentation(String presentation_name) throws FileNotFoundException, UnsupportedEncodingException {
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


    public static void addSlide(String presentation_name, String slide, int offset) throws IOException {
        presentation_name = presentation_name.concat(".html");
        FileWriter writer = new FileWriter(presentation_name, true);
        writer.write("<div class=\"step\" data-y=\"" + offset + "\">");
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
    }

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
        String relationsDirPath = "/Users/Naomi/localmh/MathHub/MiKoMH/pythagoreantheorem/relational";
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

        for (Object f : stripFiles(relationFiles, relationsDirPath)) {
            System.out.println(f);
        }

        for (Object f : relationFiles) {
            System.out.println(f);
            getRelations(f.toString());
        }

        // Start presentation

        setupPresentation("testpres");

        // Extract html information

        for (Object f : stripFiles(htmlFiles, htmlDirPath)) {

            System.out.println(f);
        }

//        for (Object f : htmlFiles) {
//            System.out.println(f);
//        }

        int x = 1000;
        String test;

        for (Object slide : htmlFiles) {
            // if not ".html" file
            if(!(slide.toString().replaceAll(".*/", "").equals(".html") || slide.toString().replaceAll(".*/", "").equals(".DS_Store"))) {
                addSlide("testpres", slide.toString(), x);
                x += 1000;
            }
        }

        // End presentation
        System.out.println("DONE");
        endPresentation("testpres");

//        Relation r = new Relation("parent", "/file1", "/file2")


    }
}
