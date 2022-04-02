package me.unp0wnable.groupeo.utils;

import lombok.experimental.UtilityClass;
import me.unp0wnable.groupeo.model.constants.UserRoles;
import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.exceptions.InstanceAlreadyExistsException;
import me.unp0wnable.groupeo.model.services.UserService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Clase con métodos auxiliares para la ejecución de los test */
@UtilityClass
public class TestDataGenerator {
    // INFO: https://stackoverflow.com/questions/64580412/generate-valid-deterministic-uuids-for-tests
    public static final UUID NON_EXISTENT_USER_ID = UUID.fromString("00000000-0000-4000-8000-000000000000");
    public static final UUID NON_EXISTENT_ADDRESS_ID = UUID.fromString("00000001-0000-4000-8000-000000000000");
    public static final String NON_EXISTENT_NICKNAME = "NON_EXISTENT_NICKNAME";
    public static final String DEFAULT_NICKNAME = "nickName";
    public static final String DEFAULT_PASSWORD = "password";
    
    
    public static UserAddress generateValidAddressForUser(User user) {
        UserAddress address = new UserAddress();
        address.setCity("A Coruña");
        address.setRegion("Galicia");
        address.setPostalCode("15000");
        address.setCountry("España");
        address.setUser(user);
        
        return address;
    }
    
    public static User generateValidUser(String nickName) {
        User user = new User();
        //user.setUserID(UUID.fromString("00000001-0000-4000-8000-000000000001"));
        user.setFirstName("FirstName");
        user.setSurname1("Surname1");
        user.setSurname2("Surname2");
        user.setEmail(nickName.toLowerCase() + "@groupeo.es");
        user.setBirthDate(parseDate("2022-02-01"));
        user.setJoinDate(Calendar.getInstance().getTime());
        user.setDescription("A brief description of myself");
        user.setNickName(nickName);
        user.setPassword(DEFAULT_PASSWORD);
        user.setImageB64(null);
        user.setScore((float) 0);
        user.setRole(UserRoles.USER);
        
        return user;
    }
    
    public static User generateAdmin() {
        User admin = generateValidUser("ADMIN");
        admin.setJoinDate(parseDate("2022-02-02"));
        admin.setDescription("I am the administrator of the system");
        admin.setRole(UserRoles.ADMIN);
        
        return admin;
    }
    
    
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch ( ParseException e) {
            return null;
        }
    }
    
    public static User registerValidUser(String nickName, UserService service) throws InstanceAlreadyExistsException {
        User user = generateValidUser(nickName);
        
        return service.signUp(user);
    }
    
    public static List<User> registerMultipleUsers(int ammountOfUsers, UserService service) throws InstanceAlreadyExistsException {
        List<User> listOfUsers = new ArrayList<User>();
        
        for ( int i=1; i <= ammountOfUsers; i++ ) {
            User auxUser = registerValidUser("Target" + i, service);
            listOfUsers.add(auxUser);
        }
        
        return listOfUsers;
    }
}
