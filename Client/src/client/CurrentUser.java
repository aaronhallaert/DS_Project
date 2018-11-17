package client;

public class CurrentUser {
    private static CurrentUser currentUser;
    private String username;
    private String token;


    private CurrentUser(){}

    public static CurrentUser getInstance(){
        if(currentUser==null){
            synchronized (CurrentUser.class){
                if(currentUser==null){
                    currentUser= new CurrentUser();
                }
            }
        }
        return currentUser;
    }

    public static void setCurrentUser(CurrentUser currentUser) {
        CurrentUser.currentUser = currentUser;
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
