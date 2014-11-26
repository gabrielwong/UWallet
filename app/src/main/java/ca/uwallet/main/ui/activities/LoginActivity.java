package ca.uwallet.main.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import ca.uwallet.main.R;
import ca.uwallet.main.ui.fragments.LoginFragment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity {
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
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.login_fragment_container, fragment).commit();
        }
    }
}
