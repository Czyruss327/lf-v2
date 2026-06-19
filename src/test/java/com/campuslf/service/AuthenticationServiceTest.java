import com.campuslf.service.AuthenticationService;

public static void main(String[] args) {

    AuthenticationService service =
            new AuthenticationService();

    boolean success =
            service.login(
                    "test_admin",
                    "plain_password_but_hash_in_real"
            );

    System.out.println(success);
}