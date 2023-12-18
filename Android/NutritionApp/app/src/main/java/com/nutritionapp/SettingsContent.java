package com.nutritionapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.parse.ParseUser;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.Nullable;

public class SettingsContent extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_content, container, false);

        /**
         * Initialize MyAccount
         */
        // Initialize imageView3, line1, and MyAccount
        // I know the names sounds stupid but that is how beautiful palwinder named them, and im too lazy to rename id names
        ImageView imageView3 = view.findViewById(R.id.imageView3);
        TextView line1 = view.findViewById(R.id.line1);
        TextView myAccount = view.findViewById(R.id.MyAccount);

        // Set OnClickListener for imageView3, line1, and MyAccount
        View.OnClickListener navigateToSetgAccount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Sett_myAcc.class);
                startActivity(intent);
            }
        };
        imageView3.setOnClickListener(navigateToSetgAccount);
        line1.setOnClickListener(navigateToSetgAccount);
        myAccount.setOnClickListener(navigateToSetgAccount);

        /**
        /**
         * Initialize Payment method
         */
        TextView editPayment = view.findViewById(R.id.editPayment);
        TextView payment = view.findViewById(R.id.payment);
        ImageView paymentIcon = view.findViewById(R.id.paymentIcon);
        View.OnClickListener navigateToEditPaymentList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPaymentPage.class);
                startActivity(intent);
            }
        };
        editPayment.setOnClickListener(navigateToEditPaymentList);
        payment.setOnClickListener(navigateToEditPaymentList);
        paymentIcon.setOnClickListener(navigateToEditPaymentList);

        /**
         * Initialize Promo Code
         */
        TextView PromoCode = view.findViewById(R.id.PromoCode);
        TextView Promotions = view.findViewById(R.id.Promotions);
        ImageView PromotionIcon = view.findViewById(R.id.PromotionIcon);
        View.OnClickListener navigateToPromotion = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Promotion.class);
                startActivity(intent);
            }
        };
        PromoCode.setOnClickListener(navigateToPromotion);
        Promotions.setOnClickListener(navigateToPromotion);
        PromotionIcon.setOnClickListener(navigateToPromotion);

        /**
         * Initialize Diet Edit List
         */
        TextView diet = view.findViewById(R.id.Diet);
        TextView dietList = view.findViewById(R.id.dietList);
        ImageView dietIcon = view.findViewById(R.id.dieticon);
        View.OnClickListener navigateToEditDietList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditDietList.class);
                startActivity(intent);
            }
        };
        diet.setOnClickListener(navigateToEditDietList);
        dietList.setOnClickListener(navigateToEditDietList);
        dietIcon.setOnClickListener(navigateToEditDietList);

        /**
         * Initialize Diet Health List
         */
        TextView health = view.findViewById(R.id.Health);
        TextView healthList = view.findViewById(R.id.healthConcern);
        ImageView healthIcon = view.findViewById(R.id.healthIcon);
        View.OnClickListener navigateToEditHealthList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditHealthList.class);
                startActivity(intent);
            }
        };
        health.setOnClickListener(navigateToEditHealthList);
        healthList.setOnClickListener(navigateToEditHealthList);
        healthIcon.setOnClickListener(navigateToEditHealthList);
        /**
         * Initialize theme APP
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Switch themeSwitch = view.findViewById(R.id.themeSwitch);
        int nightMode = preferences.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_YES);
        boolean isNightMode = (nightMode == AppCompatDelegate.MODE_NIGHT_NO);

        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Switch to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                // Switch to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            // Save the preference
            preferences.edit().putInt("nightMode", isChecked ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES).apply();
        });
        /**
         * Initialize About APP
         */
        TextView Info = view.findViewById(R.id.Info);
        ImageView infoIcon = view.findViewById(R.id.infoIcon);

        // Set OnClickListener for 'Log out'
        View.OnClickListener infoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutApp.class);
                startActivity(intent);
            }
        };

        Info.setOnClickListener(infoClickListener);
        infoIcon.setOnClickListener(infoClickListener);

        /**
         * Initialize Privacy
         */
        TextView Privacy = view.findViewById(R.id.Privacy);
        ImageView privacyIcon = view.findViewById(R.id.privacyIcon);

        // Set OnClickListener for 'Log out'
        View.OnClickListener privacyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacyPolicy.class);
                startActivity(intent);
            }
        };

        Privacy.setOnClickListener(privacyClickListener);
        privacyIcon.setOnClickListener(privacyClickListener);

        /**
         * Initialize logout
         */
        // Initialize 'Log out' TextView and ImageView
        TextView logoutText = view.findViewById(R.id.e);
        ImageView logoutIcon = view.findViewById(R.id.logout);

        // Set OnClickListener for 'Log out'
        View.OnClickListener logoutClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        };

        logoutText.setOnClickListener(logoutClickListener);
        logoutIcon.setOnClickListener(logoutClickListener);

        return view;
    }
    /**
     * dialog message confirmation message for user to logout
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform action to logout and redirect to LoginPage
                        ParseUser.logOut();
                        Intent intent = new Intent(getActivity(), LoginPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }
}

