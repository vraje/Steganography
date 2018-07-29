package minor2;

import java.awt.image.BufferedImage;

import java.util.ArrayDeque;
import java.util.Deque;


public class Decode{
    String decode1(BufferedImage input, int width, int height) {

        if(!Steg.isEncoded(input, width, height)) {
            return null;
        }

        int msgLength = Steg.getEncodedLength(input, width, height);

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();

        int pixel, temp, charOut, ignore = 0, count = 0;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(ignore < 36 + 4*(String.valueOf(msgLength).length()+1)) {
                    ignore++;
                    continue;
                }

                if(count++ == 4*msgLength) {
                    break loop;
                }
                pixel = input.getRGB(i, j); //grab RGB value at specified pixel
                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if(listChar.size() >=4) { //once we have 8 bits of data extracted
                    //combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop() ;
                    decodedMsg.append((char)charOut);
                }
            }

        }

        String outputMsg = new String(decodedMsg); //generate extracted message

        return outputMsg;
    }


}//end of decode()