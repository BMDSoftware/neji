/*
 * Copyright (c) 2016 BMD Software and University of Aveiro.
 *
 * Neji is a flexible and powerful platform for biomedical information extraction from text.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it.
 * However, you may not use it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package pt.ua.tm.neji.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Add users script.
 * 
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class AddUsers {
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:neji.db");
        
        // Add role
        PreparedStatement statement;        
        String query = "INSERT INTO roles(role) VALUES (?)";
        statement = connection.prepareStatement(query);

        statement.setString(1, "admin");

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
        
        // Add user admin
        query = "INSERT INTO users(username, pwd) VALUES (?,?)";
        statement = connection.prepareStatement(query);

        statement.setString(1, "admin");
        statement.setString(2, "MD5:21232f297a57a5a743894a0e4a801fc3");

        statement.addBatch();
        statement.executeBatch();
        statement.close();
        
        // Add user bmd
        query = "INSERT INTO users(username, pwd) VALUES (?,?)";
        statement = connection.prepareStatement(query);

        statement.setString(1, "bmd");
        statement.setString(2, "MD5:d160df71b03287399ad6984a847a07cb");

        statement.addBatch();
        statement.executeBatch();
        statement.close();
        
        // Add user role (admin - admin)
        query = "INSERT INTO user_roles(user_id, role_id) VALUES (?,?)";
        statement = connection.prepareStatement(query);

        statement.setInt(1, 1);
        statement.setInt(2, 1);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
        
        // Add user role (bmd - admin)
        query = "INSERT INTO user_roles(user_id, role_id) VALUES (?,?)";
        statement = connection.prepareStatement(query);

        statement.setInt(1, 2);
        statement.setInt(2, 1);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
}
