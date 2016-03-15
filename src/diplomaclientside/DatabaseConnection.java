package diplomaclientside;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author egg
 */

//konstruktorban adatbázis kapcsolódáshoz szükséges adatok beállítása
public class DatabaseConnection {
    private String host = "jdbc:derby://109.110.143.103:1527//home/server/.netbeans-derby/DiplomaOddsDatabase";
    private String user = "diploma";
    private String pass = "diploma";
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    
    public DatabaseConnection(String host, int port, String user, String password){
        this.host = "jdbc:derby://" + host +":" + port + "//home/server/.netbeans-derby/DiplomaOddsDatabase";
        this.user = user;
        this.pass = password;
    }
    
    //paraméterként megadott táblához kapcsolódás (a táblát adja vissza ResultSet-ként)
    public ResultSet openConnection(String table) throws SQLException{
        con = DriverManager.getConnection(host, user, pass);
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        String SQL = "SELECT * FROM " + table;
        rs = stmt.executeQuery(SQL);
        return rs;
    }
    
    public void setConnection(String host, int port, String user, String password) throws SQLException{
        this.host = "jdbc:derby://" + host +":" + port + "//home/server/.netbeans-derby/DiplomaOddsDatabase";
        this.user = user;
        this.pass = password;
        this.closeConnection();
        this.openConnection();
    }
    
    //kapcsolat megnyitása
    public void openConnection() throws SQLException{
        con = DriverManager.getConnection(host, user, pass);
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    
    //SQL lekérdezés végrehajtása
    public ResultSet executeCommand(String command) throws SQLException{
        this.openConnection();
        ResultSet rs = stmt.executeQuery(command);
        return rs;
    }
    
    //kapcsolat bezárása
    public void closeConnection() throws SQLException {
        con.close();
        stmt.close();
        if (rs != null) {
            rs.close();
        }
    }
    
    //egy adott táblából az összes rekord törlése
    public void deleteAllRows(String table) throws SQLException{
        String SQL = "DELETE FROM DIPLOMA." + table;
        stmt.execute(SQL);
    }
    
    //egy adott táblából megadott feltétel szerinti rekordok törlése
    public void deleteSpecificRows(String table, String condition) throws SQLException{
        String SQL = "DELETE FROM DIPLOMA." + table + " WHERE " + condition;
        System.out.println(SQL);
        stmt.execute(SQL);
    }
}
