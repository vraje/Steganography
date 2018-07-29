package minor2;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.util.Deque;

public class Steg {
    static Connection conn = Connect.getConnection();


    static Scanner scan = new Scanner(System.in);

    static void init() throws SQLException {

        Statement st=st = conn.createStatement();

        if(!tableExist("inputimages")) {

            st.execute("CREATE TABLE IF NOT EXISTS inputimages(" +
                    "id NUMBER PRIMARY KEY ," +
                    "filename text," +
                    "image blob )");


            addImageToDB(st, "inputimages");

            System.out.println("All the images in input_images folder added to the DB");
        }
        if(!tableExist("outputimages")) {
            st.execute("CREATE TABLE IF NOT EXISTS outputimages(" +
                    "id NUMBER PRIMARY KEY ," +
                    "filename text," +
                    "image blob )");

            addImageToDB(st, "outputimages");

            System.out.println("All the images in input_images folder added to the DB");

        }

    }

    static boolean tableExist(String table){

        try {
            ResultSet rs = conn.createStatement().executeQuery("select * from "+table);
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
        return true;

    }




    public static void main(String args[]) throws SQLException {

        init();

        System.out.println("Enter 1 to encode image ");
        System.out.println("Enter 2 to decode image ");
        int check = scan.nextInt();

        try {

            if (check == 1) {

                listImages("inputimages");

                System.out.println("Enter image number");
                int imgNumber = scan.nextInt();

                ResultSet rs = conn.createStatement().executeQuery("select image from inputimages where id="+imgNumber);

                String encname = rs.getString("image");

                System.out.println("\n ######ENCODING THE IMAGE####### \n");
                BufferedImage imageobj = ImageIO.read(new File(encname));
                BufferedImage imageout = ImageIO.read(new File(encname));//just to put some data in the imageout variable
                int imgHe = imageobj.getHeight();
                int imgWi = imageobj.getWidth();
                System.out.println("\n The height of the image that is being encoded is:" + imgHe);
                System.out.println("\n The width of the image that is being encoded is:" + imgWi + "\n");

                System.out.println("Write a name for the encoded image:\n");

                String out_text = scan.next();
                scan.nextLine();

                System.out.println("Enter the message to be encoded:\n");


                String text = scan.nextLine();
                Encode s1 = new Encode();

                s1.encode1(imageobj, imageout, imgHe, imgWi, text, out_text);


                rs = conn.createStatement().executeQuery("select * from outputimages");
                int id=0;

                while (rs.next()) id++;


                System.out.println("id - "+id);

                File f = new File("output_images/"+out_text+".png");


                conn.createStatement().execute("INSERT INTO outputimages values(" + id + ", '" + f.getName() + "', '" + f.getAbsolutePath() + "')");

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
        if (check == 2) {
            try {

                listImages("outputimages");

                System.out.println("Enter image number");
                int imgNumber = scan.nextInt();

                ResultSet rs = conn.createStatement().executeQuery("select image from outputimages where id="+imgNumber);

                String decname = rs.getString("image");


                System.out.println("\n ######DECODING THE IMAGE####### \n");
                System.out.println("Loading the encoded image:\n");
                BufferedImage imageobj2 = ImageIO.read(new File(decname));
                int height_en = imageobj2.getHeight();
                int width_en = imageobj2.getWidth();
                System.out.println("The height and width of the encoded image is:" + height_en + "  and  " + width_en);
                Decode s2 = new Decode();
                String ea = s2.decode1(imageobj2, height_en, width_en);
                System.out.println(ea);
            } catch (IOException e) {
                System.out.println(e.getMessage());

            }
        }
    }

    private static void listImages(String table) {

        Statement st = null;
        ResultSet rs = null;

        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from "+table);

            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getString("filename"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static void addImageToDB(Statement st,String table) {
        File file;

        if(table.equals("inputimages"))
            file = new File("input_images");
        else
            file = new File("output_images");

        int id = 0;
        try {
            for (File f : file.listFiles()) {
                st.execute("INSERT INTO "+table+" values(" + id + ", '" + f.getName() + "', '" + f.getAbsolutePath() + "')");
                id++;
            }

        } catch (SQLException e) {

        }

    }


    static boolean isEncoded(BufferedImage input, int width, int height) { //Check for "!encoded!" at starting

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();

        int pixel, temp, charOut, count = 0;
        loop:
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++, count++) {

                if (count == 45) { //remain in loop till first 9 characters are extracted
                    break loop;
                }
                pixel = input.getRGB(i, j); //grab RGB value at specified pixel
                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if (listChar.size() >= 4) { //once we have 8 bits of data extracted
                    //combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop();
                    decodedMsg.append((char) charOut); //else add character to a StringBuffer
                    count++;


                }
            }
        }

        String check = new String(decodedMsg);

        if (check.compareTo("!encoded!") == 0) {
            System.out.println("\n The image is encoded with the message :) \n");
            return true;
        } else {
            return false;
        }

    } //end of isEncoded() method

    static int getEncodedLength(BufferedImage input, int width, int height) { //method to get actual length of message encoded

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();

        int pixel, temp, charOut, count = 0;
        loop:
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (count < 36) { //ignore the 36 bits or 9 bytes, equal to "!encoded!"
                    count++;
                    continue;
                }

                pixel = input.getRGB(i, j); //grab RGB value at specified pixel


                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if (listChar.size() >= 4) { //once we have 8 bits of data extracted
                    //combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop();
                    if ((char) charOut == '!') { //terminate process if character extracted is '!'
                        break loop;
                    } else {
                        decodedMsg.append((char) charOut); //else add character to a StringBuffer
                    }
                }
            }

        }

        String length = new String(decodedMsg);
        System.out.println("Length of the message is " + Integer.parseInt(length));

        return Integer.parseInt(length);
    } //end of getEncodedLength() method


}
