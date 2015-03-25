package com.company;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main {

    public static List getRelations(String file) {

        List<Relation> rel_list = new ArrayList<Relation>();;
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

    public static void main(String[] args) {

        List<String> modules = new ArrayList<String>();

        String dir_path = "/Users/Naomi/localmh/MathHub/MiKoMH/pythagoreantheorem/relational";

        File dir = new File(dir_path);

        Collection files = FileUtils.listFiles(
                dir,
                new RegexFileFilter("^(.*?)"),
                DirectoryFileFilter.DIRECTORY
        );

        for (Object f : stripFiles(files, dir_path)) {
            System.out.println(f);
        }

        for (Object f : files) {
            System.out.println(f);
            getRelations(f.toString());
        }

//        Relation r = new Relation("parent", "/file1", "/file2");
        

    }
}
