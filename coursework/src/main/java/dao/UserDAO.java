package dao;
import java.sql.SQLException;
import model.User;

public interface UserDAO {
    boolean addUser(User user) throws SQLException;
    User getUserByEmail(String email);
}
