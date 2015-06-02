package com.company;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.*;
import java.util.*;

public class Main {

    public static final boolean DEBUG = false;
    public static final boolean DEBUG_SLIDE = false;
    public static final boolean DEBUG_RELATION = false;
    public static final boolean DEBUG_PATHS = false;
    public static final boolean DEBUG_IMAGES = false;
    public static final boolean RELATION_PARSE_ERRORS = false;
    public static final boolean USE_DIALOG = false;
    public static final boolean SCREENSHOT_HACK = true;
    public static final boolean CREATE_DEPENDENCY_PATH = false;
    public static final boolean PRINT_DEPENDENCY_GRAPH = false;
    public static final boolean SUPPRESS_WARNINGS = true;

    // Paths for the directory with relational data and the export html files
    public static String projectName = "GenCS";

    public static String mathHubPath = "/Users/Naomi/localmh/MathHub";
    public static String MiKoMH = "/MiKoMH/";
    public static String dirPath = mathHubPath + MiKoMH + projectName + "/";
    public static String sourcePath = dirPath + "source";
    public static String relationsDirPath = dirPath + "relational2";

    public static String notes_path = sourcePath + "/course/notes/notes.tex";
    public static String outputPresentationPath = projectName;
    public static String htmlDirPath = dirPath + "export/planetary/narration";
    public static String presentationTitle = "Gen CS Presentation";
    public static String presentationAuthor = "Naomi Pentrel";

    public static Hashtable<String, String[]> dependencies = new Hashtable<String, String[]>();

    public static Vector<String> top_order = new Vector<String>();

    public static int slideXOffset = 1250;
    public static int slideYOffset = 800;
    public static int primaryNarrativePathSlideCount = 0;


    public static void main(String[] args) throws IOException {

        System.out.println(sourcePath);

        if (USE_DIALOG) {
            DialogueQuestions.updateProjectVariables();
        }

        // setup collections of files
        File relationsDir = new File(relationsDirPath);
        if (!relationsDir.isDirectory()) {
            System.out.println("ERROR: Seems like the directory for relational data is missing? Please run lmh pdf -f in your directory!\nWe tried:" + relationsDirPath);
            return;
        }
        Collection relationFiles = FileUtils.listFiles(relationsDir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);

        File htmlDir = new File(htmlDirPath);
        if (!htmlDir.isDirectory()) {
            System.out.println("ERROR: Seems like the directory for html data is missing? Please run lmh xhtml -f in your directory!");
            return;
        }
        Collection htmlFiles = FileUtils.listFiles(htmlDir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);

        OMDocHelper.getOrderOfSlides(notes_path, 0);
        OMDocHelper.extractRelationalInfo(relationFiles);

        PresentationMaker.setupPresentation(outputPresentationPath, sourcePath);
        if (!SCREENSHOT_HACK) {
            OMDocHelper.extractHtmlInformation(htmlFiles);
        }
        
        int xOverviewLength = Logic.addSlidesToPresentation();
        PresentationMaker.endPresentation(outputPresentationPath, xOverviewLength);

        if (USE_DIALOG) {
            DialogueQuestions.printEndDialog();
        }
    }
}
