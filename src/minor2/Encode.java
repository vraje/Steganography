package minor2;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;


public class Encode{

    boolean encode1(BufferedImage input, BufferedImage output, int width, int height, String msg, String outputName) {


        int msgLength = msg.length();

        String message = "!encoded!" + msgLength + "!" + msg;

        msgLength = message.length();

        int[] twoBitMessage = new int[4 * msgLength];

        char currentChar;
        for(int i =0; i < msgLength ; i++)
        {
            currentChar = message.charAt(i);

            twoBitMessage[4*i + 0] = (currentChar >> 6) & 0x3;

            twoBitMessage[4*i + 1] = (currentChar >> 4) & 0x3;

            twoBitMessage[4*i + 2] = (currentChar >> 2) & 0x3;

            twoBitMessage[4*i + 3] = (currentChar)      & 0x3;

        }

        int pixel, pixOut, count = 0;;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(count < 4*msgLength) {
                    pixel = input.getRGB(i, j);

                    pixOut = (pixel & 0xFFFFFFFC) | twoBitMessage[count++];

                    output.setRGB(i, j, pixOut);

                } else {
                    break loop;
                }
            }

        }

        try {

            ImageIO.write(output, "png", new File("output_images/"+outputName+".png"));
            return true;
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return false;
        }
    }
}//end of encode()