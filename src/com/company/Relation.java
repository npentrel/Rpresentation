package com.company;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Relation {

    public String rel_type;
    public String file1;
    public String file2;

    public Relation(String relType, String fileOne, String fileTwo) {
        rel_type = relType;
        file1 = fileOne;
        file2 = fileTwo;
    }


}
