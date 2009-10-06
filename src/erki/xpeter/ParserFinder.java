/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
 * 
 * This file is part of the chatbot xpeter.
 * 
 * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import erki.api.util.Log;

/**
 * This class searches the current directory for classes implementing the {@link Parser} interface.
 * 
 * @author Edgar Kalkowski
 */
public class ParserFinder {
    
    private static final String CLASS_ENDING = ".class";
    private static final String JAR_ENDING = ".jar";
    private static final String INTERFACE = "erki.xpeter.Parser";
    private static final String PACKAGE = "erki.xpeter.parsers";
    
    private ParserFinder() {
        
    }
    
    @SuppressWarnings("unchecked")
    private static void findParsers(File folder, Set<Class<? extends Parser>> botClasses,
            String path) {
        
        for (File file : folder.listFiles()) {
            
            if (file.isDirectory()) {
                findParsers(file, botClasses, new String(path + file.getName() + "."));
            } else if (file.isFile() && file.getName().endsWith(CLASS_ENDING)) {
                String className = file.getName().replaceAll(CLASS_ENDING, "");
                Class<?> clazz = null;
                
                try {
                    clazz = Class.forName(path + className);
                } catch (ClassNotFoundException e) {
                    continue;
                }
                
                if (isImplementing(clazz, INTERFACE)
                        && clazz.getPackage().getName().startsWith(PACKAGE)) {
                    botClasses.add((Class<? extends Parser>) clazz);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static boolean isImplementing(Class<?> clazz, String intorface) {
        
        for (Class c : clazz.getInterfaces()) {
            
            if (c.getCanonicalName().equals(intorface)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Searches the {@code folder} for classes implementing the {@link Parser} interface.
     * 
     * @param folder
     *        The folder to look at.
     * @return A list containing the class objects of all classes found the implement the {@code
     *         Parser} interface.
     */
    @SuppressWarnings("unchecked")
    public static TreeSet<Class<? extends Parser>> findParsers(File folder) {
        
        TreeSet<Class<? extends Parser>> result = new TreeSet<Class<? extends Parser>>(
                new Comparator<Class<?>>() {
                    
                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        return o1.getCanonicalName().compareTo(o2.getCanonicalName());
                    }
                });
        
        for (File file : folder.listFiles()) {
            
            if (file.getName().endsWith(JAR_ENDING)) {
                Enumeration<JarEntry> entries = null;
                
                try {
                    entries = new JarFile(file).entries();
                } catch (IOException e) {
                    Log.error(e);
                    Log.warning("Could not read jar file " + file + ". Trying to continue.");
                }
                
                while (entries.hasMoreElements()) {
                    JarEntry element = entries.nextElement();
                    String name = element.getName();
                    
                    if (name.endsWith(CLASS_ENDING)) {
                        name = name.replaceAll(CLASS_ENDING, "");
                        name = name.replaceAll(System.getProperty("file.separator"), ".");
                        Class<?> clazz = null;
                        
                        try {
                            clazz = Class.forName(name);
                        } catch (ClassNotFoundException e) {
                            Log.error(e);
                            Log.warning("Could not read class " + name + ". Trying to continue.");
                        }
                        
                        if (isImplementing(clazz, INTERFACE)
                                && clazz.getPackage().getName().startsWith(PACKAGE)) {
                            result.add((Class<? extends Parser>) clazz);
                        }
                    }
                }
            }
        }
        
        findParsers(folder.getAbsoluteFile(), result, "");
        return result;
    }
}
