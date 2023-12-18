import SwiftUI
import Parse

struct AddItemButton: View {

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //Alerts controls
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""

    @State var showingPopover: Bool = false

    @Binding var shouldRefresh: Bool
    @Binding var selectedDate: Date
    
    @State var addItemName = ""
    @State var addItemQuantity = ""
    @State var addItemQuantityUnit = ""
    
    var isAddedItemValid: Bool {
        return !addItemName.isEmpty || !addItemQuantity.isEmpty || !addItemQuantityUnit.isEmpty
    }
    
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()
    
    init(_ shouldRefresh: Binding<Bool>, _ selectedDate: Binding<Date>) {
        self._shouldRefresh = shouldRefresh
        self._selectedDate = selectedDate
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Rectangle()
                .foregroundColor(.orange)

            HStack {
                Image(systemName: "plus.circle")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 0.04*screenWidth)

                Text("Add item")
                    .font(.headline)

            }
            .padding(0.015*screenWidth)
            .foregroundColor(.white)

        }
        .frame(width: 0.40*screenWidth, height: 0.05*screenHeight)
        .cornerRadius(10)
        .onTapGesture {
            showingPopover = true
        }
        .alert(isPresented: $isAlertPresented) {
            Alert(title: Text(alertTitle), message: Text(alertMessage),
                  dismissButton: .default(Text("OK"),
                      action: {
                        isAlertPresented = false
                      }
                  ))
        }
        .popover(isPresented: $showingPopover) {
            addItemToListPopover()
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    /*

        Creates the popover view when a user selects add meal with no date context selected. Contains logic for handling input + adding to meal plan.

     */
    func addItemToListPopover() -> some View {
        VStack {
            //Top row cancel/add buttons
            HStack {
                // Cancel button
                Button(action: {
                    showingPopover = false
                }) {
                    Text("Cancel").fontWeight(.semibold)
                }
                .foregroundColor(.blue)
                .padding()
                Spacer()

                //Add button
                Button(action: {
                    //close the pop over if we have the data
                    showingPopover = false
                }) {
                    Text("Add").fontWeight(.semibold)
                }
                .disabled(!isAddedItemValid) //disable if we dont have the data
                .simultaneousGesture(TapGesture().onEnded {
                    //Show alert when user presses done without selecting data. Date will be current day by default
                    if !isAddedItemValid {
                        alertTitle = "Error"
                        alertMessage = "New item must have all information."
                        isAlertPresented.toggle()

                    } else {
                        //else we have our data and can add item to groceryList
                        addItemToGroceryList()
                    }
                })
                .alert(isPresented: $isAlertPresented) {
                    Alert(title: Text(alertTitle), message: Text(alertMessage),
                          dismissButton: .default(Text("OK"),
                              action: {
                                //if alert is success alert, close popover after dismissing
                                if alertTitle == "Success!" {
                                    showingPopover = false
                                }}))
                }
                .foregroundColor(.blue)
                .padding()
            }
            Divider()
            
            //Info textfields
            VStack {
                VStack(spacing: 0.01*screenHeight) {
                    HStack() {
                        Text("Item name: ")
                        Spacer()
                    }
                    .padding(.leading, 0.02*screenWidth)

                    TextField(addItemName, text: $addItemName)
                }

                VStack(spacing: 0.01*screenHeight) {
                    HStack() {
                        Text("Item quantity: ")
                        Spacer()
                    }
                    .padding(.leading, 0.02*screenWidth)

                    TextField(addItemQuantity, text: $addItemQuantity)
                        .keyboardType(.numberPad)
                }

                VStack(spacing: 0.01*screenHeight) {
                    HStack() {
                        Text("Item quantity unit: ")
                        Spacer()
                    }
                    .padding(.leading, 0.02*screenWidth)

                    TextField(addItemQuantityUnit, text: $addItemQuantityUnit)
                }
            }
            .textFieldStyle(CustomTextFieldStyle(screenWidth, screenHeight))
            Spacer()
        }
        .modifier(CustomTextLabelStyle(0.045*screenWidth))
        .background(Color("background"))
    }
   
    /*
     
        Adds a user defined item to the grocery list after filling out the add item popover info.
     
     */
    func addItemToGroceryList() {
        let formattedDate = dateFormatter.string(from: selectedDate)
        
        var newGroceryList = backendUtilities.getUserGroceryList()
        var groceryListDay = newGroceryList[formattedDate] as? [[String: Any]] ?? [[:]]
        
        let newIngredient: [String: Any] = ["ingredientName": addItemName,
                                            "quantity": (addItemQuantity as NSString).doubleValue,
                                            "quantityUnit": addItemQuantityUnit,
                                            "mealPlanAdded": false,
                                            "isToggled": false]
        
        groceryListDay.append(newIngredient)
        
        //Update the userGroceryList dictionary with the modified array
        newGroceryList[formattedDate] = groceryListDay
        
        backendUtilities.updateUserGroceryList(newGroceryList) { success in
            if success {
                print("Successfully added \(addItemQuantity) \(addItemQuantityUnit) \(addItemName) to grocery list.\n")
                alertTitle = "Success!"
                alertMessage = "New item successfully added to grocery list."
                isAlertPresented = true
                
                //reinitialize daily list items to show the new item
                shouldRefresh = true
            } else {
                print("Unsuccessfully added \(addItemQuantity) \(addItemQuantityUnit) \(addItemName) to grocery list.\n")
                
                self.alertTitle = "Error"
                self.alertMessage = "Adding new item unsuccessful."
                self.isAlertPresented.toggle()
            }
        }
    }
}
