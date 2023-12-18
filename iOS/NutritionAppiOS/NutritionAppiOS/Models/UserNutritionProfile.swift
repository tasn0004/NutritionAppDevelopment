import Foundation
import Parse

class UserNutritionProfile {
    
    var userSex: String
    var calories: Double
    
    var nutritionProfileDictionary: [String: [Any]] = [:]
    
    init() {
        self.userSex = ""
        self.calories = 0.0
    }
    
    /*
     
        Initialize a nutrition profile as the standard unmodifed version.
     
     */
    init(_ userSex: String, _ calories: Double) {
        
        self.userSex = userSex
        self.calories = calories
        
        nutritionProfileDictionary["calories"] = [calories, "cals"]
        
        nutritionProfileDictionary["protein"] = [(0.27 * calories) / 4 , "g"]
        
        nutritionProfileDictionary["carbohydrates"] = [(0.58 * calories) / 4, "g"]
        nutritionProfileDictionary["fibre"] = [14 * (calories/1000), "g"]
        nutritionProfileDictionary["sugar"] = [userSex == "Male" ? 36 : 24, "g"]

        nutritionProfileDictionary["fat"] = [(0.15 * calories) / 9, "g"]
        nutritionProfileDictionary["saturatedFat"] = [0.05 * calories, "g"]
        nutritionProfileDictionary["transFat"] = [0.0085 * calories, "g"]
        nutritionProfileDictionary["cholesterol"] = [300, "mg"]
        
        nutritionProfileDictionary["sodium"] = [2300, "mg"]
        nutritionProfileDictionary["potassium"] = [userSex == "Male" ? 3400 : 2600, "mg"]
        nutritionProfileDictionary["calcium"] = [userSex == "Male" ? 1083 : 842, "mg"]

        nutritionProfileDictionary["vitaminA"] = [userSex == "Male" ? 900 : 700, "mcg"]
        nutritionProfileDictionary["vitaminC"] = [userSex == "Male" ? 90 : 75, "mg"]
        nutritionProfileDictionary["vitaminD"] = [600, "iu"]
        nutritionProfileDictionary["folate"] = [400, "mcg"]
        nutritionProfileDictionary["vitaminB12"] = [2.4, "mcg"]

        nutritionProfileDictionary["iron"] = [userSex == "Male" ? 8 : 18, "mg"]
        nutritionProfileDictionary["magnesium"] = [userSex == "Male" ? 420 : 320, "mg"]
        nutritionProfileDictionary["zinc"] = [userSex == "Male" ? 11 : 8, "mg"]
        
        nutritionProfileDictionary["vegetableServings"] = [userSex == "Male" ? 5 : 4, "servings"]
        nutritionProfileDictionary["fruitServings"] = [userSex == "Male" ? 5 : 4, "servings"]
        nutritionProfileDictionary["grainServings"] = [userSex == "Male" ? 8 : 7, "servings"]
        nutritionProfileDictionary["dairyServings"] = [userSex == "Male" ? 3 : 2, "servings"]
        nutritionProfileDictionary["meatServings"] = [userSex == "Male" ? 2 : 1, "servings"]
        nutritionProfileDictionary["nutsLegumesServings"] = [1, "servings"]
        nutritionProfileDictionary["fatOilServings"] = [userSex == "Male" ? 3 : 2, "servings"]
    }
    
    /*
     
     
     
     */
    func setNutrientValuesByModifier(_ modifierDictionaryName: String) {
        let query = PFQuery(className:"NutritionProfiles")
        
        query.whereKey("name", equalTo: modifierDictionaryName)
        query.getFirstObjectInBackground { (object, error) in
            if let error = error {
                print("Error when trying to find nutrient modifier dictionary. \(error.localizedDescription)\n")
                
            } else if let nutritionProfile = object, let nutrientModifiers = nutritionProfile["nutrientModifiers"] as? [String: Double] {
                //Loop through each nutrient in the modifier dictionary and apply the modification in the user's nutrition profile.
                for (nutrientName, modifierValue) in nutrientModifiers {
                    //If nutrient is macro replace value with new value
                    if self.isMacro(nutrientName) {
                        if nutrientName == "fat" {
                            self.nutritionProfileDictionary[nutrientName]?[0] = (modifierValue * self.calories) / 9
                        } else {
                            self.nutritionProfileDictionary[nutrientName]?[0] = (modifierValue * self.calories) / 4
                        }
                    } else {
                        //else the modifier is a multiplication factor and we do the calculation
                        let currentValue = self.nutritionProfileDictionary[nutrientName]?[0] as? Double ?? 0.0
                        self.nutritionProfileDictionary[nutrientName]?[0] =  currentValue * modifierValue
                    }
                }
            }
        }
    }
    
    /*
     
     
     
     */
    func calculateTotalCalories(_ userSex: String, _ weightKg: Double, _ heightCm: Double, _ age: Double, _ activityFactor: Double, _ weightManagementGoal: String) -> Double {
        var totalCals = 10.0*weightKg + 6.25*heightCm - 5.0*age

        if userSex == "Male" {
            totalCals += 5.0
        } else {
            totalCals -= 161.0
        }
        
        totalCals *= activityFactor
        
        switch weightManagementGoal {
            case "Lose":
                totalCals -= 600
                break
            case "Gain":
                totalCals += 300
                break
            default:
                break
        }
        
        return totalCals
    }
    
    /*
     
     
     
     */
    func isMacro(_ nutrientName: String) -> Bool { return nutrientName == "protein" || nutrientName == "carbohydrates" || nutrientName == "fat" }
}
