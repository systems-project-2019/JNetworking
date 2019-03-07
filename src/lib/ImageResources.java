package lib;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageResources {

    private static Image juntaBackground = new Image("/img/junta_background.jpg");

    public static Image getJuntaBackground() {
        return juntaBackground;
    }

    /**
     * Title: stackoverflow.com
     * Authors: trichetriche & Josh Hilbert
     * Availability: https://stackoverflow.com/questions/32781362/centering-an-image-in-an-imageview
     *
     * Centers an image in an ImageView
     * @param imageView image view which contains image to be centered
     */
    public static void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reduceCoef;
            if(ratioX >= ratioY) {
                reduceCoef = ratioY;
            } else {
                reduceCoef = ratioX;
            }

            w = img.getWidth() * reduceCoef;
            h = img.getHeight() * reduceCoef;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);

        }
    }
}
