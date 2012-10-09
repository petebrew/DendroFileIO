package org.tridas.io.formats.tucson;


import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegexTestHarness {

    public static void main(String[] args){



        Pattern pattern = 
        Pattern.compile("[\\w\\t -.]{8}[\\t\\d -]{3}[\\d]{1}[ -]{1}[\\t\\d -]{4}[\\d]{1}", Pattern.CASE_INSENSITIVE);

        Matcher matcher = 
        pattern.matcher("﻿ICC219B 1912  1341  1473  1655  1542  1259  1383  1571  1940                      ");

        boolean found = false;
        while (matcher.find()) {
            String out = "I found the text" +
                " \""+matcher.group()+"\" starting at " +
                "index "+matcher.start()+" and ending at index "+matcher.end()+"";
            found = true;
            System.out.println(out);
        }
        if(!found){
        	System.out.println("No match found");
        }
        
    }
}