package models;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileManager {

    public static final String IMAGES_FOLDER = "./images/";
    public static final String PROCESSED_IMAGES_FOLDER = "./imagesResult/";
    private static ArrayList<Coordinate> coordinates;
    private Coordinate[] randomCoordinates;

    public String[] readFilesFromFolder() {
        File folder = new File(IMAGES_FOLDER);
        return folder.list();
    }

    public void transformImageSequential(int totalPoints) throws IOException {
        // deleteFiles();
        long startTime = System.currentTimeMillis();
        String[] images = readFilesFromFolder();
        for (String image : images) {
            try {
                System.out.println("Procesando image:" + image);
                changeImageColor(new File(IMAGES_FOLDER + image), totalPoints);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Tiempo de Ejecución: " + (System.currentTimeMillis() - startTime) / 1000);
    }

    private void deleteFiles() throws IOException {
        Files.walk(Paths.get(PROCESSED_IMAGES_FOLDER))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public void changeImageColor(File file, int totalPoints) throws IOException {
        coordinates = new ArrayList<>();
        BufferedImage image = ImageIO.read(file);
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));
                int average = (color.getBlue() + color.getRed() + color.getGreen()) / 3;
                if (average < 127) {
                    newImage.setRGB(i, j, Color.BLACK.getRGB());
                    coordinates.add(new Coordinate(i, j));
                } else {
                    newImage.setRGB(i, j, Color.WHITE.getRGB());
                }
            }
        }
        calculateCenter(totalPoints, image, newImage);
        ImageIO.write(newImage, "png", new File(PROCESSED_IMAGES_FOLDER + file.getName()));
    }

    private void calculateCenter(int totalPoints, BufferedImage image, BufferedImage newImage) {
        generateRandomPoints(image.getWidth(), image.getHeight(), totalPoints);
        paintRandomPoints(newImage);
        ArrayList<Coordinate> countAux = countPointsIn();
        System.out.println("Puntos internos: " + countAux.size());
        Coordinate center = average(countAux);
        System.out.println("Punto central: "+ center);

        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setColor(Color.decode("#87EF19"));
        g2.fillOval(center.getX()-5, center.getY()-5, 10,10);
        newImage.setRGB(center.getX(), center.getY(), Color.WHITE.getRGB());
    }

    public Coordinate[] generateRandomPoints(int width, int height, int totalPoints) {
        randomCoordinates = new Coordinate[totalPoints];
        for (int i = 0; i < totalPoints; i++) {
            randomCoordinates[i] = new Coordinate((int) (Math.random() * width), (int) (Math.random() * height));
        }
        return randomCoordinates;
    }

    public void paintRandomPoints(BufferedImage image) {
        for (int i = 0; i < randomCoordinates.length; i++) {
            System.out.println("X: " + randomCoordinates[i].getX() + " y: " + randomCoordinates[i].getY());
            image.setRGB(randomCoordinates[i].getX(), randomCoordinates[i].getY(), Color.MAGENTA.getRGB());
        }
    }

    public ArrayList<Coordinate> countPointsIn() {
        ArrayList<Coordinate> pointsIn = new ArrayList<>();
        for (int i = 0; i < randomCoordinates.length; i++) {
            for (int j = 0; j < coordinates.size(); j++) {
                if (randomCoordinates[i].getX() == coordinates.get(j).getX() && randomCoordinates[i].getY() == coordinates.get(j).getY()) {
                    pointsIn.add(new Coordinate(randomCoordinates[i].getX(), randomCoordinates[i].getY()));
                }
            }
        }
        return pointsIn;
    }

    public Coordinate average(ArrayList<Coordinate> pointsIn) {
        int x = 0;
        int y = 0;
        for (Coordinate coordinate : pointsIn) {
            x += coordinate.getX();
            y += coordinate.getY();
        }
        return new Coordinate(x / pointsIn.size(), y / pointsIn.size());
    }

    public static void main(String[] args) {
        FileManager fm = new FileManager();
        try {
            fm.transformImageSequential(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
