package com.nutritionapp;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Sett_myAcc extends AppCompatActivity {


    private Calendar c = Calendar.getInstance();
    String selectedDate;
    ParseUser currentUser = ParseUser.getCurrentUser();
    UserAccount userData;
    private Spinner heightInchSpinner;
    private EditText weightInput, heightInput, heightInputInch, wristCircumferenceInput, editFirstName,
            editLastName, editEmail, editHeight, editWeight, editWrist;
    private TextView editPassword;
    private RadioGroup radioGroupForGenders, radioGroupForWeightMeasurement, heightRadioGroup, wristCircumferenceRadioGroup;
    private RadioButton heightIn, heightCm, weightLb, weightKg, wristCm, wristIn;
    private String heightFeet;
    private String heightInches;
    private String heightFractionOfInch;
    private String weightTotal;
    private String wristTotal;
    private String heightUnit;
    private String weightUnit;
    private String wristUnit;
    private double totalHeight;
    private double wristCircumference;
    private double totalWeight;
    private String heightTotal;
    private double totalHeightSave;
    private String password, changedPassword;
    private Boolean passwordValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sett_account);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        userData = (UserAccount) getIntent().getSerializableExtra("userData");

        Button saveButton = findViewById(R.id.saveButton);

        wristCircumferenceRadioGroup = findViewById(R.id.wristRadioGroup);
        radioGroupForWeightMeasurement = findViewById(R.id.weightRadioGroup);
        heightRadioGroup = findViewById(R.id.heightRadioGroup1);
        heightInputInch = findViewById(R.id.editHeight2);
        wristCircumferenceInput = findViewById(R.id.editWrist);
        heightInchSpinner = findViewById(R.id.heightInchSpinner);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editHeight = findViewById(R.id.editHeight1);
        editWeight = findViewById(R.id.editWeight);
        editWrist = findViewById(R.id.editWrist);
        editPassword = findViewById(R.id.editPassword);
        heightIn = findViewById(R.id.inButton);
        heightCm = findViewById(R.id.cmButton);
        weightLb = findViewById(R.id.lbButton);
        weightKg = findViewById(R.id.kgButton);
        wristIn = findViewById(R.id.inButtonWrist);
        wristCm = findViewById(R.id.cmButtonWrist);

        heightUnit = currentUser.getString("heightUnit");
        weightUnit = currentUser.getString("weightUnit");
        wristUnit = currentUser.getString("wristCircumferenceUnit");

        editFirstName.setText(currentUser.getString("firstName"));
        editLastName.setText(currentUser.getString("lastName"));
        editEmail.setText(currentUser.getEmail());

        totalHeight = currentUser.getDouble("height");
        totalWeight = currentUser.getDouble("weight");
        wristCircumference = currentUser.getDouble("wristCircumference");


        if (heightUnit.equals("cm")) {
            editHeight.setText(String.format("%.1f", totalHeight));
            heightCm.setChecked(true);
            heightIn.setChecked(false);

        } else if (heightUnit.equals("in")) {
            heightInputInch.setVisibility(View.VISIBLE);
            heightInchSpinner.setVisibility(View.VISIBLE);
            convertAndSetHeightInches(totalHeight);
            editHeight.setText(String.format("%s", heightFeet));
            heightInputInch.setText(String.format("%s", heightInches));
            heightUnit = "in";
            switch (heightFractionOfInch) {
                case "1/16":
                    heightInchSpinner.setSelection(1);
                    break;
                case "1/4":
                    heightInchSpinner.setSelection(2);
                    break;
                case "1/2":
                    heightInchSpinner.setSelection(3);
                    break;
                case "3/4":
                    heightInchSpinner.setSelection(4);
                    break;
                default:
                    heightInchSpinner.setSelection(0);
                    break;
            }
            heightCm.setChecked(false);
            heightIn.setChecked(true);
        }

        if (weightUnit.equals("kg")) {
            editWeight.setText(String.format("%.1f", totalWeight));
            weightKg.setChecked(true);
            weightLb.setChecked(false);

        } else if (weightUnit.equals("lb")) {
            editWeight.setText(String.format("%.1f", totalWeight));
            weightKg.setChecked(false);
            weightLb.setChecked(true);
        }

        if (wristUnit.equals("cm")) {
            editWrist.setText(String.format("%.1f", wristCircumference));
            wristCm.setChecked(true);
            wristIn.setChecked(false);

        } else if (wristUnit.equals("in")) {
            editWrist.setText(String.format("%.1f", wristCircumference));
            wristCm.setChecked(false);
            wristIn.setChecked(true);
        }

        editPassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Sett_myAcc.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_edit_password, null);

            EditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
            EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);

            builder.setView(dialogView)
                    .setTitle("Edit Password")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            String oldPassword = oldPasswordInput.getText().toString().trim();
                            String newPassword = newPasswordInput.getText().toString().trim();

                            ParseUser.logInInBackground(currentUser.getUsername(), oldPassword, (user, e) -> {
                                if (user != null) {
                                    // Old password is correct, proceed to update the password
                                    currentUser.setPassword(newPassword);
                                    currentUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(Sett_myAcc.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Sett_myAcc.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    // Old password is incorrect, show an error message
                                    Toast.makeText(Sett_myAcc.this, "Incorrect old password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        final Button dateButton = findViewById(R.id.date_button);
        final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        Date birthdate = currentUser.getDate("birthdate");
        dateButton.setText(sdf.format(birthdate));
        c.setTime(birthdate);

        dateButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Get Current Date
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Sett_myAcc.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                // Update calendar instance with selected date
                                c.set(Calendar.YEAR, year);
                                c.set(Calendar.MONTH, month);
                                c.set(Calendar.DAY_OF_MONTH, day);

                                // Format and display the selected date in button
                                selectedDate = sdf.format(c.getTime());
                                dateButton.setText(selectedDate);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        /********************************************************************/
        radioGroupForWeightMeasurement.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lbButton) {
                double totalWeightLb = totalWeight * 2.2;
                totalWeight = totalWeightLb;
                editWeight.setText(String.format("%.1f", totalWeight));
                weightUnit = "lb";
            } else if (checkedId == R.id.kgButton) {
                double totalWeightKg = totalWeight / 2.2;
                totalWeight = totalWeightKg;
                editWeight.setText(String.format("%.1f", totalWeight));
                weightUnit = "kg";
            }
        });

        /********************************************************************/
        heightRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.inButton) {
                heightInputInch.setVisibility(View.VISIBLE);
                heightInchSpinner.setVisibility(View.VISIBLE);
                double totalHeightIn = totalHeight * 0.393701;
                totalHeight = totalHeightIn;
                convertAndSetHeightInches(totalHeightIn);
                editHeight.setText(String.format("%s", heightFeet));
                heightInputInch.setText(String.format("%s", heightInches));
                heightUnit = "in";
                switch (heightFractionOfInch) {
                    case "1/16":
                        heightInchSpinner.setSelection(1);
                        break;
                    case "1/4":
                        heightInchSpinner.setSelection(2);
                        break;
                    case "1/2":
                        heightInchSpinner.setSelection(3);
                        break;
                    case "3/4":
                        heightInchSpinner.setSelection(4);
                        break;
                    default:
                        heightInchSpinner.setSelection(0);
                        break;
                }
            } else if (checkedId == R.id.cmButton) {
                heightInputInch.setVisibility(View.GONE);
                heightInchSpinner.setVisibility(View.GONE);
                double totalHeightCm = totalHeight * 2.54;
                totalHeight = totalHeightCm;
                editHeight.setText(String.format("%.1f", totalHeightCm));
                heightUnit = "cm";
            }
        });
        /********************************************************************/
        wristCircumferenceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.inButtonWrist) {
                double totalWristIn = wristCircumference * 0.393701;
                wristCircumference = totalWristIn;
                wristCircumferenceInput.setText(String.format("%.1f", wristCircumference));
                wristUnit = "in";
            } else if (checkedId == R.id.cmButtonWrist) {
                double totalWristCm = wristCircumference * 2.54;
                wristCircumference = totalWristCm;
                wristCircumferenceInput.setText(String.format("%.1f", wristCircumference));
                wristUnit = "cm";
            }
        });
        saveButton.setOnClickListener(v -> {
            weightTotal = editWeight.getText().toString().trim();
            totalWeight = Double.parseDouble(weightTotal);
            wristTotal = editWrist.getText().toString().trim();
            wristCircumference = Double.parseDouble(wristTotal);

            if (heightUnit.equals("cm")) {
                heightTotal = editHeight.getText().toString().trim();
                totalHeight = Double.parseDouble(heightTotal);
                currentUser.put("height", totalHeight);
            } else if (heightUnit.equals("in")) {
                heightFeet = editHeight.getText().toString().trim();
                heightInches = heightInputInch.getText().toString().trim();
                heightFractionOfInch = heightInchSpinner.getSelectedItem().toString();
                totalHeight = ((Double.parseDouble(heightFeet) * 12) + (Double.parseDouble(heightInches)) + convertFractionsOfInchToDouble(heightFractionOfInch));
                currentUser.put("height", totalHeight);
            }


            currentUser.setEmail(editEmail.getText().toString().trim());
            currentUser.put("firstName", editFirstName.getText().toString().trim());
            currentUser.put("lastName", editLastName.getText().toString().trim());
            currentUser.put("birthdate", c.getTime());
            currentUser.put("weight", totalWeight);
            currentUser.put("wristCircumference", wristCircumference);
            currentUser.put("heightUnit", heightUnit);
            currentUser.put("weightUnit", weightUnit);
            currentUser.put("wristCircumferenceUnit", wristUnit);

            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(Sett_myAcc.this, "Update Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Sett_myAcc.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Intent intent = new Intent(Sett_myAcc.this, Settings.class);
            startActivity(intent);
        });
    }

    /**
     * This method converts the total inches to how inches and feet are normally displayed
     * @param totalInches
     */
    public void convertAndSetHeightInches(double totalInches) {
        int feet = (int) Math.floor(totalInches / 12);
        double remainingInches = totalInches % 12;
        int wholeInches = (int) Math.floor(remainingInches);
        double fractionOfInch = remainingInches - wholeInches;

        this.heightFeet = String.valueOf(feet);
        this.heightInches = String.valueOf(wholeInches);
        this.heightFractionOfInch = convertFractionOfInchToString(fractionOfInch);
    }

    /**
     * Converts the fraction inch spinner to a string
     * @param total
     * @return
     */
    public static String convertFractionOfInchToString(double total) {
        if (total > 0 && total <= 0.1) {
            return "1/16";
        } else if (total > 0.1 && total <= 0.3) {
            return "1/4";
        } else if (total > 0.3 && total <= 0.6) {
            return "1/2";
        } else if (total > 0.6 && total <= 0.99) {
            return "3/4";
        } else {
            return "--";
        }
    }

    /**
     * Converts the fraction inch string to a double
     * @param heightFractionOfInch
     * @return
     */
    public double convertFractionsOfInchToDouble(String heightFractionOfInch) {
        switch (heightFractionOfInch) {
            case "1/16":
                return 0.0625;

            case "1/4":
                return 0.25;

            case "1/2":
                return 0.5;

            case "3/4":
                return 0.75;

            default:
                return 0.0;
        }
    }
}