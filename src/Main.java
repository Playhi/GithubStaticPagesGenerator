import org.ini4j.Ini;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        String rootDirLocation = System.getProperty("user.dir");
        String rawArticlesDirLocation = rootDirLocation + File.separator + "raw article";
        if (!new File(rawArticlesDirLocation).exists()) {
            new File(rawArticlesDirLocation).mkdir();
        }
        String configDirLocation = rootDirLocation + File.separator + "config";
        if (!new File(configDirLocation).exists()) {
            new File(configDirLocation).mkdir();
        }

        try {

            if (!new File(configDirLocation + File.separator + "config.ini").exists()) {
                new File(configDirLocation + File.separator + "config.ini").createNewFile();
            }
            Ini configIni = new Ini(new File(configDirLocation + File.separator + "config.ini"));


            ArrayList<String> rawArticlesPaths = new ArrayList<>();

            printInfo("Copy some files.");
            Files.walkFileTree(Paths.get(rootDirLocation + File.separator + "template" + File.separator + "source"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    Path destination = Paths.get(path.toAbsolutePath().toString().replace(File.separator + "template" + File.separator + "source", File.separator + "article"));
                    Files.createDirectories(destination.getParent());
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    return super.visitFile(path, basicFileAttributes);
                }

            });

            printInfo("Searching articles.");
            Files.walkFileTree(Paths.get(rawArticlesDirLocation), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    if (path.toString().matches(".*\\.phba")) {
                        rawArticlesPaths.add(path.toString());
                    }
                    return super.visitFile(path, basicFileAttributes);
                }

            });


            String pageTitle = configIni.get("Settings", "Page Title");
            String pageAuthor = configIni.get("Settings", "Page author");
            String pageBackgroundPath = configIni.get("Settings", "background path");
            String pageBio = configIni.get("Settings", "this the bio");
            String pageName = configIni.get("Settings", "this is the name");
            String pageFooter = configIni.get("Settings", "Page footer");
            String pagePreviousPost = configIni.get("Settings", "Previous Post");
            String pageNextPost = configIni.get("Settings", "Next Post");
            BufferedReader indexReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "index.html"));
            String tempString;
            StringBuilder generatedIndex = new StringBuilder();
            while ((tempString = indexReader.readLine()) != null) {
                generatedIndex
                        .append(
                                tempString
                                        .replaceAll("\\[Page Title]", pageTitle)
                                        .replaceAll("\\[Page author]", pageAuthor)
                                        .replaceAll("\\[background path]", pageBackgroundPath)
                                        .replaceAll("\\[this the bio]", pageBio)
                                        .replaceAll("\\[this is the name]", pageName)
                                        .replaceAll("\\[Page footer]", pageFooter))
                        .append(System.getProperty("line.separator"));
            }
            indexReader.close();


            for (int i = 0; i < rawArticlesPaths.size(); i++) {
                BufferedReader articleContainerReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "article container.html"));
                BufferedReader articleSinglePageReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "article.html"));
                Ini rawArticleIni = new Ini(new File(rawArticlesPaths.get(i)));
                String icon = rawArticleIni.get("Article", "Article Icon");
                String date = rawArticleIni.get("Article", "Article date");
                String title = rawArticleIni.get("Article", "Article title");
                String base64Title = Base64.getUrlEncoder().encodeToString(title.getBytes());
                String content = rawArticleIni.get("Article", "Article content");
                StringBuilder generatedPagesContainer = new StringBuilder();
                while ((tempString = articleContainerReader.readLine()) != null) {
                    generatedPagesContainer
                            .append(tempString
                                    .replaceAll("\\[Article Icon]", icon)
                                    .replaceAll("\\[Article date]", date)
                                    .replaceAll("\\[Article title]", title)
                                    .replaceAll("\\[Article Path]", "article/" + base64Title + ".html")
                                    .replaceAll("\\[Article summary]", content.length() < 60 ? content : content.substring(0, 59)))
                            .append(System.getProperty("line.separator"));
                }
                articleContainerReader.close();
                generatedPagesContainer.insert(0, "[Article Container]" + System.getProperty("line.separator"));
                generatedIndex.replace(
                        generatedIndex.indexOf("[Article Container]"),
                        generatedIndex.indexOf("[Article Container]") + 19,
                        generatedPagesContainer.toString());

                StringBuilder generatedSinglePage = new StringBuilder();
                while ((tempString = articleSinglePageReader.readLine()) != null) {
                    generatedSinglePage
                            .append(tempString
                                    .replaceAll("\\[Article Icon]", icon)
                                    .replaceAll("\\[Article date]", date)
                                    .replaceAll("\\[Article Title]", title)
                                    .replaceAll("\\[Article content]", content))
                            .append(System.getProperty("line.separator"));
                }

                if (!new File(rootDirLocation + File.separator + "article").exists())
                    new File(rootDirLocation + File.separator + "article").mkdir();

                FileWriter writer = new FileWriter(rootDirLocation + File.separator + "article" + File.separator + base64Title + ".html");
                writer.write(generatedIndex.toString());
                writer.close();
                printInfo("File:'" + base64Title + ".html', Generated.");

            }

            FileWriter writer = new FileWriter(rootDirLocation + File.separator + "index.html");
            writer.write(generatedIndex.toString());
            writer.close();
            printInfo("File:'index.html', Generated.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(System.getProperty("line.separator") + "[ERROR] " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        printInfo("Done.");
        JOptionPane.showMessageDialog(null, "Done", "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void printInfo(String info) {
        System.out.println("[INFO] " + info);
    }
}
