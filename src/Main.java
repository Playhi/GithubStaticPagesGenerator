import org.ini4j.Ini;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "generateonenewrawarticle"://Generate One New Raw Article
                case "gonra":
                    generateOneNewEmptyRawArticle();
                    break;
                default:
                    generatePages();
                    break;
            }
        } else
            generatePages();
    }

    private static void printInfo(String info) {
        System.out.println("[INFO] " + info);
    }

    private static void generateOneNewEmptyRawArticle() {
        String rootDirLocation = System.getProperty("user.dir");
        String rawArticlesDirLocation = rootDirLocation + File.separator + "raw article";
        if (!new File(rawArticlesDirLocation).exists()) {
            new File(rawArticlesDirLocation).mkdir();
        }
        Date date = new Date();
        File newRawArticle = new File(rawArticlesDirLocation + File.separator + Long.toString(date.getTime()) + ".phba");
        try {
            if (!newRawArticle.exists()) {
                newRawArticle.createNewFile();
            }
            Ini newRawArticleIni = new Ini(newRawArticle);
            newRawArticleIni.put("Article", "Article Header", "");
            newRawArticleIni.put("Article", "Article Icon", "");
            newRawArticleIni.put("Article", "Article date", new SimpleDateFormat("yyyyMMdd").format(date));
            newRawArticleIni.put("Article", "Article title", "Title");
            newRawArticleIni.put("Article", "Page author", "");
            newRawArticleIni.put("Article", "Article content", "Content");
            newRawArticleIni.store();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(System.getProperty("line.separator") + "[ERROR] " + e.getMessage());
        }
        printInfo("Done.");
    }

    private static void generatePages() {
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


            ArrayList<Path> rawArticlesPaths = new ArrayList<>();

            printInfo("Copy some files.");
            Files.walkFileTree(Paths.get(rootDirLocation + File.separator + "template" + File.separator + "source"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    Path destination = Paths.get(path.toAbsolutePath().toString().replace(File.separator + "template" + File.separator + "source", File.separator));
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
                        rawArticlesPaths.add(path);
                    }
                    return super.visitFile(path, basicFileAttributes);
                }

            });


            String pageTitle = configIni.get("Settings", "Page Title");
            String pageAuthor = configIni.get("Settings", "Page author");
            String pageDescription = configIni.get("Settings", "Page description");
            String pageKeywords = configIni.get("Settings", "Page keywords");
            String pageBackgroundPath = configIni.get("Settings", "background path");
            String pageBio = configIni.get("Settings", "this the bio");
            String pageName = configIni.get("Settings", "this is the name");
            String pageFooter = configIni.get("Settings", "Page footer");
            String pagePreviousPost = configIni.get("Settings", "Previous Post");
            String pageNextPost = configIni.get("Settings", "Next Post");
            String globalHeader = configIni.get("Settings", "Global Header");
            String singleHeader = configIni.get("Settings", "Single Header");
            BufferedReader indexReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "index.html"));
            String tempString;
            StringBuilder generatedIndex = new StringBuilder();
            while ((tempString = indexReader.readLine()) != null) {
                generatedIndex
                        .append(
                                tempString
                                        .replaceAll("\\[Page keywords]", pageKeywords == null ? "" : pageKeywords)
                                        .replaceAll("\\[Page description]", pageDescription == null ? "" : pageDescription)
                                        .replaceAll("\\[Page Title]", pageTitle == null ? "" : pageTitle)
                                        .replaceAll("\\[Page author]", pageAuthor == null ? "" : pageAuthor)
                                        .replaceAll("\\[background path]", pageBackgroundPath == null ? "" : pageBackgroundPath)
                                        .replaceAll("\\[this the bio]", pageBio == null ? "" : pageBio)
                                        .replaceAll("\\[this is the name]", pageName == null ? "" : pageName)
                                        .replaceAll("\\[Page footer]", pageFooter == null ? "" : pageFooter)
                                        .replaceAll("\\[Global Header]", globalHeader == null ? "" : globalHeader)
                        )
                        .append(System.getProperty("line.separator"));
            }
            indexReader.close();

            Collections.sort(rawArticlesPaths);

            for (int i = 0; i < rawArticlesPaths.size(); i++) {
                BufferedReader articleContainerReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "article container.html"));
                BufferedReader articleSinglePageReader = new BufferedReader(new FileReader(rootDirLocation + File.separator + "template" + File.separator + "article.html"));
                Ini rawArticleIni = new Ini(new File(rawArticlesPaths.get(i).toString()));
                String icon = rawArticleIni.get("Article", "Article Icon");
                String date = rawArticleIni.get("Article", "Article date");
                String title = rawArticleIni.get("Article", "Article title");
                String author = rawArticleIni.get("Article", "Page author");
                String articleHeader = rawArticleIni.get("Article", "Article Header");
                String generatedFileName = rawArticlesPaths.get(i).getFileName().toString();
                if (generatedFileName.contains(".")) {
                    generatedFileName = generatedFileName.substring(0, generatedFileName.lastIndexOf("."));
                }
                String content = rawArticleIni.get("Article", "Article content");
                if (content == null)
                    content = "";
                StringBuilder generatedPagesContainer = new StringBuilder();
                String singleDescription = content.trim().replaceAll("<[^>]+>", "").replaceAll("&nbsp;", "");
                singleDescription = singleDescription.length() <= 150 ? singleDescription : singleDescription.substring(0, 150) + "...";
                while ((tempString = articleContainerReader.readLine()) != null) {
                    generatedPagesContainer
                            .append(tempString
                                    .replaceAll("\\[Article Icon]", icon == null ? "" : icon)
                                    .replaceAll("\\[Article date]", date == null ? "" : date)
                                    .replaceAll("\\[Article title]", title == null ? "" : title)
                                    .replaceAll("\\[Article Path]", "article/" + generatedFileName + ".html")
                                    .replaceAll("\\[Article summary]", singleDescription)
                            )
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
                    String previousPostUrl = i == 0 ? "#" : rawArticlesPaths.get(i - 1).getFileName().toString();
                    if (!"#".equals(previousPostUrl)) {
                        previousPostUrl =
                                previousPostUrl.contains(".") ?
                                        previousPostUrl.substring(0, previousPostUrl.lastIndexOf(".")) + ".html" :
                                        previousPostUrl + ".html";
                    }
                    String nextPostUrl = (i + 1 == rawArticlesPaths.size()) ? "#" : rawArticlesPaths.get(i + 1).getFileName().toString();
                    if (!"#".equals(nextPostUrl)) {
                        nextPostUrl =
                                nextPostUrl.contains(".") ?
                                        nextPostUrl.substring(0, nextPostUrl.lastIndexOf(".")) + ".html" :
                                        nextPostUrl + ".html";
                    }
                    generatedSinglePage
                            .append(tempString
                                    .replaceAll("\\[Page author]", author == null ? "" : author)
                                    .replaceAll("\\[Page keywords]", title == null ? "" : title)
                                    .replaceAll("\\[Page description]", singleDescription)
                                    .replaceAll("\\[Article Icon]", icon == null ? "" : icon)
                                    .replaceAll("\\[Article date]", date == null ? "" : date)
                                    .replaceAll("\\[Article Title]", title == null ? "" : title)
                                    .replaceAll("\\[Article content]", content)
                                    .replaceAll("\\[Page footer]", pageFooter == null ? "" : pageFooter)
                                    .replaceAll("\\[Global Header]", globalHeader == null ? "" : globalHeader)
                                    .replaceAll("\\[Single Header]", singleHeader == null ? "" : singleHeader)
                                    .replaceAll("\\[Article Header]", articleHeader == null ? "" : articleHeader)
                                    .replaceAll("\\[Previous Post Img]", i == 0 ? "" : new Ini(rawArticlesPaths.get(i - 1).toFile()).get("Article", "Article Icon"))
                                    .replaceAll("\\[Previous Post Url]", previousPostUrl)
                                    .replaceAll("\\[Previous Post]", pagePreviousPost)
                                    .replaceAll("\\[Previous Post Title]", i == 0 ? "没有了" : new Ini(rawArticlesPaths.get(i - 1).toFile()).get("Article", "Article title"))
                                    .replaceAll("\\[Next Post Img]", i + 1 == rawArticlesPaths.size() ? "" : new Ini(rawArticlesPaths.get(i + 1).toFile()).get("Article", "Article Icon"))
                                    .replaceAll("\\[Next Post Url]", nextPostUrl)
                                    .replaceAll("\\[Next Post]", pageNextPost)
                                    .replaceAll("\\[Next Post Title]", i + 1 >= rawArticlesPaths.size() ? "没有了" : new Ini(rawArticlesPaths.get(i + 1).toFile()).get("Article", "Article title")))
                            .append(System.getProperty("line.separator"));//[article background path]
                }

                if (!new File(rootDirLocation + File.separator + "article").exists())
                    new File(rootDirLocation + File.separator + "article").mkdir();

                FileWriter writer = new FileWriter(rootDirLocation + File.separator + "article" + File.separator + generatedFileName + ".html");
                writer.write(generatedSinglePage.toString());
                writer.close();
                printInfo("File:'" + generatedFileName + ".html', Generated.");

            }

            FileWriter writer = new FileWriter(rootDirLocation + File.separator + "index.html");
            writer.write(generatedIndex.toString().replaceAll("\\[Article Container]", ""));
            writer.close();
            printInfo("File:'index.html', Generated.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(System.getProperty("line.separator") + "[ERROR] " + e.getMessage());
//            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        printInfo("Done.");
//        JOptionPane.showMessageDialog(null, "Done", "Done", JOptionPane.INFORMATION_MESSAGE);
    }
}
