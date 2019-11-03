package de.invesdwin.util.lang;

import java.io.File;
import java.util.Iterator;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import de.invesdwin.norva.apt.staticfacade.StaticFacadeDefinition;
import de.invesdwin.util.lang.internal.AFilesStaticFacade;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FDate;

@Immutable
@StaticFacadeDefinition(name = "de.invesdwin.util.lang.internal.AFilesStaticFacade", targets = {
        org.apache.commons.io.FileUtils.class, java.nio.file.Files.class })
public final class Files extends AFilesStaticFacade {

    private Files() {}

    public static void purgeOldFiles(final File directory, final Duration threshold) {
        if (!directory.exists()) {
            return;
        }
        final FDate thresholdDate = new FDate().subtract(threshold);
        final Iterator<File> filesToDelete = iterateFiles(directory, new AgeFileFilter(thresholdDate.dateValue(), true),
                TrueFileFilter.INSTANCE);
        while (filesToDelete.hasNext()) {
            final File fileToDelete = filesToDelete.next();
            fileToDelete.delete();
        }
        for (final File f : directory.listFiles()) {
            deleteEmptyDirectories(f);
        }
    }

    /**
     * https://stackoverflow.com/questions/26017545/delete-all-empty-folders-in-java
     */
    public static long deleteEmptyDirectories(final File f) {
        final String[] listFiles = f.list();
        long totalSize = 0;
        for (final String file : listFiles) {

            final File folder = new File(f, file);
            if (folder.isDirectory()) {
                totalSize += deleteEmptyDirectories(folder);
            } else {
                totalSize += folder.length();
            }
        }

        if (totalSize == 0) {
            f.delete();
        }

        return totalSize;
    }

    public static String normalizeFilename(final String name) {
        return name.replace(":", "_").replace("@", "_");
    }

}
