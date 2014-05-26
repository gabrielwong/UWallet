package ca.uwallet.main;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.Serializable;

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
    private BroadcastReceiver receiver;
    private AccountAuthenticatorResponse accountAuthenticatorResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        accountAuthenticatorResponse =
                getActivity().getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onLoginResult(intent);
            }
        };
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.registerReceiver(receiver, LoginService.FILTER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, null, true);
        usernameView = (EditText) v.findViewById(R.id.username_input);
        passwordView = (EditText) v.findViewById(R.id.password_input);
        formView = v.findViewById(R.id.login_form);
        statusView = v.findViewById(R.id.login_status);
        statusMessageView = (TextView) v.findViewById(R.id.login_status_message);
		v.findViewById(R.id.login_button).setOnClickListener(this);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LoginActivity.EXTRA_USERNAME)){
                usernameView.setText(savedInstanceState.getCharSequence(LoginActivity.EXTRA_USERNAME));
            }
        }
		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.login, menu);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.unregisterReceiver(receiver);
    }

    public void onLoginResult(Intent intent) {
        Serializable result = intent.getSerializableExtra(LoginService.RESULT);
        if (!(result instanceof LoginService.Result)) {
            return;
        }
        switch((LoginService.Result)result) {
            case SUCCESS:
                Bundle bundle = intent.getExtras();
                if (accountAuthenticatorResponse != null) {
                    accountAuthenticatorResponse.onResult(bundle);
                }
                getActivity().finish();
                break;
            case INVALID_CREDENTIALS:
                showToast(getString(R.string.error_incorrect_credentials), Toast.LENGTH_SHORT);
                break;
            case CONNECTION_ERROR:
                showToast(getString(R.string.error_connection_failed), Toast.LENGTH_SHORT);
                break;
        }
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
        intent.putExtra(LoginService.PASSWORD, passwordView.getText().toString());
        getActivity().startService(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
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
    }

    private void showToast(String text, int duration){
        Toast.makeText(getActivity(), text, duration).show();
    }

    public void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

}
