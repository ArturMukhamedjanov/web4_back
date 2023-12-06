package weblab4.dbmanager;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import weblab4.model.Point;

public class ConnectToDB {
    private final Properties properties = new Properties();

    private DataSource dataSource;
    private InputStream fis = ConnectToDB.class.getResourceAsStream("/config.properties");
    private String dbUrl;
    private String user;
    private String pass;

    private Connection connection;

    public void init() throws NamingException, IOException, SQLException {
        initConnection();
    }

    private void initConnection() throws NamingException, IOException, SQLException {
        //Context ctx = new InitialContext();
        //dataSource = (DataSource) ctx.lookup("java:jboss/Lab3");
        properties.load(fis);
        dbUrl = properties.getProperty("DB.URL");
        user = properties.getProperty("DB.USER");
        pass = properties.getProperty("DB.PASSWORD");
        try {
            System.out.println("initing db");
            //connection = dataSource.getConnection();
            connection = DriverManager.getConnection(dbUrl, user, pass);
            //connection = DatabaseCommunicator.getConnection();
            connection.createStatement().execute(
                    "create table if not exists results (" +
                            "x real , y real, r real, res text, token text)"
            );
            connection.createStatement().execute(
                    "create table if not exists users (" +
                            "login text , password text, token text)"
            );
        } catch (SQLException e) {
            throw e;
        }
    }

    public void addPointToTable(Point point){
        try {
            if (connection == null)
                initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO results VALUES (?, ?, ?, ?, ?)"
            );
            preparedStatement.setDouble(1,point.getX());
            preparedStatement.setDouble(2,point.getY());
            preparedStatement.setDouble(3,point.getR());
            preparedStatement.setString(4,point.getRes());
            preparedStatement.setString(5,point.getOwner());
            preparedStatement.execute();
            System.out.println("Point added");
        } catch (NamingException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<Point> getPoints(String token){
        List<Point> pointsList = new ArrayList<>();
        System.out.println("asdasd");
        try {
            if (connection == null) {
                System.out.println("null conn");
                initConnection();
            }
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM results");
            System.out.println("gor result" + rs );
            while (rs.next()) {
                System.out.println("enter");
                Point point = new Point();
                point.setX(rs.getDouble("x"));
                point.setY(rs.getDouble("y"));
                point.setR(rs.getDouble("r"));
                point.setRes(rs.getString("res"));
                if (rs.getString("token").equals(token)) {
                    point.setOwner(rs.getString("token"));
                    pointsList.add(0,point);
                }else{
                    System.out.println(token + " " + rs.getString("token"));
                }
            }
        }catch (SQLException | NamingException | IOException throwable) {
            System.out.println("ex" + throwable.getMessage());
        }
        System.out.println("Goted list " + pointsList);
        return pointsList;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String addUser(String login, String password) {
        // Проверка наличия пользователя с таким логином
        if (userExists(login)) {
            return "Логин уже используется";
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        // Генерация токена
        String token = generateToken();

        // Вставка новой записи в таблицу users
        if (insertUser(login, hashedPassword, token)) {
            return token;
        } else {
            return "Логин";
        }
    }

    private String generateToken() {
        // Генерация уникального токена
        return UUID.randomUUID().toString();
    }

    public boolean userExists(String login) {
        // Проверка наличия пользователя в базе данных
        try {
            String query = "SELECT * FROM users WHERE login=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userValid(String login, String password) {
        try {
            String query = "SELECT * FROM users WHERE login=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Retrieve the hashed password from the database
                String hashedPasswordFromDB = resultSet.getString("password");

                // Use BCrypt to verify the entered password against the hashed password
                return BCrypt.checkpw(password, hashedPasswordFromDB);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean insertUser(String user, String password, String token) {
        // Вставка новой записи в базу данных
        try {
            String query = "INSERT INTO users (login, password, token) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, token);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTokenForUser(String user) {
        // Получение токена из базы данных для пользователя
        try {
            String query = "SELECT token FROM users WHERE login=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("token");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkToken(String token){
        try {
            String query = "SELECT * FROM users WHERE token=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Вернет true, если токен найден в базе данных
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clear(String token) {
        try {
            if (connection == null)
                initConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM results WHERE token = ?"
            );
            preparedStatement.setString(1, token);
            preparedStatement.executeUpdate();

        } catch (NamingException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }*/
}