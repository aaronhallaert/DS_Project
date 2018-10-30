package client;

public class User {
    private static User currentUser;
    private String username;
    private String token;


    private User(){}

    public static User getCurrentUser(){
        if(currentUser==null){
            synchronized (User.class){
                if(currentUser==null){
                    currentUser= new User();
                }
            }
        }
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
