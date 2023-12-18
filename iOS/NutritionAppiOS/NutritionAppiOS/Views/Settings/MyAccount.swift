//
//  MyAccount.swift
//  NutritionAppiOS
//
//  Created by Tanisa Tasneem on 2023-09-18.
//

import SwiftUI

struct MyAccount: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @State var screenHeight = 0.0
    @State var screenWidth = 0.0
    
    @State var sex = ""
    
    @State var weight = ""
    @State var weightUnit = "lbs"
    
    @State private var feet = ""
    @State private var inches = ""
    @State private var fractionsOfInch = ""
    @State private var heightUnit = "in"
    
    @State var wristCircumference = ""
    @State var wristCircumferenceUnit = "in"
    
    @State var firstName: String = ""
    @State var lastName: String = ""
    @State var emailAddress: String = ""
    @State var password: String = ""
    @State var birthDate: Date = Date()
    @State var labelSpacing = 0.0

    
    let ageValues = 1...100
    let sexValues = ["Male", "Female"]
    let inchesAndCm = ["in", "cm"]
    let fractionsOfInchValues = ["--", "3/4", "1/2", "1/4", "1/16"]
    let weightUnitValues = ["lbs", "kg"]
    
    let builder = AccountCreationViewBuilders()
    
    init() {
        UISegmentedControl.appearance().selectedSegmentTintColor = .orange

        UINavigationBar.appearance().largeTitleTextAttributes = [.foregroundColor: UIColor.init(.white)]
    }
    
    var body: some View {
        NavigationStack{
            ZStack{
                
                
                /*
                    Set background colour and grab dimensions of screen by storing in state variables
                 */
                GeometryReader { geometry in
                    ComponentColours.backgroundPrimary.ignoresSafeArea()
                        .onAppear() {
                            self.screenHeight = geometry.size.height
                            self.screenWidth = geometry.size.width
                            
                            self.labelSpacing = 0.02*screenWidth
                        }
                }

                Form {
                    Section(header: Text("Personal Information").bold()
                        .foregroundColor(.white)) {
                        TextField("Edit first name", text: $firstName)
                        TextField("Edit last name", text: $lastName)
                        TextField("Edit email address", text: $emailAddress)
                        TextField("Edit password", text: $password)
                        DatePicker("Edit birth date", selection: $birthDate, displayedComponents: .date)
                    }
                    .listRowBackground(Color("neonGreen"))

                    Section(header:Text("Biometric Information").bold().foregroundColor(.white)) {
                        /*
                         Height
                         */
                        Text("Height")
                        HStack {
                            if heightUnit == "in" {
                                createHeightFieldsInches()
                            }
                            else {
                                createHeightFieldsCm()
                            }
                            createUnitSelector(inchesAndCm, $heightUnit)
                        }
                        
                        /*
                         Weight
                         */
                        
                        HStack {
                            builder.createTextField("Weight", "\(weightUnit)", $weight, screenHeight)
                            
                            createUnitSelector(weightUnitValues, $weightUnit)
                                .padding(.top, 0.05*screenHeight)
                        }
                        
                        /*
                         Wrist
                         */
                        
                        HStack {
                            builder.createTextField("Wrist Circumference", "\(wristCircumferenceUnit)", $wristCircumference, screenHeight)
                            
                            createUnitSelector(inchesAndCm, $wristCircumferenceUnit)
                                .padding(.top, 0.053*screenHeight)
                        }
                    }
                    .listRowBackground(Color("neonGreen"))
                }
                .scrollContentBackground(.hidden)
                .navigationBarItems(leading: backButton, trailing: saveButton)
                .navigationTitle("My Account")
            }
        }
    }
    func save() {
    }
    var saveButton: some View {
        Button("Save", action: {
            save()
        })
    }
    
    var backButton: some View {
        Button("Back", action: {
            back()
        })
    }
    
    func back() {
            
    }
    
    func createHeightFieldsInches() -> some View {
        HStack(spacing: 0.015*screenWidth){
            TextField("ft'", text: $feet)
                .keyboardType(.numberPad)
            
            TextField("in''", text: $inches)
                .keyboardType(.numberPad)
            
            Picker("", selection: $fractionsOfInch) {
                ForEach(0..<fractionsOfInchValues.count, id: \.self) { index in
                    Text(fractionsOfInchValues[index])
                }
            }
            .labelsHidden()
            .colorMultiply(colorScheme == .dark ? .white : .black)
            .background(ComponentColours.dataFieldsSecondary)
            .frame(width: 0.135*screenWidth, height: 0.052*screenHeight)
            .cornerRadius(10)
            Spacer()
        }
    }
    
    func createHeightFieldsCm() -> some View {
        HStack(spacing: 0.015*screenWidth){
            TextField("cm", text: $feet)
                .keyboardType(.numberPad)
            Spacer()
        }
    }
    
    func createUnitSelector(_ values: [String], _ storageVariable: Binding<String>) -> some View {
        HStack {
            Spacer()

            Picker("", selection: storageVariable) {
                ForEach(values, id: \.self) { unit in
                    Text(unit)
                }
            }
            .pickerStyle(SegmentedPickerStyle())
            .background(colorScheme == .dark ? Color(.darkGray) : Color(.lightGray))
            .frame(width: 0.20*screenWidth)
            .cornerRadius(8)
        }
    }
}

struct MyAccount_Previews: PreviewProvider {
    static var previews: some View {
        MyAccount()
    }
}
