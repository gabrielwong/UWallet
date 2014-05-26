package ca.uwallet.main;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity{
	/**
	 * The extra describing the default username to populate the field with.
	 */
	public static final String EXTRA_USERNAME = "ca.uwallet.main.extra.USERNAME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            LoginFragment fragment = new LoginFragment();
            fragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.login_fragment_container, fragment).commit();
        }
    }
}
