/*
 * Copyright (c) 2016 BMD Software and University of Aveiro.
 *
 * Neji is a flexible and powerful platform for biomedical information extraction from text.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it.
 * However, you may not use it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package pt.ua.tm.neji.util;

import com.aliasi.util.Pair;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class that writes a root folder and a set of extra files in other locations
 * into a ZIP file.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class ZipGenerator {

    private final File rootFolder;
    private final Pair<String, File>[] otherFiles;
    private final PipedOutputStream pos;
    private final PipedInputStream pis;
    private ZipOutputStream zip;
    private BufferedOutputStream out;

    /**
     * Builds a zip file generator that stores in it the specified root folder and its sub-folders and sub-files.
     * To avoid having to copy data from other paths into the root in order to add it to the resulting
     * zip file, the user can specify extra files to be added into the root on generation. The user must specify
     * a final name for each extra file object.
     */
    public ZipGenerator(final File rootFolder, final Pair<String, File>... extraFilesToAddIntoRoot) throws IOException {
        this.rootFolder = rootFolder;
        this.otherFiles = extraFilesToAddIntoRoot;

        // writing in the PipedOutputStream will pass the data
        // into the PipedInputStream when completed
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        zip = new ZipOutputStream(pos);
        out = new BufferedOutputStream(zip);
    }

    public InputStream generate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.print("Generating zip file");

                    fill("", new Pair[]{ new Pair<>(rootFolder.getName(), rootFolder) }, zip, out);
                    fill(rootFolder.getName() + File.separator, otherFiles, zip, out);

                    System.out.println("done!");

                    out.flush();
                    out.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        return pis;
    }

    private static void fill(String parentPath, Pair<String, File>[] files, ZipOutputStream zip, BufferedOutputStream out) throws IOException {
        for (Pair<String, File> pair : files) {

            if(pair.b()==null)
                continue;

            if(pair.b().isDirectory() && pair.b().list().length>0) {
                File[] subFiles = pair.b().listFiles();
                int size = subFiles.length;
                Pair<String, File>[] subPairs = new Pair[size];
                for(int i = 0; i<size; i++){
                    subPairs[i] = new Pair<>(subFiles[i].getName(), subFiles[i]);
                }

                fill(parentPath + pair.a() + File.separator, subPairs, zip, out);

            } else if(pair.b().isFile()) {
                zip.putNextEntry(new ZipEntry(parentPath + pair.a()));
                FileInputStream fis = new FileInputStream(pair.b());
                IOUtils.copyLarge(fis, out);
                fis.close();
                out.flush();
            }

            System.out.print(".");

            zip.closeEntry();
        }
    }
}
