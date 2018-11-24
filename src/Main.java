import org.ini4j.Config;
import org.ini4j.Ini;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {

    public static void main(String[] args) {
        String rootDirLocation = System.getProperty("user.dir");
        String rawArticlesDirLocation = rootDirLocation + File.separator + "raw article";
        if (!new File(rawArticlesDirLocation).exists()){
            new File(rawArticlesDirLocation).mkdir();

        }

        try {
            Files.walkFileTree(Paths.get(rawArticlesDirLocation), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    dot();
                    return super.preVisitDirectory(path, basicFileAttributes);
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    dot();
                    if (path.toString().matches(".*\\.phba")){
                        Ini rawArticleIni = new Ini(path.toFile());
                        String icon = rawArticleIni.get("Article","Article Icon");
                        String date = rawArticleIni.get("Article","Article date");
                        String title = rawArticleIni.get("Article","Article title");
                        String content = rawArticleIni.get("Article","Article content");


                    }
                    return super.visitFile(path, basicFileAttributes);
                }

            });
        } catch (Exception e) {
            System.err.println("\r\n[ERROR] " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("\r\n[INFO] Done.");
        JOptionPane.showMessageDialog(null, "Done", "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void dot() {
        System.out.print(".");
    }

}
