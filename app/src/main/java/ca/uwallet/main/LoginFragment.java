package ca.uwallet.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ca.uwallet.main.sync.LoginService;

/**
 * Standard fragment with 2 input fields and a button with listener attached
 * @author Seikun
 */

public class LoginFragment extends Fragment implements OnClickListener {

    private EditText usernameView;
    private EditText passwordView;
    private View formView;
    private View statusView;
    private TextView statusMessageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, container,
				false);
        usernameView = (EditText) v.findViewById(R.id.username_input);
        // TODO restore last username
        passwordView = (EditText) v.findViewById(R.id.password_input);
        formView = v.findViewById(R.id.login_form);
        statusView = v.findViewById(R.id.login_status);
        statusMessageView = (TextView) v.findViewById(R.id.login_status_message);
		v.findViewById(R.id.login_button).setOnClickListener(this);
        setHasOptionsMenu(true);
		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.login, menu);
    }
    
    

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.login_button:
            login();
			break;
		}
	}

    private void login() {
        Intent intent = new Intent(getActivity(), LoginService.class);
        intent.putExtra(LoginService.USERNAME, usernameView.getText().toString());
        intent.putExtra(LoginService.PASSWORD, usernameView.getText().toString());
        getActivity().startService(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            statusView.setVisibility(View.VISIBLE);
            statusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            statusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            formView.setVisibility(View.VISIBLE);
            formView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            formView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            statusView.setVisibility(show ? View.VISIBLE : View.GONE);
            formView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showToast(String text, int duration){
        Toast.makeText(getActivity(), text, duration).show();
    }

    public void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

}
