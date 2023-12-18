package com.nutritionapp;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class UserAccount implements Serializable {

    private String firstName = "";
    private String lastName = "";
    private String emailAddress = "";
    private String password = "";
    private String sex = "";
    private double weight = 0.0;
    private String weightUnit = "";
    private double height = 0.0;
    private String heightCm = "";
    private String heightUnit = "";
    private double wristCircumference = 0.0;
    private String wristCircumferenceUnit = "";
    private String ethnicity = "";
    private double activityLevel = 0.0;
    private String[] dietPreferences;
    private String[] healthConcerns;
    private Date birthDate;
    private String preferredStartDayOfWeek = "";
    private String weightManagementGoal = "";

    // Getters and Setters for each attribute
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(String heightCm) {
        this.heightCm = heightCm;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(String heightUnit) {
        this.heightUnit = heightUnit;
    }

    public double getWristCircumference() {
        return wristCircumference;
    }

    public void setWristCircumference(double wristCircumference) {
        this.wristCircumference = wristCircumference;
    }

    public String getWristCircumferenceUnit() {
        return wristCircumferenceUnit;
    }

    public void setWristCircumferenceUnit(String wristCircumferenceUnit) {
        this.wristCircumferenceUnit = wristCircumferenceUnit;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public double getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(double activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String[] getDietPreferences() {
        return dietPreferences;
    }

    public void setDietPreferences(String[] dietPreferences) {
        this.dietPreferences = dietPreferences;
    }

    public String[] getHealthConcerns() {
        return healthConcerns;
    }

    public void setHealthConcerns(String[] healthConcerns) {
        this.healthConcerns = healthConcerns;
    }

    public void setPreferredStartDayOfWeek(String preferredStartDayOfWeek) {
        this.preferredStartDayOfWeek = preferredStartDayOfWeek;
    }

    public String getPreferredStartDayOfWeek() {
        return preferredStartDayOfWeek;
    }

    public void setWeightManagementGoal(String weightManagementGoal) {
        this.weightManagementGoal = weightManagementGoal;
    }

    public String getWeightManagementGoal() {
        return weightManagementGoal;
    }
    private int calculateAge(Date birthDate) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    // Calculate total calories
    private double calculateTotalCalories() {
        double weightInKg = this.getWeightUnit().equals("lb") ? this.getWeight() * 0.453592 : this.getWeight();
        double heightInCm = this.getHeightUnit().equals("in") ? this.getHeight() * 2.54 : this.getHeight();
        int age = calculateAge(this.getBirthDate());

        double totalCals = this.getSex().equals("Male") ?
                10.0 * weightInKg + 6.25 * heightInCm - 5.0 * age + 5.0 :
                10.0 * weightInKg + 6.25 * heightInCm - 5.0 * age - 161.0;

        if (this.getWeightManagementGoal().equals("Lose")) {
            totalCals -= 600;
        } else if (this.getWeightManagementGoal().equals("Gain")) {
            totalCals += 300;
        }

        return totalCals * this.getActivityLevel();
    }

    // Create the nutrition profile based on calculated values
    private JSONObject createNutritionProfile() {

        JSONObject nutritionProfile = new JSONObject();
        try {
        double calories = calculateTotalCalories();
        String userSex = this.getSex();
        // Now we will put the calculated values into the nutrition profile
        nutritionProfile.put("calories", new JSONArray(Arrays.asList(calories, "cals")));
        nutritionProfile.put("protein", new JSONArray(Arrays.asList((0.27 * calories) / 4, "g")));
        nutritionProfile.put("carbohydrates", new JSONArray(Arrays.asList((0.58 * calories) / 4, "g")));
        nutritionProfile.put("fibre", new JSONArray(Arrays.asList(14 * (calories / 1000), "g")));
        nutritionProfile.put("sugar", new JSONArray(Arrays.asList(userSex.equals("Male") ? 36 : 24, "g")));
        nutritionProfile.put("fat", new JSONArray(Arrays.asList((0.15 * calories) / 9, "g")));
        nutritionProfile.put("saturatedFat", new JSONArray(Arrays.asList(0.05 * calories, "g")));
        nutritionProfile.put("transFat", new JSONArray(Arrays.asList(0.0085 * calories, "g")));
        nutritionProfile.put("cholesterol", new JSONArray(Arrays.asList(300, "mg")));
        nutritionProfile.put("sodium", new JSONArray(Arrays.asList(2300, "mg")));
        nutritionProfile.put("potassium", new JSONArray(Arrays.asList(userSex.equals("Male") ? 3400 : 2600, "mg")));
        nutritionProfile.put("calcium", new JSONArray(Arrays.asList(userSex.equals("Male") ? 1083 : 842, "mg")));
        nutritionProfile.put("iron", new JSONArray(Arrays.asList(userSex.equals("Male") ? 8 : 18, "mg")));
        nutritionProfile.put("vitaminA", new JSONArray(Arrays.asList(userSex.equals("Male") ? 900 : 700, "mcg")));
        nutritionProfile.put("vitaminC", new JSONArray(Arrays.asList(userSex.equals("Male") ? 90 : 75, "mg")));
        nutritionProfile.put("vitaminD", new JSONArray(Arrays.asList(600, "iu")));
        nutritionProfile.put("folate", new JSONArray(Arrays.asList(400, "mcg")));
        nutritionProfile.put("vitaminB12", new JSONArray(Arrays.asList(2.4, "mcg")));
        nutritionProfile.put("magnesium", new JSONArray(Arrays.asList(userSex.equals("Male") ? 420 : 320, "servings")));
        nutritionProfile.put("zinc", new JSONArray(Arrays.asList(userSex.equals("Male") ? 11 : 8, "servings")));
        nutritionProfile.put("vegetableServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 5 : 4, "servings")));
        nutritionProfile.put("fruitServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 5 : 4, "servings")));
        nutritionProfile.put("grainServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 8 : 7, "servings")));
        nutritionProfile.put("dairyServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 3 : 2, "servings")));
        nutritionProfile.put("meatServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 2 : 1, "servings")));
        nutritionProfile.put("nutsLegumesServings", new JSONArray(Arrays.asList(1, "mg")));
        nutritionProfile.put("fatOilServings", new JSONArray(Arrays.asList(userSex.equals("Male") ? 3 : 2, "servings")));

        } catch (JSONException e) {
            // Handle exception here
            e.printStackTrace();
        }

        return nutritionProfile;
    }
    private boolean isMacro(String nutrient) {
        return nutrient.equals("fat") || nutrient.equals("protein") || nutrient.equals("carbohydrates");
    }
    private void applyNutrientModifiers(JSONObject nutritionProfile, JSONArray healthConcerns, int index, NutritionProfileCallback callback) {
        if (index < healthConcerns.length()) {
            try {
                String healthConcern = healthConcerns.getString(index);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("NutritionProfiles");
                query.whereEqualTo("name", healthConcern);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null && object != null) {
                            JSONObject nutrientModifiers = object.getJSONObject("nutrientModifiers");
                            applyModifiersToProfile(nutritionProfile, nutrientModifiers);
                        }
                        // Process next health concern or complete
                        applyNutrientModifiers(nutritionProfile, healthConcerns, index + 1, callback);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onCompleted(nutritionProfile); // In case of an error, return the current profile
            }
        } else {
            callback.onCompleted(nutritionProfile); // All health concerns processed
        }
    }

    private void applyModifiersToProfile(JSONObject nutritionProfile, JSONObject nutrientModifiers) {
        Iterator<String> keys = nutrientModifiers.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                double modifierValue = nutrientModifiers.getDouble(key);
                JSONArray nutrientArray = nutritionProfile.optJSONArray(key);

                if (nutrientArray != null && nutrientArray.length() > 0) {
                    double currentValue = nutrientArray.getDouble(0);

                    if (isMacro(key)) {
                        double calories = nutritionProfile.getJSONArray("calories").getDouble(0);
                        currentValue = key.equals("fat") ?
                                (modifierValue * calories) / 9 :
                                (modifierValue * calories) / 4;
                    } else {
                        currentValue *= modifierValue;
                    }

                    nutrientArray.put(0, currentValue);
                    nutritionProfile.put(key, nutrientArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface NutritionProfileCallback {
        void onCompleted(JSONObject updatedNutritionProfile);
    }

    public void saveToBack4App() {
        JSONObject nutritionProfile = createNutritionProfile();
        JSONArray healthConcerns = new JSONArray(Arrays.asList(this.getHealthConcerns()));

        if (healthConcerns.length() == 0 || (healthConcerns.length() == 1 && healthConcerns.optString(0, "").equalsIgnoreCase("None"))) {
            saveUserWithProfile(nutritionProfile);
        } else {
            applyNutrientModifiers(nutritionProfile, healthConcerns, 0, this::saveUserWithProfile);
        }
    }



    private void saveUserWithProfile(JSONObject nutritionProfile) {
        ParseUser user = new ParseUser();
        user.setUsername(this.getEmailAddress());
        user.setPassword(this.getPassword());
        user.setEmail(this.getEmailAddress());
        user.put("firstName", this.getFirstName());
        user.put("lastName", this.getLastName());
        user.put("sex", this.getSex());
        user.put("wristCircumference", this.getWristCircumference());
        user.put("wristCircumferenceUnit", this.getWristCircumferenceUnit());
        user.put("dietPreferences", Arrays.asList(this.getDietPreferences()));
        user.put("height", this.getHeight());
        user.put("heightUnit", this.getHeightUnit());
        user.put("birthdate", this.getBirthDate());
        user.put("healthConcerns", Arrays.asList(this.getHealthConcerns()));
        user.put("activityLevel", this.getActivityLevel());
        user.put("weight", this.getWeight());
        user.put("weightUnit", this.getWeightUnit());
        user.put("ethnicity", this.getEthnicity());
        user.put("preferredStartDayOfWeek", this.getPreferredStartDayOfWeek());
        user.put("weightManagementGoal", this.getWeightManagementGoal());
        user.put("isPaidAccount", true);
        user.put("nutritionProfile", nutritionProfile);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    System.out.println(e);
                } else {
                    System.out.println(e);
                }
            }
        });
    }
}
