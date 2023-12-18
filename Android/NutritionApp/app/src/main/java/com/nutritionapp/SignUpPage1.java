package com.nutritionapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SignUpPage1 extends AppCompatActivity {

    private ImageButton nextButton1;
    private SimpleDateFormat sdf;
    private Calendar c;
    private Spinner heightInchSpinner;
    private Button ageSelectButton;
    private TextView backText;
    private EditText weightInput, heightInput, heightInputInch, WristCircumferenceInput;
    private RadioGroup radioGroupForGenders, radioGroupForWeightMeasurement, heightRadioGroup, WristCircumferenceRadioGroup;
    private String selectedEthnicity = null; // to hold the selected ethnicity
    private void showEthnicityDialog() {
        String[] ethnicities = {"Caucasian", "African", "Arab", "Latin"," East Asian", "South Asian"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpPage1.this);
        builder.setTitle("Select Ethnicity");
        builder.setItems(ethnicities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedEthnicity = ethnicities[which];
                Button ethnicityButton = findViewById(R.id.ethnicityButton);
                ethnicityButton.setText(selectedEthnicity);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up1);
        /************************* hide top bar ***************************************************/
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        /************************* Linking the UI elements from XML layout to Java variables ***************************************************/
        nextButton1 = findViewById(R.id.nextButton1);
        radioGroupForGenders = findViewById(R.id.radioGroupForGenders);
        WristCircumferenceRadioGroup = findViewById(R.id.WristCircumferenceRadioGroup);
        radioGroupForWeightMeasurement = findViewById(R.id.radioGroup);
        heightRadioGroup = findViewById(R.id.heightRadioGroup);
        weightInput = findViewById(R.id.weightInput);
        heightInput = findViewById(R.id.heightInput);
        heightInputInch = findViewById(R.id.heightInputInch);
        heightInchSpinner = findViewById(R.id.heightInchSpinner);
        WristCircumferenceInput = findViewById(R.id.WristCircumferenceInput);
        ageSelectButton = findViewById(R.id.ageSelect);
        backText = findViewById(R.id.backText1);
        sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        c = Calendar.getInstance();
        /************************* back function ***************************************************/
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        /**********************  user age by date form  *********************************/
        // Set today's date
        String currentDate = sdf.format(c.getTime());
        ageSelectButton.setText(currentDate);
        ageSelectButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // Get current date
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpPage1.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    c.set(Calendar.YEAR, year);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    c.set(Calendar.MONTH, month);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    c.set(Calendar.DAY_OF_MONTH, day);
                                }
                                // Format and display the selected date in the button
                                String selectedDate = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    selectedDate = sdf.format(c.getTime());
                                }
                                ageSelectButton.setText(selectedDate);
                            }
                        }, mYear, mMonth, mDay);

                datePickerDialog.show();
            }
        });

        /*************** hint weightInput **********************************/
        weightInput.setHint("kg"); // SET DEFAULT AS
        radioGroupForWeightMeasurement.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lbButton) {
                weightInput.setHint("lb");
            } else if (checkedId == R.id.kgButton) {
                weightInput.setHint("kg");
            }
        });

        /*************** hint heightInput **********************************/
        heightInput.setHint("cm"); // SET DEFAULT AS
        heightRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.inButton) {
                heightInputInch.setVisibility(View.VISIBLE);
                heightInchSpinner.setVisibility(View.VISIBLE);
                heightInput.setHint("ft\'");
                heightInputInch.setHint("in\"");
            } else if (checkedId == R.id.cmButton) {
                heightInputInch.setVisibility(View.GONE);
                heightInchSpinner.setVisibility(View.GONE);
                heightInput.setHint("cm");
            }
        });
        /*************** hint WristCircumferenceInput **********************************/
        WristCircumferenceInput.setHint("cm"); // SET DEFAULT AS
        WristCircumferenceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.inWCButton) {
                WristCircumferenceInput.setHint("in");
            } else if (checkedId == R.id.cmWCButton) {
                WristCircumferenceInput.setHint("cm");
            }
        });
        Button ethnicityButton = findViewById(R.id.ethnicityButton);
        ethnicityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEthnicityDialog();
            }
        });

        /***********************************************************************************************/
        /************************   nextButton action !!!!  ********************************************/
        /***********************************************************************************************/
        nextButton1.setOnClickListener(v -> {
            if (!areInputsValid()) {
                Toast.makeText(SignUpPage1.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Retrieve the intent that started from previous page
            UserAccount userData = (UserAccount) getIntent().getSerializableExtra("userData");
            //if UserAccount is empty creating new object
            if (userData == null) {
                userData = new UserAccount();
            }
            /**************set gender to userData********************/
            int selectedGenderId = radioGroupForGenders.getCheckedRadioButtonId();
            if (selectedGenderId == R.id.maleButton) {
                userData.setSex("Male");
            } else if (selectedGenderId == R.id.femaleButton) {
                userData.setSex("Female");
            }
            /**************set birthday to UserAccount********************/
            userData.setBirthDate(c.getTime());
            /**************set height input to UserAccount********************/
            int selectedHeightUnitId = heightRadioGroup.getCheckedRadioButtonId();//initialize heightRadioGroup

            if (selectedHeightUnitId == R.id.inButton) {//if select inch
                // Get feet and inches input
                String heightFeet = heightInput.getText().toString();
                String heightInch = heightInputInch.getText().toString();

                if (!heightFeet.isEmpty() && !heightInch.isEmpty()) {//if the text field not empty
                    int totalHeightInInches = (Integer.parseInt(heightFeet) * 12) + Integer.parseInt(heightInch);//calculate in and feet
                    userData.setHeight(totalHeightInInches);  // Store the total height in inches
                }
                userData.setHeightUnit("in");//store unit

            } else if (selectedHeightUnitId == R.id.cmButton) {//if select cm
                String heightCm = heightInput.getText().toString();//get input
                if (!heightCm.isEmpty()) {//if not empty
                    double heightInCm = Double.parseDouble(heightCm);//initialize height
                    userData.setHeight(heightInCm);  // Store the height in centimeters
                }
                userData.setHeightUnit("cm");//store unit cm to UserAccount
            }
            /**************set weight input to UserAccount********************/
            int selectedWeightUnitId = radioGroupForWeightMeasurement.getCheckedRadioButtonId(); //initialize the radioGroupForWeightMeasurement

            if (selectedWeightUnitId == R.id.lbButton) { //if lbs is selected
                String weightLbs = weightInput.getText().toString(); //get the weight input
                if (!weightLbs.isEmpty()) { // if the text field is not empty
                    double weightInLbs = Double.parseDouble(weightLbs); // parse the weight
                    userData.setWeight(weightInLbs);  // Store the weight in lbs
                }
                userData.setWeightUnit("lb"); //store unit

            } else if (selectedWeightUnitId == R.id.kgButton) { // if kgs is selected
                String weightKgs = weightInput.getText().toString(); //get the weight input
                if (!weightKgs.isEmpty()) { // if the text field is not empty
                    double weightInKgs = Double.parseDouble(weightKgs); // parse the weight
                    userData.setWeight(weightInKgs);  // Store the weight in kgs
                }
                userData.setWeightUnit("kg"); //store unit
            }
            /**************set wrist circumference  input to UserAccount********************/
            int selectedWristUnitId = WristCircumferenceRadioGroup.getCheckedRadioButtonId(); //initialize the WristCircumferenceRadioGroup
            if (selectedWristUnitId == R.id.inWCButton) { //if inches is selected
                String wristCircumferenceInInches = WristCircumferenceInput.getText().toString(); //get the wrist circumference input
                if (!wristCircumferenceInInches.isEmpty()) { // if the text field is not empty
                    double circumferenceInInches = Double.parseDouble(wristCircumferenceInInches); // parse the wrist circumference
                    userData.setWristCircumference(circumferenceInInches);  // Store the wrist circumference in inches
                }
                userData.setWristCircumferenceUnit("in"); //store unit
            } else if (selectedWristUnitId == R.id.cmWCButton) { // if centimeters is selected
                String wristCircumferenceInCm = WristCircumferenceInput.getText().toString(); //get the wrist circumference input
                if (!wristCircumferenceInCm.isEmpty()) { // if the text field is not empty
                    double circumferenceInCm = Double.parseDouble(wristCircumferenceInCm); // parse the wrist circumference
                    userData.setWristCircumference(circumferenceInCm);  // Store the wrist circumference in centimeters
                }
                userData.setWristCircumferenceUnit("cm"); //store unit
            }
            if (selectedEthnicity != null) {
                userData.setEthnicity(selectedEthnicity);
            }
            //go to next page (SignUpPage2)
            Intent nextPageIntent = new Intent(SignUpPage1.this, SignUpPage2.class);
            nextPageIntent.putExtra("userData", userData);
            startActivity(nextPageIntent);
        });
    }
    private boolean areInputsValid() {
        boolean isHeightValid = heightRadioGroup.getCheckedRadioButtonId() != R.id.inButton || !heightInputInch.getText().toString().trim().isEmpty();
        boolean isDateOfBirthSelected = !ageSelectButton.getText().toString().equals("Select Date of Birth");
        return radioGroupForGenders.getCheckedRadioButtonId() != -1 &&
                !weightInput.getText().toString().trim().isEmpty() &&
                !heightInput.getText().toString().trim().isEmpty() &&
                isHeightValid &&
                !WristCircumferenceInput.getText().toString().trim().isEmpty() &&
                selectedEthnicity != null &&
                isDateOfBirthSelected;
    }



}
