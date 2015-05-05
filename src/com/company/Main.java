package com.company;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import javax.swing.JOptionPane;

import java.io.*;
import java.util.*;

import static org.apache.commons.io.FileUtils.copyFile;


public class Main {

    public static final boolean DEBUG = false;
    public static final boolean DEBUG_SLIDE = false;
    public static final boolean DEBUG_RELATION = false;
    public static final boolean DEBUG_PATHS = false;
    public static final boolean DEBUG_IMAGES = false;
    public static final boolean RELATION_PARSE_ERRORS = false;
    public static final boolean USE_DIALOG = false;

    // Paths for the directory with relational data and the export html files
    public static String projectName = "pythagoreantheorem";
    public static String mathHubPath = "/Users/Naomi/localmh/MathHub";
    public static String MiKoMH = "/MiKoMH/";
    public static String dirPath = mathHubPath + MiKoMH + projectName + "/";
    public static String sourcePath = dirPath + "source";
    public static String relationsDirPath = dirPath + "relational";
    public static String notes_path = sourcePath + "/notes/notes.tex";
    public static String outputPresentationPath = projectName;
    public static String htmlDirPath = dirPath + "export/planetary/narration";

    public static Hashtable<String, String[]> dependencies = new Hashtable<String, String[]>();

    public static Vector<String> top_order = new Vector<String>();

    public static int SLIDE_OFFSET = 1500;


    public static void getOrderofSlides(String notes_path, int level) throws IOException {

        if (DEBUG)
            System.out.println(notes_path);

        File f = new File(notes_path);
        if(!f.exists() || f.isDirectory()) {
            return;
        }

        BufferedReader in = new BufferedReader(new FileReader(notes_path));
        String str;

        List<String> list = new ArrayList<String>();
        while ((str = in.readLine()) != null){
            list.add(str);
        }
        String[] stringArr = list.toArray(new String[0]);

        String out;

        for (int i = 0; i < stringArr.length; i++) {
            if (!stringArr[i].contains("%") && stringArr[i].contains("mhinputref")) {
                out = stringArr[i].replaceAll("(.)*mhinputref.{1}", "").replaceAll("}(.)*","").replaceAll(".tex","");
                top_order.add(level + " " + out);
                getOrderofSlides(sourcePath + '/' + out + ".tex", level + 1);
            }
        }

    }

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

//                System.out.println("includesnum: " + includes_num + " " + stringArr[i]);
                String[] element_includes = new String[includes_num];
                for (int j = i+1; j < i+1+includes_num && stringArr[j].contains("Includes "); j++) {
                    element_includes[j - i - 1] = stringArr[j].replaceAll("(Includes )([^\\ ]*)( http:\\/*)(([^\\/]+)(\\/))", "").replaceAll("(.omdoc\\?.*)", "");
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

    public static void removePreviousImages(String sourcePath) {
        File sourceDir = new File(".");
        Collection pictureFiles = FileUtils.listFiles(
                sourceDir,
                new RegexFileFilter(".*.(png|jpg|jpeg)"),
                DirectoryFileFilter.DIRECTORY
        );
        for (Object o : pictureFiles) {
            if (o.toString().matches("(\\./)[^/]*.(png|jpg|jpeg)")) {
                System.out.println("DELETE: " + o.toString());
                try {
                    File file = new File(o.toString());
                    if (!file.delete()) {
                        System.out.println("Problem deleting: " + file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    public static int presentationLength() {
        System.out.println("TOPORDERSIZE: " + top_order.size());
        return (500+(((top_order.size()-1) * SLIDE_OFFSET)/2));
    }

    public static void setupPresentation(String presentation_name, String sourcePath) throws FileNotFoundException, UnsupportedEncodingException {
        removePreviousImages(sourcePath);
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


    public static String addSlide(String presentation_name, String slide, int x_offset, int y_offset) throws IOException {
        presentation_name = presentation_name.concat(".html");
        FileWriter writer = new FileWriter(presentation_name, true);
        writer.write("<div class=\"step slide\" data-x=\"" + x_offset + "\" data-y=\"" + y_offset + "\">");
        writer.write("\n");
        writer.write("<header> </header>\n<main>");
        writer.write("\n");
        writer.write(slide);
        writer.write("\n");
        writer.write("</main>");
        writer.write("\n");

        // Open the file
        System.out.println("PRESENTATIONNAME: " + slide);

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
        return addIncludesSlides(slide);
    }

    public static String getTheorySlidePath(String slide) {
        String slide_short = slide.replaceAll("(/)(((([^/\n]*)(/)){9}))","").replaceAll(".html", "");
        String path_short = slide.replaceAll(".*(localmh/MathHub/)","").replaceAll("(export).*", "");
        String theorySlidePath = path_short + slide_short;
        System.out.println("4 slide: " + theorySlidePath);
        return theorySlidePath;
    }

    public static String addIncludesSlides(String slide) {
        String theorySlidePath = getTheorySlidePath(slide);
        return theorySlidePath;

    };

    public static void endPresentation(String presentation_name, int length) {
        presentation_name = presentation_name.concat(".html");
        FileWriter writer = null;
        try {
            writer = new FileWriter(presentation_name, true);
            writer.write("<div id=\"overview\" class=\"step\" data-x=\"" + length + "\n" +
                    "\" data-y=\"1000\" data-scale=\"" + (top_order.size()-1) +"\n" +
                    "\">" +
                    "</div>" +
                    "</div>" +
                    "<script type=\"text/javascript\" src=\"impress.js\">" +
                    "<script type=\"text/javascript\">impress().init();</script>" +
                    "</script></body></html>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateProjectVariables() {
        projectName = JOptionPane.showInputDialog("Welcome to RPresentation! To get started, please tell me the name of the project you want to create a presentation for: ");
        mathHubPath = JOptionPane.showInputDialog("Please tell me the path to your MathHub folder (e.g. /Users/Naomi/localmh/MathHub): ");
        String MiKoMH_local = JOptionPane.showInputDialog("Please tell me which directory your project is in (e.g. MiKoMH), if it is in no further directory please leave this blank: ");
        if (MiKoMH_local != "") {
            MiKoMH = "/" + MiKoMH_local + "/";
        } else {
            MiKoMH = "/";
        }
    }

    public static void printEndDialog() {
        String currentDirectory = System.getProperty("user.dir");
        JOptionPane.showConfirmDialog(null, "You can find your presentation in " + currentDirectory, "Presentation created", JOptionPane.PLAIN_MESSAGE);
    }

    public static void extractRelationalInfo(Collection relationFiles) throws IOException {
        if (DEBUG || DEBUG_PATHS) {
            System.out.println("\nPATHS: -----------------------------------------------------------------------------------\n");
            for (Object f : stripFiles(relationFiles, relationsDirPath)) {
                System.out.println(f);
            }
            System.out.println("\nRELATION: -----------------------------------------------------------------------------------\n");
        }
        for (Object f : relationFiles) {
            if (DEBUG || DEBUG_PATHS) {
                System.out.println(f);
            }
            getRelations(f.toString());
        }
    }

    public static void extractHtmlInformation(Collection htmlFiles) {
        if (DEBUG || DEBUG_PATHS) {
            System.out.println("\nFILE PATHS: -----------------------------------------------------------------------------------\n");
        }
        for (Object f : stripFiles(htmlFiles, htmlDirPath)) {
            if (DEBUG || DEBUG_PATHS) {
                System.out.println(f);
            }
        }
    }

    public static void addSlidesToPresentation() throws IOException {
        int x = 1000;
        int y = 0;

        for (Object o : top_order) {
            String slidePath = htmlDirPath + '/' + o.toString().replaceAll("[0-9]*( )*", "") + ".html";

            File f = new File(slidePath);
            if(!f.exists() || f.isDirectory()) {
                continue;
            }

            System.out.println("HTML:" + slidePath);

            String DependenciesKey = addSlide(outputPresentationPath, slidePath.toString(), x, y);

            System.out.println("!!SLIDE: " + slidePath.toString());
            if (dependencies.containsKey(DependenciesKey)) {
                String[] values = dependencies.get(DependenciesKey);
                for (int i = 0; i < values.length; i++) {
                    if (values[i].contains(projectName)) {
                        y += 1000;

                        System.out.println("yes");
                        String dependentSlidePath = htmlDirPath.concat(values[i].toString().replaceAll("MiKoMH/" + projectName, "")).concat(".html");
                        System.out.println("DEP:" + dependentSlidePath);

                        addSlide(outputPresentationPath, dependentSlidePath, x, y);

                        String currentDependencyKey = dependentSlidePath.replaceAll("(/)(((([^/]*)(/)){9}))", "");
                        System.out.println("CURRENT DEP KEY: " + currentDependencyKey);
                        int index = top_order.indexOf(currentDependencyKey);
                        System.out.println("Index: " + index + "\n\n");

//                        char currentLevel = o.toString().charAt(index);
//
//                        boolean found = false;
//                        for (Object p : top_order) {
//                            if (o == p) {
//                                found = true;
//                                break;
//                            }
//                        }

//                        add until number changes
// data-rotate-y="90"

                    }
                }
                y = 0;
            }
            x += SLIDE_OFFSET;

        }

        //        for (Object slide : htmlFiles) {
//            // if not ".html" file
//            if (!(slide.toString().replaceAll(".*/", "").equals(".html") || slide.toString().replaceAll(".*/", "").equals(".DS_Store"))) {
//                String DependenciesKey = addSlide("testpres", slide.toString(), x, y);
//
//                System.out.println("!!SLIDE: " + slide.toString());
//                if (dependencies.containsKey(DependenciesKey)) {
//                    String[] values = dependencies.get(DependenciesKey);
//                    for (int i = 0; i < values.length; i++) {
//                        if (values[i].contains(projectName)) {
//                            y += 1000;
//                            System.out.println("yes");
//                            String slidePath = htmlDirPath.concat(values[i].toString().replaceAll("MiKoMH/" + projectName, "")).concat(".html");
//                            System.out.println(slidePath);
//                            addSlide("testpres", slidePath, x, y);
//                        }
//                    }
//                    y = 0;
//                }
//                x += SLIDE_OFFSET;
//            }
//        }

        if (DEBUG) {
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
        }
    }

    public static void main(String[] args) throws IOException {

        if (USE_DIALOG) {
            updateProjectVariables();
        }

        // setup collections of files
        File relationsDir = new File(relationsDirPath);
        Collection relationFiles = FileUtils.listFiles(relationsDir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);

        File htmlDir = new File(htmlDirPath);
        Collection htmlFiles = FileUtils.listFiles(htmlDir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);

        getOrderofSlides(notes_path, 0);
        extractRelationalInfo(relationFiles);

        setupPresentation(outputPresentationPath, sourcePath);
        extractHtmlInformation(htmlFiles);
        addSlidesToPresentation();
        endPresentation(outputPresentationPath, presentationLength());

        printEndDialog();
    }
}
