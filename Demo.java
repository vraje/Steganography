package minor2;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Demo {
    public static void main(String[] args) {
        //addImageToDB();

        Connection conn = Connect.getConnection();

        Statement st = null;
        ResultSet rs = null;

        try {
            st = conn.createStatement();
            rs = st.executeQuery("select name from inputimages;");

        } catch (SQLException e) {
            try {
                st.execute("CREATE TABLE inputimages(" +
                        "id number pk ," +
                        "filename text," +
                        "image blob )");

                addImageToDB(st);

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        System.out.printf(String.valueOf(rs));

    }

    private static void addImageToDB(Statement st) {
        File file = new File("input_images");
        //System.out.println(file.getAbsolutePath());
        int id=1;
        try{
            for(File f : file.listFiles()){
                st.execute("INSERT INTO inputimages values("+id+", '"+f.getName()+"', '"+f.getAbsolutePath()+"')");
            }

        }
        catch (SQLException e){

        }

    }
}
