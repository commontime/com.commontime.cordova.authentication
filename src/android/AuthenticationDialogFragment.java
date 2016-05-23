/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.commontime.plugin;

import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class AuthenticationDialogFragment extends DialogFragment
        implements AuthenticationUiHelper.Callback {

    private static final String TAG = "AuthDialog";
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

    private Button mCancelButton;
    private Button mSecondDialogButton;
    private View mFingerprintContent;

    private Stage mStage = Stage.FINGERPRINT;

    private KeyguardManager mKeyguardManager;
    private FingerprintManager.CryptoObject mCryptoObject;
    private AuthenticationUiHelper mFingerprintUiHelper;
    AuthenticationUiHelper.AuthenticationUiHelperBuilder mFingerprintUiHelperBuilder;

    public AuthenticationDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);

        mKeyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        mFingerprintUiHelperBuilder = new AuthenticationUiHelper.AuthenticationUiHelperBuilder(
                getContext(), getContext().getSystemService(FingerprintManager.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = null;

        if (mStage == Stage.BACKUP)
        {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            int auth_dialog_container_id = getResources()
                    .getIdentifier("auth_dialog_container_no_fingerprint", "layout",
                            Authentication.packageName);
            v = inflater.inflate(auth_dialog_container_id, container, false);

            showStandardAuthScreen();
        }
        else
        {
            int fingerprint_auth_dialog_title_id = getResources()
                    .getIdentifier("fingerprint_auth_dialog_title", "string",
                            Authentication.packageName);
            getDialog().setTitle(getString(fingerprint_auth_dialog_title_id));
            int fingerprint_dialog_container_id = getResources()
                    .getIdentifier("auth_dialog_container", "layout",
                            Authentication.packageName);
            v = inflater.inflate(fingerprint_dialog_container_id, container, false);
            int cancel_button_id = getResources()
                    .getIdentifier("cancel_button", "id", Authentication.packageName);
            mCancelButton = (Button) v.findViewById(cancel_button_id);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            int second_dialog_button_id = getResources()
                    .getIdentifier("second_dialog_button", "id", Authentication.packageName);
            mSecondDialogButton = (Button) v.findViewById(second_dialog_button_id);
            mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToBackup();
                }
            });
            int fingerprint_container_id = getResources()
                    .getIdentifier("fingerprint_container", "id", Authentication.packageName);
            mFingerprintContent = v.findViewById(fingerprint_container_id);

            int new_fingerprint_enrolled_description_id = getResources()
                    .getIdentifier("new_fingerprint_enrolled_description", "id",
                            Authentication.packageName);

            int fingerprint_icon_id = getResources()
                    .getIdentifier("fingerprint_icon", "id", Authentication.packageName);
            int fingerprint_status_id = getResources()
                    .getIdentifier("fingerprint_status", "id", Authentication.packageName);
            mFingerprintUiHelper = mFingerprintUiHelperBuilder.build(
                    (ImageView) v.findViewById(fingerprint_icon_id),
                    (TextView) v.findViewById(fingerprint_status_id), this);
            updateStage();
        }

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.stopListening();
        }
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        mStage = Stage.BACKUP;
        updateStage();
    }

    private void updateStage() {
        int cancel_id = getResources()
                .getIdentifier("cancel", "string", Authentication.packageName);
        switch (mStage) {
            case FINGERPRINT:
                mCancelButton.setText(cancel_id);
                int use_backup_id = getResources()
                        .getIdentifier("use_backup", "string", Authentication.packageName);
                mSecondDialogButton.setText(use_backup_id);
                mFingerprintContent.setVisibility(View.VISIBLE);
                break;
            case NEW_FINGERPRINT_ENROLLED:
                // Intentional fall through
            case BACKUP:
                if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {

                }
                showStandardAuthScreen();
                break;
        }
    }

    private void showStandardAuthScreen() {
        if (!mKeyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a lock screen.
            int secure_lock_screen_required_id = getResources()
                    .getIdentifier("secure_lock_screen_required", "string",
                            Authentication.packageName);
            Toast.makeText(getContext(),
                    getString(secure_lock_screen_required_id),
                    Toast.LENGTH_LONG).show();
            Authentication.setPluginResultError("Secure lock screen required");
            dismiss();
            return;
        }
        showAuthenticationScreen();
    }

    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == getActivity().RESULT_OK) {
                Authentication.onAuthenticated(false /* used backup */);
            } else {
                Authentication.setPluginResultError("Cancelled");
            }
            dismiss();
        }
    }

    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        Authentication.onAuthenticated(true /* withFingerprint */);
        dismiss();
    }

    @Override
    public void onError() {
        goToBackup();
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        BACKUP
    }
}
