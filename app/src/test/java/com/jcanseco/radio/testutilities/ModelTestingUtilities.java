package com.jcanseco.radio.testutilities;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

public class ModelTestingUtilities {

    public static Object parseFakeJson(Object instanceOfCallingClass, String fakeJsonFilename, Class model) throws FileNotFoundException {
        ClassLoader classLoader = instanceOfCallingClass.getClass().getClassLoader();
        URL fakeJsonResourceUrl = classLoader.getResource(fakeJsonFilename);
        String pathOfFakeJsonFile = fakeJsonResourceUrl.getPath();
        Reader reader = new FileReader(new File(pathOfFakeJsonFile));
        return new Gson().fromJson(reader, model);
    }
}